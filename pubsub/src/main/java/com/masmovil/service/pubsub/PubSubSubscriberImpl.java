package com.masmovil.service.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.reactivex.functions.Consumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.MessageConsumer;

/**
 * Represents a subscriber that is subscribed to a Pub/Sub channel. Creates a
 * {@link PubSubWorkerVerticle} that works in another thread, receiving Pub/Sub messages and
 * sending them towards this class via the Event Bus. Then, this class applies a Consumer to the
 * received messages.
 *
 * It implements RxJava's Disposable interface because it keeps receiving and processing messages
 * until it is externally disposed.
 * @param <T> The type of the messages that this subscriber will receive.
 */
public class PubSubSubscriberImpl<T> extends PubSubSubscriber<T> {

  public static final String EVENT_NAME = "pubsub.rejected.porta.event";
  private final PubSubWorkerVerticle pubSubWorkerVerticle;
  private final Vertx vertx;
  private final Class<T> tClass;
  private final Consumer<T> tConsumer;
  Logger log = LoggerFactory.getLogger(PubSubSubscriberImpl.class);

  private boolean disposed = false;


  PubSubSubscriberImpl(Vertx vertx, Class<T> tClass,
      ProjectSubscriptionName projectSubscriptionName, Consumer<T> tConsumer) {
    this.vertx = vertx;
    this.tClass = tClass;
    this.pubSubWorkerVerticle = new PubSubWorkerVerticle(projectSubscriptionName);
    this.tConsumer = tConsumer;
    init();
  }

  private void init() {
    MessageConsumer<Object> localConsumer = vertx.eventBus().localConsumer(EVENT_NAME, message -> {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        T extractedT = (tClass == String.class)? (T) message.body().toString():
                objectMapper.readValue(message.body().toString(), tClass);
        tConsumer.accept(extractedT);
      }
      catch (Exception e) {
        log.error("Error when deserializing element of class " + tClass.getName(), e);
      }
    });
    vertx.deployVerticle(pubSubWorkerVerticle);

  }

  @Override
  public void dispose() {
    try {
      vertx.rxUndeploy(pubSubWorkerVerticle.deploymentID()).blockingAwait();
    } catch (Exception e) {
      log.warn("Problem trying to stop pubsub, stopping anyway...", e);
    } finally {
      this.disposed = true;
    }
  }

  @Override
  public boolean isDisposed() {
    return disposed;
  }
}
