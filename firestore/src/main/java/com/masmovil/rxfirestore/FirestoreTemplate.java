package com.masmovil.rxfirestore;

import static io.vertx.ext.sync.Sync.streamAdaptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.SerializationUtils;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.ImmutableList;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;


public class FirestoreTemplate extends SyncVerticle {


	public static final List<String> SCOPES = ImmutableList.of("https://www.googleapis.com/auth/datastore");
    public static final String TOPIC_INSERT = "FIRESTORE_INSERT";
	public static final String TOPIC_EMPTY = "FIRESTORE_EMPTY";
	public static final String TOPIC_UPSERT = "FIRESTORE_UPSERT";
	public static final String TOPIC_GET = "FIRESTORE_GET";
	public static final String TOPIC_UPDATE = "FIRESTORE_UPDATE";
	public static final String TOPIC_DELETE = "FIRESTORE_DELETE";
	public static final String TOPIC_QUERY = "FIRESTORE_QUERY";
	public static final String TOPIC_QUERY_BUILDER = "FIRESTORE_QUERY_BUILDER";

	private final FirestoreOptions.Builder firestoreBuilder;

	public FirestoreTemplate() {

		try {

			String keyPath = Optional.ofNullable(System.getenv("GCLOUD_KEY_PATH")).orElseThrow(
					() -> new IllegalArgumentException("GCLOUD_KEY_PATH is not set in the environment"));

			firestoreBuilder = FirestoreOptions.newBuilder().setCredentials(
					GoogleCredentials.fromStream(new FileInputStream(new File(keyPath))).createScoped(SCOPES));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	@Suspendable
	public void start() {
		EventBus firestoreEventBus = vertx.eventBus();

		HandlerReceiverAdaptor<Message<Object>> insertConsumer = streamAdaptor();

		firestoreEventBus.localConsumer(TOPIC_INSERT).handler( msg-> handlerInsert(msg));
				/*message -> {
			String _collectionName = message.headers().get("_collectionName");
			HashMap entity = Json.decodeValue((String) message.body(), HashMap.class);
			SingleEntityIdCallbackHandler singleEntityId = new SingleEntityIdCallbackHandler<String>();
			//String id;
			System.out.println(Thread.activeCount());

			vertx.<String>executeBlocking(future -> {
				       System.out.println(Thread.activeCount());
						try (Firestore db = firestoreBuilder.build().getService()) {

							ApiFuture<DocumentReference> response = db.collection(_collectionName).add(entity);
							future.complete(response.get().getId());
							//ApiFutures.addCallback(response, singleEntityId, Runnable::run);
							//id =  (String)singleEntityId.getEntityID().blockingGet();
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage());
						}
					}, res -> {
				            message.reply(res.result());
					});*/
			//System.out.println(Thread.activeCount());
			//message.reply(id);
			//handlerInsert(msg)
		//});

		HandlerReceiverAdaptor<Message<Object>> emptyConsumer = streamAdaptor();
		firestoreEventBus.localConsumer(TOPIC_EMPTY).handler(msg -> handlerEmpty(msg));

		HandlerReceiverAdaptor<Message<Object>> upsertConsumer = streamAdaptor();
		firestoreEventBus.localConsumer(TOPIC_UPSERT).handler(msg -> handlerUpsert(msg));

		HandlerReceiverAdaptor<Message<Object>> getConsumer = streamAdaptor();
		firestoreEventBus.localConsumer(TOPIC_GET).handler(msg -> handlerGet(msg));

		HandlerReceiverAdaptor<Message<Object>> updateConsumer = streamAdaptor();
		firestoreEventBus.localConsumer(TOPIC_UPDATE).handler(msg -> handlerUpdate(msg));

		HandlerReceiverAdaptor<Message<Object>> deleteConsumer = streamAdaptor();
		firestoreEventBus.localConsumer(TOPIC_DELETE).handler(msg -> handlerDelete(msg));

		HandlerReceiverAdaptor<Message<byte[]>> queryBuilderConsumer = streamAdaptor();
		firestoreEventBus.<byte[]>localConsumer(TOPIC_QUERY_BUILDER).handler(msg -> handlerQueryBuilder(msg));

		HandlerReceiverAdaptor<Message<byte[]>> queryConsumer = streamAdaptor();
		firestoreEventBus.<byte[]>localConsumer(TOPIC_QUERY).handler(msg -> handlerQuery(msg));

	}

	public String insert(final HashMap<String, Object> entity, final String collectionName) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			SingleEntityIdCallbackHandler singleEntityId = new SingleEntityIdCallbackHandler<String>();
			ApiFuture<DocumentReference> response = db.collection(collectionName).add(entity);
			//DocumentReference df = response.get();
			ApiFutures.addCallback(response, singleEntityId, Runnable::run);
			return (String)singleEntityId.getEntityID().blockingGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}


	public String empty(final String collectionName) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			DocumentReference response = db.collection(collectionName).document();
			return response.getId();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}


