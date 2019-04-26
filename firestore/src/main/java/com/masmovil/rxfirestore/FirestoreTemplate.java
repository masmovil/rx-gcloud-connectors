package com.masmovil.rxfirestore;

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
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.ImmutableList;

import io.vertx.core.json.Json;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;


public class FirestoreTemplate extends AbstractVerticle {


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

			var keyPath = Optional.ofNullable(System.getenv("GCLOUD_KEY_PATH")).orElseThrow(
					() -> new IllegalArgumentException("GCLOUD_KEY_PATH is not set in the environment"));

			firestoreBuilder = FirestoreOptions.newBuilder().setCredentials(
					GoogleCredentials.fromStream(new FileInputStream(new File(keyPath))).createScoped(SCOPES));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void start() {
		var firestoreEventBus = vertx.eventBus();

		MessageConsumer<Object> insertConsumer = firestoreEventBus.localConsumer(TOPIC_INSERT);
		insertConsumer.handler(this::handlerInsert);

		MessageConsumer<Object> emptyConsumer = firestoreEventBus.localConsumer(TOPIC_EMPTY);
		emptyConsumer.handler(this::handlerEmpty);

		MessageConsumer<Object> upsertConsumer = firestoreEventBus.localConsumer(TOPIC_UPSERT);
		upsertConsumer.handler(this::handlerUpsert);

		MessageConsumer<Object> getConsumer = firestoreEventBus.localConsumer(TOPIC_GET);
		getConsumer.handler(this::handlerGet);

		MessageConsumer<Object> updateConsumer = firestoreEventBus.localConsumer(TOPIC_UPDATE);
		updateConsumer.handler(this::handlerUpdate);

		MessageConsumer<Object> deleteConsumer = firestoreEventBus.localConsumer(TOPIC_DELETE);
		deleteConsumer.handler(this::handlerDelete);

		MessageConsumer<byte[]> queryBuilderConsumer = firestoreEventBus.localConsumer(TOPIC_QUERY_BUILDER);
		queryBuilderConsumer.handler(this::handlerQueryBuilder);

		MessageConsumer<byte[]> queryConsumer = firestoreEventBus.localConsumer(TOPIC_QUERY);
		queryConsumer.handler(this::handlerQuery);

	}

	public String insert(final HashMap<String, Object> entity, final String collectionName) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			var singleEntityId = new SingleEntityIdCallbackHandler<String>();
			ApiFuture<DocumentReference> response = db.collection(collectionName).add(entity);
			ApiFutures.addCallback(response, singleEntityId, Runnable::run);
			return singleEntityId.getEntityID().blockingGet();
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
			var updateCallbackHandler = new UpdateCallbackHandler();
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
			var entityCallbackHandler = new SingleEntityCallbackHandler();
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

