package com.masmovil.firestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Precondition;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.ImmutableList;

import io.reactivex.Single;

/**
 * FirestoreTemplate is a data access object implementation for Google Firestore database.
 * In order to use it, your repositories must extends this abstract class, where K means
 * the type of your Key (should be a String), E means the entity type that you want to manage in your collection
 * and R means the return type (could be the same as your entity type).
 *
 * This implementation will give you commons methods in order to work with firestore, but you could overwrite them or
 * implements your own methods in your repository.
 * */

public abstract class FirestoreTemplate<K extends String,E extends Entity, R extends Entity> {

	public static final List<String> SCOPES = ImmutableList.of("https://www.googleapis.com/auth/datastore");
	private final Firestore firestore;

	private final ExecutorService rxJavaExecutor;
	private final Supplier<? extends R> supplier;
	private final SingleEntityIdCallbackHandler<K> singleEntityId;
	private final SingleEntityCallbackHandler<R> entityCallbackHandler;
	private final UpdateCallbackHandler updateCallbackHandler;
	private final DeleteCallbackHandler deleteCallbackHandler;
	private final QueryCallbackHandler queryCallbackHandler;
	private final PartialUpdateCallbackHandler partialUpdateCallbackHandler;

	/**
	 * FirestoreTemplate constructor. You must call this method in your repository constructor.
	 *
	 * example:
	 *
	 * public class CarsRepository extends FirestoreTemplate<String, CarModel, CarModel> {
	 *
	 * 	public CarsRepository(String keyPath, int threadPoolSize) {
	 * 		super(keyPath, threadPoolSize, CarModel::new);
	 *        }
	 * }
	 *
	 * */

