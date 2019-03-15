# Firestore
<img align="right" src="https://github.com/masmovil/rx-gcloud-connectors/blob/master/firestore/firestoreLogo.png">
Java Reactive `Firestore` connector.

## Minimum Requirements

-   Java 11
-   Maven 3.5.3

## Maven useful commands

* build firestore project: ```mvn -pl .,firestore clean install```
* deploy firestore SDK into nexus: ```mvn -pl firestore deploy```

## How to use it

Add in your pom the following dependency:

```
        <dependency>
            <groupId>com.masmovil.gcloud</groupId>
            <artifactId>firestore</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
```

Create your own repository and extends `FirestoreTemplate`. You must provide your Key, model and return type as parameters.

for example:

Imagine that you have a garage, and you would like to manage your vehicles catalog.

```
public class CarsRepository extends FirestoreTemplate<String, CarModel, CarModel> {

	public CarsRepository(String keyPath, int threadPoolSize) {
		super(keyPath, Executors.newFixedThreadPool(10), CarModel::new);
	}
}
```

That it's all the code that you need in order to instantiate firestore connector. Where Keypath means the path of your Gcloud credentials and threadPoolSize the amount of threads that you would like to use in this repository tasks.

## Methods

### Insert

Insert create a Document with an auto-generate ID. Firestore auto-generated IDs do not provide any automatic
ordering. If you want to be able to order your documents by creation date, you should store a timestamp as a
field in the documents.

```
Single<K> insert(final E entity)
```

### Empty

Empty create a document for a given collection, and return an an auto-generate ID.
In some cases, it can be useful to create a document reference with an auto-generated ID,
then use the reference later through a upsert method.

```
Single<K> empty(final String collectionName)
```

### Upsert

If the document does not exist, it will be created. If the document does exist, its contents will be overwritten
with the newly provided data.

When you use upsert to create or update a document, you must specify an ID for the document. But sometimes there
isn't a meaningful ID for the document, and it's more convenient to let Cloud Firestore auto-generate an ID for
you. You can do this by calling empty.

```
Single<Boolean> upsert(final E entity, final K id)
```

### Upsert with options


If the document does not exist, it will be created. If the document does exist, its contents will be overwritten
with the newly provided data unless you specify that the data should be merged into the existing document.

example: ```upsert(SetOptions.merge(), vehicle, "adf12we60rx")```

If you're not sure whether the document exists, pass the option to merge the new data with any existing document
to avoid overwriting entire documents.

When you use upsert to create or update a document, you must specify an ID for the document. But sometimes there
isn't a meaningful ID for the document, and it's more convenient to let Cloud Firestore auto-generate an ID for
you. You can do this by calling insert.

```
Single<Boolean> upsert(final SetOptions options, final E entity, final K id)
```

### Get

Get will retrieve a Document by ID for a given collection name.

```
Single<R> get(final K id, final String collectionName)
```

### Query Builder

Query builder allow you to develop your own query with where statement. Use in combination with get in order to
develop complex inferences.

example:
```
var query = carsRepository.queryBuilder(CarModel.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");
```

```
Query queryBuilder(final String collectionName)
```

### Run Query

get will retrieve a List of Documents by a given query.

```
Single<List<R>> get(final Query query)
```

### Add Query Listener

AddQueryListener, You can listen to a document changes (create, update and delete).

An "eventsFlow" represent a flow of changes. Firstly you will get all the events that match with your query,
and then all the changes until you close your listener.

example:
```
listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()));

// finally after all you could close your connection
listener.getRegistration().remove();
```

```
EventListenerResponse<R> addQueryListener(final Query query, final Optional<EventListener<QuerySnapshot>> eventsHandler)
```


### Update

Update full document (overwrite).

```
Single<Boolean> update(final K id, final E entity)
```

### Update with preconditions

Update full document with a given precondition.

```
Single<Boolean> update(final Precondition precondition, final K id, final E entity)
```


### Partial update

To update some fields of a document without overwriting the entire document, use update.
If your document contains nested objects, you can use "dot notation" to reference nested fields within the
document when you call update.

```
Single<Boolean> update(final K id, final String collectionName, final HashMap<String, Object> fields)
```


### Delete

To delete a document, use the delete method. Deleting a document does not delete its subcollections!

```
Single<Boolean> delete(final K id, final String collectionName)
```

### Delete with preconditions

To delete a document with some given preconditions.

```
Single<Boolean> delete(final Precondition precondition, final K id, final String collectionName)
```


## How to contribute

Anyone could create a branch from master and create a PR.
This PR should have a description, and also unitTest. The solution must be reactive (callback hell, rxJava, verticles/actors, promises as completable futures, listeners... hooks) and documented.