	public Boolean upsert(final HashMap<String, Object> entity, final String id, final String collectionName) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			UpdateCallbackHandler updateCallbackHandler = new UpdateCallbackHandler();
			ApiFuture<WriteResult> response = db.collection(collectionName).document(id).set(entity);
			ApiFutures.addCallback(response, updateCallbackHandler, Runnable::run);
			return updateCallbackHandler.isUpdated().blockingGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}



	public Map<String, Object> get(final String id, final String collectionName) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			SingleEntityCallbackHandler entityCallbackHandler = new SingleEntityCallbackHandler();
			ApiFuture<DocumentSnapshot> response = db.collection(collectionName).document(id).get();
			ApiFutures.addCallback(response, entityCallbackHandler, Runnable::run);
			return entityCallbackHandler.getEntity().blockingGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}


	public Query queryBuilder(final String collectionName) {
			return new Query(collectionName);
	}
/*
	/**
	 * addQueryListener, You can listen to a document changes (create, update and delete).
	 *
	 * @param query         to subscribe. Build your query with queryBuilder method.
	 * @param eventsHandler will handler document changes. By default we provide an eventHandler that will give you a Flowable with all the document changes.
	 * @return EventListenerResponse, contains two object.
	 * "registration" will allow you to close the event flow
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
/*
	public EventListenerResponse<R> addQueryListener(final Query query,
			final Optional<EventListener<QuerySnapshot>> eventsHandler) {
		var defaultHandler = new DefaultEventListener<R>(supplier.get());
		var listener = query.addSnapshotListener(rxJavaExecutor, eventsHandler.orElse(defaultHandler));
		return new EventListenerResponse<R>(defaultHandler.getSource(), listener);
	}
*/


	public List<Map<String, Object>> get(final Query query) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			CollectionReference q = db.collection(query.getCollectionName());
			com.google.cloud.firestore.Query queryBuilder;

			if(query.isLimitSet()){
				queryBuilder = q.limit(query.getLimit());
			}else{
				queryBuilder = q.limit(20);
			}

			if(query.isOffsetSet()){
				queryBuilder.offset(query.getOffset());
			}

			HashMap<String,Object> equalTo = query.getEqualTo();
			Iterator equalToIt = equalTo.entrySet().iterator();
			while (equalToIt.hasNext()) {
				Map.Entry pair = (Map.Entry) equalToIt.next();
				queryBuilder = queryBuilder.whereEqualTo((String)pair.getKey(), pair.getValue());
			}

			HashMap<String,Object> arrayContains = query.getArrayContains();
			Iterator arrayContainsIt = arrayContains.entrySet().iterator();
			while (arrayContainsIt.hasNext()) {
				Map.Entry pair = (Map.Entry) arrayContainsIt.next();
				queryBuilder = queryBuilder.whereEqualTo((String)pair.getKey(), pair.getValue());
			}

			QueryCallbackHandler queryCallbackHandler = new QueryCallbackHandler();
			ApiFuture<QuerySnapshot> response = queryBuilder.get();
			ApiFutures.addCallback(response, queryCallbackHandler, Runnable::run);

			return queryCallbackHandler.getEntities().blockingGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}


	public Boolean update(final String id, final String collectionName, final HashMap<String, Object> entity) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			UpdateCallbackHandler updateCallbackHandler = new UpdateCallbackHandler();
			ApiFuture<WriteResult> response = db.collection(collectionName).document(id).update(entity);
			ApiFutures.addCallback(response, updateCallbackHandler, Runnable::run);
			return updateCallbackHandler.isUpdated().blockingGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}