	public List<Map<String, Object>> get(final Query query) {
		try (Firestore db = firestoreBuilder.build().getService()) {
			var q = db.collection(query.getCollectionName());
			com.google.cloud.firestore.Query queryBuilder;

			if(query.isLimitSet()){
				queryBuilder = q.limit(query.getLimit());
			}else{
				queryBuilder = q.limit(20);
			}

			if(query.isOffsetSet()){
				queryBuilder.offset(query.getOffset());
			}

			var equalTo = query.getEqualTo();
			Iterator equalToIt = equalTo.entrySet().iterator();
			while (equalToIt.hasNext()) {
				Map.Entry pair = (Map.Entry) equalToIt.next();
				queryBuilder = queryBuilder.whereEqualTo((String)pair.getKey(), pair.getValue());
			}

			var arrayContains = query.getArrayContains();
			Iterator arrayContainsIt = arrayContains.entrySet().iterator();
			while (arrayContainsIt.hasNext()) {
				Map.Entry pair = (Map.Entry) arrayContainsIt.next();
				queryBuilder = queryBuilder.whereArrayContains((String)pair.getKey(), pair.getValue());
			}

			var greaterThan = query.getGreaterThan();
			Iterator greaterThanIt = greaterThan.entrySet().iterator();
			while (greaterThanIt.hasNext()) {
				Map.Entry pair = (Map.Entry) greaterThanIt.next();
				queryBuilder = queryBuilder.whereGreaterThan((String)pair.getKey(), pair.getValue());
			}

			var lessThan = query.getLessThan();
			Iterator lessThanIt = lessThan.entrySet().iterator();
			while (lessThanIt.hasNext()) {
				Map.Entry pair = (Map.Entry) lessThanIt.next();
				queryBuilder = queryBuilder.whereLessThan((String)pair.getKey(), pair.getValue());
			}

			var queryCallbackHandler = new QueryCallbackHandler();
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
			var updateCallbackHandler = new UpdateCallbackHandler();
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
			var deleteCallbackHandler = new DeleteCallbackHandler();
			ApiFuture<WriteResult> response = db.collection(collectionName).document(id).delete();
			ApiFutures.addCallback(response, deleteCallbackHandler, Runnable::run);

			return deleteCallbackHandler.isDeleted().blockingGet();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	private void handlerInsert(Message<Object> message) {
		var _collectionName = message.headers().get("_collectionName");
		var entity = Json.decodeValue((String) message.body(), HashMap.class);
		var id = insert(entity, _collectionName);

		message.rxReply(id).onErrorReturn(throwable -> {
			message.fail(001, throwable.getMessage());
			return message;
		}).subscribe();
	}

	private void handlerEmpty(Message<Object> message) {
		var _collectionName = message.headers().get("_collectionName");
		var id = empty(_collectionName);

		message.rxReply(id).onErrorReturn(throwable -> {
			message.fail(001, throwable.getMessage());
			return message;
		}).subscribe();
	}

	private void handlerUpsert(Message<Object> message) {
		var _collectionName = message.headers().get("_collectionName");
		var _id = message.headers().get("_id");
		var entity = Json.decodeValue((String) message.body(), HashMap.class);
		var id = upsert(entity, _id, _collectionName);

		message.rxReply(id).onErrorReturn(throwable -> {
			message.fail(001, throwable.getMessage());
			return message;
		}).subscribe();
	}

	private void handlerGet(Message<Object> message) {
		var _collectionName = message.headers().get("_collectionName");
		var _id = message.headers().get("_id");
		var entity = get(_id, _collectionName);

		message.rxReply(Json.encode(entity)).onErrorReturn(throwable -> {
			message.fail(001, throwable.getMessage());
			return message;
		}).subscribe();
	}

	private void handlerUpdate(Message<Object> message) {
		var _collectionName = message.headers().get("_collectionName");
		var _id = message.headers().get("_id");
		var entity = Json.decodeValue((String) message.body(), HashMap.class);
		var updated = update(_id, _collectionName, entity);

		message.rxReply(Json.encode(updated)).onErrorReturn(throwable -> {
			message.fail(001, throwable.getMessage());
			return message;
		}).subscribe();
	}

	private void handlerDelete(Message<Object> message) {
		var _collectionName = message.headers().get("_collectionName");
		var _id = message.headers().get("_id");
		var deleted = delete(_id, _collectionName);

		message.rxReply(Json.encode(deleted)).onErrorReturn(throwable -> {
			message.fail(001, throwable.getMessage());
			return message;
		}).subscribe();
	}

	private void handlerQueryBuilder(Message<byte[]> message) {
		var _collectionName = message.headers().get("_collectionName");
		var query = queryBuilder(_collectionName);

		message.rxReply(SerializationUtils.serialize(query)).subscribe();
	}

	private void handlerQuery(Message<byte[]> message) {
		var query = (Query) SerializationUtils.deserialize(message.body());
		var entityList = get(query);

		message.rxReply(Json.encode(entityList)).subscribe();
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
