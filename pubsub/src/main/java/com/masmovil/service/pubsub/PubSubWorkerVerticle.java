package com.masmovil.service.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.concurrent.*;

public class PubSubWorkerVerticle extends AbstractVerticle {

    private final ProjectSubscriptionName projectSubscriptionName;
    private Subscriber subscriber = null;
    private Future<?> submitFuture = null;
    private ExecutorService eventProcessorExecutor = null;

    Logger log = LoggerFactory.getLogger(PubSubWorkerVerticle.class);

    public PubSubWorkerVerticle(ProjectSubscriptionName projectSubscriptionName) {
        this.projectSubscriptionName = projectSubscriptionName;
        log.info(projectSubscriptionName.getFieldValuesMap());
    }

    @Override
    public void start() {

        EventBus eventBus = vertx.eventBus();
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("pubsub-subscriber-thread-%d")
            .setDaemon(true)
            .build();
        eventProcessorExecutor = Executors.newSingleThreadExecutor(threadFactory);
        MessageReceiver receiver = createMessageReceiver(eventBus);

        try {

            submitFuture = eventProcessorExecutor.submit(() -> {
                System.out.println("Start PubSub Worker Verticle");
                Subscriber subscriber = null;

                try {
                    subscriber =
                        Subscriber.newBuilder(projectSubscriptionName, receiver).build();

                    subscriber.startAsync().awaitTerminated();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (subscriber != null) {
                        System.out.println("Shutdown PubSub Async Subscriber");
                        subscriber.stopAsync().awaitTerminated();
                    }
                }
            });
        } catch(Exception e) {
            log.error("Error creating subscription", e);
            stop();
                throw e;
        }

    }

    public MessageReceiver createMessageReceiver(EventBus eventBus) {
        log.info("Creating message receiver");

        return ((PubsubMessage pubsubMessage, AckReplyConsumer consumer) -> {
            try {
                // handle incoming message, then ack/nack the received message
                //System.out.println("Id : " + pubsubMessage.getMessageId());
                String message = pubsubMessage.getData().toStringUtf8();
                //System.out.println("Data : " + Json.encodePrettily(message));

                log.info("Received message " + pubsubMessage.getMessageId() + " from pubsub");
                eventBus.send(PubSubSubscriberImpl.EVENT_NAME, message);
                consumer.ack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated();
        }

        if (submitFuture != null) {
            log.info("Stopping future");
            log.info("Stopped future? " + submitFuture.cancel(true));
        }

        if (eventProcessorExecutor != null) {
            eventProcessorExecutor.shutdown();
        }
    }
}
