# pubsub

Java Reactive `pubsub` connector.

Pub/Sub is a google system in which a Publisher publishes contents
to a topic, this topic may generate several subscriptions, and 
subscribers may subscribe to these subscriptions.

## Minimum Requirements

-   Java 11
-   Maven 3.5.3

## Motivation

Java Pub/Sub SDK provided by Google is implemented in a traditional way, through OS kernel threads. This is a very expensive way to give us a concurrency abstraction.
This implementation, fails to meet today’s requirement of concurrency, in particular threads cannot match the scale of the domain’s unit of concurrency. For example,
applications usually allow up to millions of transactions, users or sessions. However, the number of threads supported by the kernel is much less. Thus, a Thread for every user,
transaction, or session is often not feasible. To sum up, OS kernel threads is insufficient for meeting modern demands, and wasteful in computing resources that are particularly valuable in the cloud.

### Design approach

Our main concerns here have been reactivity and, particularly, not 
blocking main threads. This library is intended to be run with Vert.X,
so if the main thread is blocked, the performance will be ruined.

This is particularly important when subscribing to events, because
subscribing to a subscription means that the subscriber has to be kept 
waiting, which would block Vert.x's event loop. In order to circumvent
this problem, we decided that the subscriber would run in another 
thread, in a separate verticle, and when it receives a message, it will
send it back to the main thread via the event bus.

## How to use it


1. Add in your pom the following dependency:

```
 <dependency>
    <groupId>com.masmovil.gcloud</groupId>
    <artifactId>pubsub</artifactId>
    <version>1.0.0-SNAPSHOT</version>
 </dependency>
```

2. Generate an object of the class 
[PubSubService](./src/main/java/com/masmovil/service/pubsub/PubSubService.java).
In order to do this, you need to provide the information for Google
Cloud to know which topic/subscription you're trying to access, which
consists of:
   * Project Id
   * Topic Id
   * Subscription Id

There are two ways of providing these values:
   * providing the values directly via `PubSubService.fromValues()`
   * providing the env variables that contain them via
   `PubSubService.fromEnv()`
   
3. Operate with the created
[PubSubService](./src/main/java/com/masmovil/service/pubsub/PubSubService.java)
object. The object will only allow to perform 2 operations: publishing
a message and subscribing to a message queue

### Publishing messages

Publishing is pretty simple, just call the `publish()` method on your
`PubSubService` instance, and the provided object will be serialized
to a String, then to a byte array and finally emitted to the queue.

This method returns a `Single<String>` that, when the message gets 
published, resolves to the message's publication Id.

### Subscribing to a subscription

The method `addSubscriber()` generates a worker verticle that receives
messages and sends them back to the main thread via the event bus, 
where they are processed by the `Consumer<T>` provided to the method.

This method returns an instance of the abstract class
[PubSubSubscriber](./src/main/java/com/masmovil/service/pubsub/PubSubSubscriber.java),
which implements RxJava's `Disposable` interface (although by now
calling `dispose()` produces an infinite loop). In the future, this 
class may also extend RxJava's `Flowable<T>` class.

## Running the tests

In order to run the tests, you need to provide these environment
variables:

* TEST_PUB_SUB_PROJECT_ID: your project Id for tests
* TEST_PUB_SUB_TOPIC_ID: your topic Id for tests
* TEST_PUB_SUB_SUBSCRIPTION_ID: your subscription Id for tests
* GOOGLE_APPLICATION_CREDENTIALS (only when not running in a gcloud
 environment, whether or not you're in test mode): points to your
 json key