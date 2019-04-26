package com.masmovil.rxfirestore;

import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_DELETE;
import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_EMPTY;
import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_GET;
import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_INSERT;
import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_QUERY;
import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_QUERY_BUILDER;
import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_UPDATE;
import static com.masmovil.rxfirestore.FirestoreTemplate.TOPIC_UPSERT;

import io.reactivex.subjects.SingleSubject;
import io.vertx.core.Future;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.apache.commons.lang3.SerializationUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.QuerySnapshot;

import io.reactivex.Single;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;

/**
 * RxFirestoreSDK is a data access object implementation for Google Firestore database.
 * In order to use it, your repositories must extends this class, where E means the entity type that you want to manage in your collection
 * <p>
 * This implementation will give you commons methods in order to work with firestore, but you could overwrite them or
 * implements your own methods in your repository.
 *
 * NOTE: you must set GCLOUD_KEY_PATH environment variable pointing to you keyfile.json.
 * Additionally you could set DB_THREAD_POOL_SIZE environment variable in order to set the amount of thread that you would like to have in order to manage all firestore connections.
 * By default, DB_THREAD_POOL_SIZE will be set to the number of cores that you have X 2.
 */
public class RxFirestoreSDK<E extends Entity> {

	private final static long SEND_TIMEOUT_MS = 59000;
	private final Supplier<? extends Entity> supplier;
	private final BlockingFirestoreTemplate blockingFirestoreTemplate;

	public RxFirestoreSDK(Supplier<? extends Entity> entityConstructor){
		supplier = Objects.requireNonNull(entityConstructor);
		FirestoreTemplateFactory.INSTANCE.init();
		blockingFirestoreTemplate = new BlockingFirestoreTemplate(supplier, FirestoreTemplateFactory.INSTANCE.getVertx());
	}

	public RxFirestoreSDK(Supplier<? extends Entity> entityConstructor, Vertx vertx){
		supplier = Objects.requireNonNull(entityConstructor);
		FirestoreTemplateFactory.INSTANCE.init(vertx);
		SingleSubject<Vertx> vertxSubject = SingleSubject.create();
		vertxSubject.onSuccess(vertx);
		blockingFirestoreTemplate = new BlockingFirestoreTemplate(supplier, vertxSubject);
	}

	/**
	 * Insert create a Document with an auto-generate ID. Firestore auto-generated IDs do not provide any automatic
	 * ordering. If you want to be able to order your documents by creation date, you should store a timestamp as a
	 * field in the documents.
	 *
	 * @param entity
	 * @return Single document key ID.
	 */
	public Single<String> insert(final E entity){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);
		deliveryOpt.addHeader("_collectionName", entity.getCollectionName());

