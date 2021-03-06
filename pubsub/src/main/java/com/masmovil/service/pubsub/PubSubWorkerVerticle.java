package com.masmovil.service.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class PubSubWorkerVerticle extends AbstractVerticle {

    private final ProjectSubscriptionName projectSubscriptionName;
    private Subscriber subscriber = null;
    private Future<?> submitFuture = null;
    private ExecutorService eventProcessorExecutor = null;

    public PubSubWorkerVerticle(ProjectSubscriptionName projectSubscriptionName) {
        this.projectSubscriptionName = projectSubscriptionName;
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
                //System.out.println("Start PubSub Worker Verticle");
                try {
                    // create a subscriber bound to the asynchronous message receiver
                    subscriber =
                        Subscriber.newBuilder(projectSubscriptionName, receiver).build();
                    subscriber.startAsync().awaitTerminated();
                } finally {
                    if (subscriber != null) {
                        subscriber.stopAsync().awaitTerminated();
                    }
                }

            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public MessageReceiver createMessageReceiver(EventBus eventBus) {
        MessageReceiver messageReceiver = ((PubsubMessage pubsubMessage, AckReplyConsumer consumer) -> {
            try {
                // handle incoming message, then ack/nack the received message
                //System.out.println("Id : " + pubsubMessage.getMessageId());
                String message = pubsubMessage.getData().toStringUtf8();
                //System.out.println("Data : " + Json.encodePrettily(message));

                eventBus.send(PubSubSubscriberImpl.EVENT_NAME, message);
                consumer.ack();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        return messageReceiver;
    }

    @Override
    public void stop() {
        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated();
        }
        if (submitFuture != null) {
            submitFuture.cancel(true);
        }
        if (eventProcessorExecutor != null) {
            eventProcessorExecutor.shutdown();
        }
    }
}
