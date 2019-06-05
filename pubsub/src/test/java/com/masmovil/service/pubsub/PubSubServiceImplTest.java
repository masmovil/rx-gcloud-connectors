package com.masmovil.service.pubsub;

import com.masmovil.service.pubsub.exceptions.SubscriptionCreationException;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PubSubServiceImplTest {

  PubSubServiceImpl pubsub = PubSubServiceImpl.test();
  List<Integer> expected = new ArrayList<>(100);
  List<Integer> actual = new ArrayList<>(100);
  ConcurrentHashMap<Integer, Integer> concurrentHashMap = new ConcurrentHashMap<>(10);
  Vertx vertx;

  @BeforeEach
  void setUp() {
    VertxOptions options = new VertxOptions();
    options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
    vertx = Vertx.newInstance(io.vertx.core.Vertx.vertx(options));
//    pubsub.ackAllPrevious(new Random().nextInt(2000) + 1000, Integer.class, vertx)
//    .blockingAwait();
  }

  @Test
  void publish() throws Exception {

    //Creating subscribers
    IntStream.range(0,10)
        .boxed()
        .map(this::createConsumer)
        .forEach(integerConsumer ->
            {
              try {
                pubsub.addSubscriber(integerConsumer, Integer.class, vertx);
              } catch (SubscriptionCreationException e) {
                e.printStackTrace();
                fail(e);
              }
            }
        );

    System.out.println("publishing");

    IntStream.range(0,25)
        .boxed()
        .peek(expected::add)
        .peek(System.out::println)
        .map(integer -> pubsub.publish(integer))
        .forEach(Single::blockingGet);

    while (concurrentHashMap.values().stream().mapToInt(i -> i).sum() < 5) {
      Thread.sleep(100);
    }

    System.out.println("published all");
    assertTrue(expected.containsAll(actual));
    assertTrue(actual.containsAll(expected));
  }

  @Test
  void publish1() throws Exception {

    PubSubService pubSubService = PubSubService.fromEnv(
        "TEST_PUB_SUB_PROJECT_ID",
        "TEST_PUB_SUB_TOPIC_ID",
        "TEST_PUB_SUB_SUBSCRIPTION_ID");

    pubSubService.addSubscriber(createConsumer(0), Integer.class, vertx);



  }

  private Consumer<Integer> createConsumer(int i) {
    return integer -> {
      System.out.println("received in subscriber " + i + " the emitted value " + integer);
      actual.add(integer);
      addToMap(integer);
    };
  }

  private synchronized void addToMap(int emittedValue) {
    if (concurrentHashMap.containsKey(emittedValue)) {
      System.out.println("value " + emittedValue + " already emitted.");
      concurrentHashMap.put(emittedValue, concurrentHashMap.get(emittedValue) + 1);
    } else {
      concurrentHashMap.put(emittedValue, 1);
    }
  }
}