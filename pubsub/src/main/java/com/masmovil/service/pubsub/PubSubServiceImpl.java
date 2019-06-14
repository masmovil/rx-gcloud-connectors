package com.masmovil.service.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.masmovil.service.pubsub.exceptions.SubscriptionCreationException;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

public class PubSubServiceImpl implements PubSubService {

  private static final int TIMEOUT = 20;
  private final ProjectTopicName topicName;
  private final ProjectSubscriptionName projectSubscriptionName;

  private final Logger log = LoggerFactory.getLogger(PubSubServiceImpl.class);

  /**
   * The constructor is package-private, only accessible via the static methods
   * {@link PubSubService#fromEnv(String, String, String)},
   * {@link PubSubService#fromValues(String, String, String)} or
   * {@link PubSubServiceImpl#test()} (which is also package-private and intended only for testing)
   * @param projectId
   * @param topicId
   * @param subscriptionId
   */
  PubSubServiceImpl(String projectId, String topicId, String subscriptionId) {
    topicName = ProjectTopicName.of(projectId, topicId);
    projectSubscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
    createTopicIfNotExists(topicId).subscribe();

  }

  private Completable createTopicIfNotExists(String topicId) {
    return Completable.fromAction(() -> {
      try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
        log.info("Attempting to create pubsub topic " + topicId);
        topicAdminClient.createTopic(topicName);
        log.info("Created topic " + topicId);
      } catch(Exception e) {
          log.info("Could not create topic " + topicId + ". Already exists?");
          log.info(e.getMessage());
      }
    });
  }

  /**
   * Static creational method for testing (package-private)
   */
  static PubSubServiceImpl test() {
    return new PubSubServiceImpl(
        System.getenv("TEST_PUB_SUB_PROJECT_ID"),
        System.getenv("TEST_PUB_SUB_TOPIC_ID"),
            System.getenv("TEST_PUB_SUB_SUBSCRIPTION_ID"));
  }


  /**
   * Package-private method for ack-ing all previous messages, intended for setting up the
   * situation for a test
   */
  <T> Completable ackAllPrevious(T hay, Class<T> tClass, Vertx vertx) {
    List<PubSubSubscriberImpl<T>> subscribers = new ArrayList<>(1);
    return Completable.create(completableEmitter ->
        {
          subscribers.add(addSubscriber(t -> {
            //System.out.println("acking " + t);
            if (t.equals(hay)) {
              completableEmitter.onComplete();
            }
          }, tClass, vertx));
          publish(hay).subscribe();
        }).andThen(Completable.fromAction(() -> subscribers
        .forEach(PubSubSubscriberImpl::dispose)))
        .andThen(Completable.fromAction(subscribers::clear));
  }

  @Override
  public <T> Single<String> publish(T t) {
    return Single.create(singleEmitter -> {
      Publisher publisher = Publisher.newBuilder(topicName).build();
      ObjectMapper objectMapper = new ObjectMapper();
      byte[] jsonized = objectMapper.writeValueAsBytes(t);
      PubsubMessage message = PubsubMessage.newBuilder()
          .setData(ByteString.copyFrom(jsonized))
          .build();

      try {
        singleEmitter.onSuccess(publisher.publish(message)
            .get(TIMEOUT, TimeUnit.SECONDS));
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        log.warn("Element not emitted to pub/sub, timeout of " + TIMEOUT + " seconds reached", e);
        singleEmitter.onError(e);
      }
    });
  }

  @Override
  public <T> PubSubSubscriberImpl<T> addSubscriber(Consumer<T> consumer, Class<T> tClass, Vertx vertx) throws SubscriptionCreationException {
    try {
      PubSubSubscriberImpl<T> subscriber = new PubSubSubscriberImpl<>(vertx, tClass, projectSubscriptionName, consumer);
      return subscriber;
    } catch (Exception e) {
      throw new SubscriptionCreationException(e);
    }
  }
}