/*
	public Single<Boolean> update(final Precondition precondition, final K id, final E entity) {
		try (Firestore db = firestoreOpts.getService()) {
			ApiFuture<WriteResult> response = db.collection(entity.getCollectionName()).document(id)
					.update(entity.toMap(), precondition);
			ApiFutures.addCallback(response, updateCallbackHandler, rxJavaExecutor);

			return updateCallbackHandler.isUpdated();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
*/

/*
	public Single<Boolean> update(final K id, final String collectionName, final HashMap<String, Object> fields) {
		try (Firestore db = firestoreOpts.getService()) {
			HashMap<String, Single<Boolean>> result = new HashMap();
			ApiFuture<HashMap<String, Single<Boolean>>> transactionResponse = db.runTransaction(transaction -> {
				for (Map.Entry<String, Object> entry : fields.entrySet()) {
					DocumentReference docRef = db.collection(collectionName).document(id);

					ApiFuture<WriteResult> response = docRef.update(entry.getKey(), entry.getValue());
					ApiFutures.addCallback(response, updateCallbackHandler, rxJavaExecutor);
					result.put(entry.getKey(), updateCallbackHandler.isUpdated());
				}
				return result;
			});

			ApiFutures.addCallback(transactionResponse, partialUpdateCallbackHandler, rxJavaExecutor);
			return partialUpdateCallbackHandler.isUpdated();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
*/


	public Boolean delete(final String id, final String collectionName) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			DeleteCallbackHandler deleteCallbackHandler = new DeleteCallbackHandler();
			ApiFuture<WriteResult> response = db.collection(collectionName).document(id).delete();
			ApiFutures.addCallback(response, deleteCallbackHandler, Runnable::run);

			return deleteCallbackHandler.isDeleted().blockingGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}


	private void handlerInsert(Message<Object> message) {
		String _collectionName = message.headers().get("_collectionName");
		HashMap entity = Json.decodeValue((String) message.body(), HashMap.class);
		String id =  insert(entity, _collectionName);
		message.reply(id);
	}

	private void handlerEmpty(Message<Object> message) {
		String _collectionName = message.headers().get("_collectionName");
		String id = empty(_collectionName);
		message.reply(id);
	}

	private void handlerUpsert(Message<Object> message) {
		String _collectionName = message.headers().get("_collectionName");
		String _id = message.headers().get("_id");
		HashMap entity = Json.decodeValue((String) message.body(), HashMap.class);
		Boolean id = upsert(entity, _id, _collectionName);
		message.reply(id);
	}

	private void handlerGet(Message<Object> message) {
		String _collectionName = message.headers().get("_collectionName");
		String _id = message.headers().get("_id");
		Map<String,Object> entity = get(_id, _collectionName);
		message.reply(Json.encode(entity));
	}

	private void handlerUpdate(Message<Object> message) {
		String _collectionName = message.headers().get("_collectionName");
		String _id = message.headers().get("_id");
		HashMap entity = Json.decodeValue((String) message.body(), HashMap.class);
		Boolean updated = update(_id, _collectionName, entity);
		message.reply(Json.encode(updated));
	}

	private void handlerDelete(Message<Object> message) {
		String _collectionName = message.headers().get("_collectionName");
		String _id = message.headers().get("_id");
		Boolean deleted = delete(_id, _collectionName);
		message.reply(Json.encode(deleted));
	}

	private void handlerQueryBuilder(Message<byte[]> message) {
		String _collectionName = message.headers().get("_collectionName");
		Query query = queryBuilder(_collectionName);
		message.reply(SerializationUtils.serialize(query));
	}

	private void handlerQuery(Message<byte[]> message) {
		Query query = (Query) SerializationUtils.deserialize(message.body());
		List<Map<String, Object>> entityList = get(query);
		message.reply(Json.encode(entityList));
	}
	/**
	 * To delete a document with some given preconditions.
	 *
	 * @param precondition
	 * @param id
	 * @param collectionName
	 * @return Single boolean
	 */
/*
	public Single<Boolean> delete(final Precondition precondition, final K id, final String collectionName) {
		try (Firestore db = firestoreOpts.getService()) {
			ApiFuture<WriteResult> response = db.collection(collectionName).document(id).delete(precondition);
			ApiFutures.addCallback(response, deleteCallbackHandler, rxJavaExecutor);

			return deleteCallbackHandler.isDeleted();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}*/

}