	public FirestoreTemplate(final String keyPath, final ExecutorService executor, Supplier<? extends R> entityConstructor)  {

		try {
			var firestoreOptions = FirestoreOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(new FileInputStream(new File(keyPath))).createScoped(SCOPES)).build();
			firestore = firestoreOptions.getService();

			rxJavaExecutor = executor; //Executors.newFixedThreadPool(threadPoolSize);
			supplier = Objects.requireNonNull(entityConstructor);
			singleEntityId = new SingleEntityIdCallbackHandler();
			entityCallbackHandler = new SingleEntityCallbackHandler(supplier.get());
			updateCallbackHandler = new UpdateCallbackHandler();
			deleteCallbackHandler = new DeleteCallbackHandler();
			queryCallbackHandler = new QueryCallbackHandler(supplier.get());
			partialUpdateCallbackHandler = new PartialUpdateCallbackHandler();

		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public FirestoreTemplate(final Firestore fs, final ExecutorService executor, Supplier<? extends R> entityConstructor)  {

			firestore = fs;
			rxJavaExecutor = executor; //Executors.newFixedThreadPool(threadPoolSize);
			supplier = Objects.requireNonNull(entityConstructor);
			singleEntityId = new SingleEntityIdCallbackHandler();
			entityCallbackHandler = new SingleEntityCallbackHandler(supplier.get());
			updateCallbackHandler = new UpdateCallbackHandler();
			deleteCallbackHandler = new DeleteCallbackHandler();
			queryCallbackHandler = new QueryCallbackHandler(supplier.get());
			partialUpdateCallbackHandler = new PartialUpdateCallbackHandler();
	}

	/**
	 *
	 * Insert create a Document with an auto-generate ID. Firestore auto-generated IDs do not provide any automatic
	 * ordering. If you want to be able to order your documents by creation date, you should store a timestamp as a
	 * field in the documents.
	 *
	 * @param entity
	 * @return Single document key ID.
	 * */

	public Single<K> insert(final E entity){

		ApiFuture<DocumentReference> response = firestore.collection(entity.getCollectionName()).add(entity.toMap());
		ApiFutures.addCallback(response, singleEntityId, rxJavaExecutor);

		return singleEntityId.getEntityID();
	}

	/**
	 *
	 * Empty create a document for a given collection, and return an an auto-generate ID.
	 * In some cases, it can be useful to create a document reference with an auto-generated ID,
	 * then use the reference later through a upsert method.
	 *
	 * @param collectionName against which you want to make the query.
	 * @return Single document key ID.
	 * */

	public Single<K> empty(final String collectionName){

		DocumentReference response = firestore.collection(collectionName).document();
		return Single.just((K)response.getId());
	}

	/**
	 *
	 * If the document does not exist, it will be created. If the document does exist, its contents will be overwritten
	 * with the newly provided data.
	 *
	 * When you use upsert to create or update a document, you must specify an ID for the document. But sometimes there
	 * isn't a meaningful ID for the document, and it's more convenient to let Cloud Firestore auto-generate an ID for
	 * you. You can do this by calling empty.
	 *
	 * @param entity
	 * @param id
	 * @return Single boolean.
	 * */

	public Single<Boolean> upsert(final E entity, final K id){

		ApiFuture<WriteResult> response = firestore.collection(entity.getCollectionName()).document(id).set(entity.toMap());
		ApiFutures.addCallback(response, updateCallbackHandler, rxJavaExecutor);

		return updateCallbackHandler.isUpdated();
	}

	/**
	 *
	 * If the document does not exist, it will be created. If the document does exist, its contents will be overwritten
	 * with the newly provided data unless you specify that the data should be merged into the existing document.
	 *
	 * example: upsert(SetOptions.merge(), vehicle, "adf12we60rx")
	 *
	 * If you're not sure whether the document exists, pass the option to merge the new data with any existing document
	 * to avoid overwriting entire documents.
	 *
	 * When you use upsert to create or update a document, you must specify an ID for the document. But sometimes there
	 * isn't a meaningful ID for the document, and it's more convenient to let Cloud Firestore auto-generate an ID for
	 * you. You can do this by calling insert.
	 *
	 * @param options
	 * @param entity
	 * @param id
	 * @return Single boolean.
	 * */

	public Single<Boolean> upsert(final SetOptions options, final E entity, final K id){

		ApiFuture<WriteResult> response = firestore.collection(entity.getCollectionName()).document(id).set(entity.toMap(), options);
		ApiFutures.addCallback(response, updateCallbackHandler, rxJavaExecutor);

		return updateCallbackHandler.isUpdated();
	}


	/**
	 * get will retrieve a Document by ID for a given collection name.
	 *
	 * @param collectionName against which you want to make the query.
	 * @param id , document ID that you would like to retrieve
	 * @return Single document
	 * */

	public Single<R> get(final K id, final String collectionName){

		ApiFuture<DocumentSnapshot> response = firestore.collection(collectionName).document(id).get();
		ApiFutures.addCallback(response, entityCallbackHandler, rxJavaExecutor);

		return entityCallbackHandler.getEntity();
	}

	/**
	 * queryBuilder allow you to develop your own query with where statement. Use in combination with get in order to
	 * develop complex inferences.
	 *
	 * @param collectionName against which you want to make the query.
	 * @return Query
	 *
	 * example:
	 *
	 * 	var query = carsRepository.queryBuilder(CarModel.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");
	 * */

	public Query queryBuilder(final String collectionName) {
		return firestore.collection(collectionName);
	}

	/**
	 * addQueryListener, You can listen to a document changes (create, update and delete).
	 *
	 * @param query to subscribe. Build your query with queryBuilder method.
	 * @param eventsHandler will handler document changes. By default we provide an eventHandler that will give you a Flowable with all the document changes.
	 * @return EventListenerResponse, contains two object.
	 * "registration" will allow you to close the event flow
	 *
	 * example:
	 *
	 * 	listener.getRegistration().remove();
	 *
	 * "eventsFlow" represent a flow of changes. Firstly you will get all the events that match with your query,
	 *  and then all the changes until you close your listener.
	 *
	 *  example:
	 *
	 *   listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()));
	 * */

	public EventListenerResponse<R> addQueryListener(final Query query, final Optional<EventListener<QuerySnapshot>> eventsHandler){
		var defaultHandler = new DefaultEventListener<R>(supplier.get());
		var listener = query.addSnapshotListener(rxJavaExecutor, eventsHandler.orElse(defaultHandler));
		return new EventListenerResponse<R>(defaultHandler.getSource(),listener);
	}

	/**
	 * get will retrieve a List of Documents by a given query.
	 *
	 * @param query .Build your query with queryBuilder method.
	 * @return a single list of documents that match query criteria.
	 * */

	public Single<List<R>> get(final Query query){

		ApiFuture<QuerySnapshot> response = query.get();
		ApiFutures.addCallback(response, queryCallbackHandler, rxJavaExecutor);

		return queryCallbackHandler.getEntities();
	}

	/**
	 * Update full document (overwrite).
	 *
	 * @param id
	 * @param entity
	 * @return Single boolean. True means updated.
	 * */

	public Single<Boolean> update(final K id, final E entity){

		ApiFuture<WriteResult> response = firestore.collection(entity.getCollectionName()).document(id).update(entity.toMap());
		ApiFutures.addCallback(response, updateCallbackHandler, rxJavaExecutor);

		return updateCallbackHandler.isUpdated();
	}

	/**
	 * Update full document with a given precondition.
	 *
	 * @param precondition
	 * @param id
	 * @param entity
	 * @return Single boolean. True means updated.
	 * */

	public Single<Boolean> update(final Precondition precondition, final K id, final E entity){

		ApiFuture<WriteResult> response = firestore.collection(entity.getCollectionName()).document(id).update(entity.toMap(), precondition);
		ApiFutures.addCallback(response, updateCallbackHandler, rxJavaExecutor);

		return updateCallbackHandler.isUpdated();
	}

	/**
	 * To update some fields of a document without overwriting the entire document, use update.
	 * If your document contains nested objects, you can use "dot notation" to reference nested fields within the
	 * document when you call update.
	 *
	 * @param id
	 * @param collectionName
	 * @param fields
	 * @return Single boolean
	 * */

	public Single<Boolean> update(final K id, final String collectionName, final HashMap<String, Object> fields){

		HashMap<String, Single<Boolean>> result = new HashMap();
		ApiFuture<HashMap<String, Single<Boolean>>> transactionResponse = firestore.runTransaction(transaction -> {
			for (Map.Entry<String, Object> entry : fields.entrySet()){
				DocumentReference docRef = firestore.collection(collectionName).document(id);

				ApiFuture<WriteResult> response = docRef.update(entry.getKey(), entry.getValue());
				ApiFutures.addCallback(response, updateCallbackHandler, rxJavaExecutor);
				result.put(entry.getKey(), updateCallbackHandler.isUpdated());
			}
			return result;
		});

		ApiFutures.addCallback(transactionResponse, partialUpdateCallbackHandler, rxJavaExecutor);
		return partialUpdateCallbackHandler.isUpdated();
	}

	/**
	 * To delete a document, use the delete method. Deleting a document does not delete its subcollections!
	 *
	 * @param id
	 * @param collectionName
	 * @return Single boolean
	 * */

	public Single<Boolean> delete(final K id, final String collectionName){

		ApiFuture<WriteResult> response = firestore.collection(collectionName).document(id).delete();
		ApiFutures.addCallback(response, deleteCallbackHandler, rxJavaExecutor);

		return deleteCallbackHandler.isDeleted();
	}

	/**
	 * To delete a document with some given preconditions.
	 *
	 * @param precondition
	 * @param id
	 * @param collectionName
	 * @return Single boolean
	 * */

	public Single<Boolean> delete(final Precondition precondition, final K id, final String collectionName){

		ApiFuture<WriteResult> response = firestore.collection(collectionName).document(id).delete(precondition);
		ApiFutures.addCallback(response, deleteCallbackHandler, rxJavaExecutor);

		return deleteCallbackHandler.isDeleted();
	}

}
