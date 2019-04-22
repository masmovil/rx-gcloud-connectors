package com.masmovil.service.pubsub;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.reactivex.core.Vertx;

public interface PubSubService {

  /**
   * Publishes a provided message to the Pub/Sub channel
   * @param t The message to be sent to the Pub/Sub channel
   * @param <T> The class of the message to be sent
   * @return A Single that resolves to the id of the message when it has been published to the
   * channel
   */
  <T> Single<String> publish(T t);

  /**
   * Adds a subscriber that receives messages from a Pub/Sub channel and processes them
   * @param consumer The function to be applied to the incoming messages
   * @param tClass The class of the incoming messages
   * @param vertx A reactive instance of vertx
   * @param <T> The type of the incoming messages
   * @return A disposable reference to the element which is processing the incoming elements
   */
  <T> PubSubSubscriber<T> addSubscriber(Consumer<T> consumer, Class<T> tClass, Vertx vertx);

  static PubSubService fromValues(String projectId, String topicId, String subscriptionId) {
    return new PubSubServiceImpl(projectId, topicId, subscriptionId);
  }

  static PubSubService fromEnv(String projectId, String topicId, String subscriptionId) {
    return new PubSubServiceImpl(System.getenv(projectId), System.getenv(topicId),
        System.getenv(subscriptionId));
  }

}