		return eventBus.<String>rxSend(TOPIC_INSERT, Json.encode(entity.toMap()), deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> message);
	}

	/**
	 * Empty create a document for a given collection, and return an an auto-generate ID.
	 * In some cases, it can be useful to create a document reference with an auto-generated ID,
	 * then use the reference later through a upsert method.
	 *
	 * @param collectionName against which you want to make the query.
	 * @return Single document key ID.
	 */
	public Single<String> empty(final String collectionName){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);
		deliveryOpt.addHeader("_collectionName", collectionName);

		return eventBus.<String>rxSend(TOPIC_EMPTY, "", deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> message);
	}

	/**
	 * queryBuilder allow you to develop your own query with where statement. Use in combination with get in order to
	 * develop complex inferences.
	 *
	 * @param collectionName against which you want to make the query.
	 * @return Query
	 * <p>
	 * example:
	 * <p>
	 * var query = carsRepository.queryBuilder(CarModel.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");
	 */
	public Single<Query> queryBuilder(final String collectionName){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);
		deliveryOpt.addHeader("_collectionName", collectionName);

		return eventBus.<byte[]>rxSend(TOPIC_QUERY_BUILDER, "", deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> SerializationUtils.deserialize(message));
	}

	/**
	 * get will retrieve a List of Documents by a given query.
	 *
	 * @param query .Build your query with queryBuilder method.
	 * @return a single list of documents that match query criteria.
	 */
	public Single<List<E>> get(Query query){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);

		return eventBus.<String>rxSend(TOPIC_QUERY, SerializationUtils.serialize(query), deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> {
					List<E> result = new ArrayList<>();
					var data = Json.decodeValue(message,new TypeReference<List<HashMap>>(){});
					data.stream().forEach(elem -> result.add((E)supplier.get().fromJsonAsMap(elem)));
					return result;
				});
	}

	/**
	 * If the document does not exist, it will be created. If the document does exist, its contents will be overwritten
	 * with the newly provided data.
	 * <p>
	 * When you use upsert to create or update a document, you must specify an ID for the document. But sometimes there
	 * isn't a meaningful ID for the document, and it's more convenient to let Cloud Firestore auto-generate an ID for
	 * you. You can do this by calling empty.
	 *
	 * @param entity
	 * @param id
	 * @param collectionName against which you want to upsert.
	 * @return Single boolean.
	 */
	public Single<Boolean> upsert(final String id, final String collectionName, final E entity){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);
		deliveryOpt.addHeader("_collectionName", collectionName);
		deliveryOpt.addHeader("_id", id);

		return eventBus.<Boolean>rxSend(TOPIC_UPSERT, entity.toMap(), deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> message);
	}

	/**
	 * get will retrieve a Document by ID for a given collection name.
	 *
	 * @param collectionName against which you want to make the query.
	 * @param id             , document ID that you would like to retrieve
	 * @return Single document
	 */
	public Single<E> get(final String id, final String collectionName){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);
		deliveryOpt.addHeader("_collectionName", collectionName);
		deliveryOpt.addHeader("_id", id);

		return eventBus.<String>rxSend(TOPIC_GET, "", deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> {
					var data = Json.decodeValue(message,HashMap.class);
					return (E)supplier.get().fromJsonAsMap(data);
				});
	}

	/**
	 * Update full document (overwrite).
	 *
	 * @param id
	 * @param entity
	 * @param collectionName against which you want to make the query.
	 * @return Single boolean. True means updated.
	 */
	public Single<Boolean> update(final String id, final String collectionName, final E entity){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);
		deliveryOpt.addHeader("_collectionName", collectionName);
		deliveryOpt.addHeader("_id", id);

		return eventBus.<String>rxSend(TOPIC_UPDATE, Json.encode(entity.toMap()), deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> Boolean.valueOf(message));
	}


	/**
	 * To delete a document, use the delete method. Deleting a document does not delete its subcollections!
	 *
	 * @param id
	 * @param collectionName
	 * @return Single boolean
	 */
	public Single<Boolean> delete(final String id, final String collectionName){
		var eventBus = FirestoreTemplateFactory.INSTANCE.getEventBus();
		var deliveryOpt = new DeliveryOptions();
		deliveryOpt.setSendTimeout(SEND_TIMEOUT_MS);
		deliveryOpt.addHeader("_collectionName", collectionName);
		deliveryOpt.addHeader("_id", id);

		return eventBus.<String>rxSend(TOPIC_DELETE, "", deliveryOpt)
				.doOnError(error -> System.err.println("The error message is: " + error.getMessage()))
				.map(Message::body)
				.map(message -> Boolean.valueOf(message));
	}

	/**
	 * addQueryListener, You can listen to a document changes (create, update and delete).
	 *
	 * @param query         to subscribe. Build your query with queryBuilder method.
	 * @param eventsHandler will handler document changes. By default we provide an eventHandler that will give you a Flowable with all the document changes.
	 * @return EventListenerResponse, contains two object.
	 * "registration" will allow you to close the event flow and eventsFlow that will give you an events Flowable
	 * <p>
	 * example:
	 * <p>
	 * listener.getRegistration().remove();
	 * <p>
	 * "eventsFlow" represent a flow of changes. Firstly you will get all the events that match with your query,
	 * and then all the changes until you close your listener.
	 * <p>
	 * example:
	 * <p>
	 * listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()));
	 */

	public EventListenerResponse<E> addQueryListener(final Query query, final Optional<EventListener<QuerySnapshot>> eventsHandler)
			throws InterruptedException, ExecutionException, TimeoutException {
		return blockingFirestoreTemplate.addQueryListener(query, eventsHandler);
	}

}

