# RxFirestore (Alpha)
<img align="right" src="https://github.com/masmovil/rx-gcloud-connectors/blob/master/firestore/firestoreLogo.png">

This SDK is a Java Reactive `Firestore` connector.

Google Cloud Firestore is a NoSQL document database built for automatic scaling, high performance, and ease of application development.
While the Cloud Firestore interface has many of the same features as traditional databases, as a NoSQL database it differs from them in the way it describes relationships between data objects.


## Index

- [Motivation](#motivation)
  - [Design approach Version 1.0.3](#design-approach)
- [Minimum Requirements](#minimum-requirements)
- [Maven useful commands](#maven-useful-commands)
- [How to use it](#How-to-use-it)
- [API methods](#API-methods)
  - [Insert](#insert)
  - [Empty](#empty)
  - [Upsert](#upsert)
  - [Get](#get)
  - [Query Builder](#query-builder)
  - [Run Query](#run-query)
  - [Update](#update)
  - [Delete](#delete)

## Motivation

Java Firestore SDK provided by Google it's implemented in a traditional way, through OS kernel threads. This is a very expensive way to give us a concurrency abstraction.
This implementation, fails to meet today’s requirement of concurrency, in particular threads cannot match the scale of the domain’s unit of concurrency. For example,
applications usually allow up to millions of transactions, users or sessions. However, the number of threads supported by the kernel is much less. Thus, a Thread for every user,
transaction, or session is often not feasible. To sum up, OS kernel threads is insufficient for meeting modern demands, and wasteful in computing resources that are particularly valuable in the cloud.

### Design approach

Our first solution to meet motivation requirements, is the use of asynchronous concurrent APIs. Common examples are CompletableFuture and RxJava.
Provided that such APIs don’t block the kernel thread, it gives an application a finer-grained concurrency construct on top of Java threads. However, at the end of the day we will have to use the blocking SDK defined by google,
so we have to think in a manner that allow us to mitigate use OS kernel threads in a per request/user way, and at the same time allow us to scale in order to meet today’s requirement of concurrency.

<img align="center" src="https://github.com/masmovil/rx-gcloud-connectors/blob/master/firestore/RxFirestoreSDK.png">

We've decided to handler all the user request through a reactive facade that will dispatch all the event to a in memory event bus (a buffer) in order to handler backpreasure and decouples I/O computation from the thread that invoked the operation.
This event bus will be consumed by a Vertx Actor (Worker Verticle), executing all of this commands in a synchronous or asynchronous way, running within a ThreadPool.

## Minimum Requirements

-   Java 11
-   Maven 3.5.3

## Maven useful commands

* build RxFirestore project: ```mvn -pl .,rxfirestore clean install -DskipTests```
* deploy RxFirestore SDK into nexus: ```mvn -pl rxfirestore deploy -DskipTests```

## How to use it

1. Add in your pom the following dependency:

```
 <dependency>
    <groupId>com.masmovil.gcloud</groupId>
    <artifactId>firestore</artifactId>
    <version>1.0.3-SNAPSHOT</version>
 </dependency>
```

2. Create your own repository and extends `RxFirestoreSDK`. You must provide your entity model as parameters.

for example:
Imagine that you have a garage, and you would like to manage your vehicles catalog.

```
public class VehicleRepository extends RxFirestoreSDK<Vehicle> {

	public VehicleRepository() {
		super(Vehicle::new);
	}

}
```

In case you are running this SDK under Vertx toolbox you could pass your vertx context as a parameter, and by this way will be reused.
```
public class VehicleRepository extends RxFirestoreSDK<Vehicle> {

	public VehicleRepository() {
		super(Vehicle::new, Vertx.currentContext().owner());
	}
}
```

3. Add `GCLOUD_KEY_PATH` environment variable to your project, pointing to your keyfile.json


## API methods

### Insert

Insert create a Document with an auto-generate ID. Firestore auto-generated IDs do not provide any automatic
ordering. If you want to be able to order your documents by creation date, you should store a timestamp as a
field in the documents.

```
Single<String> insert(final E entity)
```

### Empty

Empty create a document for a given collection, and return an an auto-generate ID.
In some cases, it can be useful to create a document reference with an auto-generated ID,
then use the reference later through a upsert method.

```
Single<String> empty(final String collectionName)
```

### Upsert

If the document does not exist, it will be created. If the document does exist, its contents will be overwritten
with the newly provided data.

When you use upsert to create or update a document, you must specify an ID for the document. But sometimes there
isn't a meaningful ID for the document, and it's more convenient to let Cloud Firestore auto-generate an ID for
you. You can do this by calling empty.

```
Single<Boolean> upsert(final String id, final String collectionName, final E entity)
```

### Get

Get will retrieve a Document by ID for a given collection name.

```
Single<E> get(final String id, final String collectionName)
```

### Query Builder

Query builder allow you to develop your own query with where statement. Use in combination with get in order to
develop complex inferences.

```
Single<Query> queryBuilder(final String collectionName)
```

example:
```
var query = carsRepository.queryBuilder(CarModel.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");
```


### Run Query

get will retrieve a List of Documents by a given query.

```
Single<List<E>> get(Query query)
```

### Run Query Listener

addQueryListener, You can listen to a document changes (create, update and delete).
```
EventListenerResponse<E> addQueryListener(final Query query, final Optional<EventListener<QuerySnapshot>> eventsHandler)
```

EventListenerResponse will allow you subscribe to a query `listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()));` and also close your connection `listener.getRegistration().remove();`

*Note:* this method is a *BLOCKING* operation, so a new thread will be created per listener.

### Update

Update full document (overwrite).

```
 Single<Boolean> update(final String id, final String collectionName, final E entity)
```


### Delete

To delete a document, use the delete method. Deleting a document does not delete its subcollections!

```
Single<Boolean> delete(final String id, final String collectionName)
```
