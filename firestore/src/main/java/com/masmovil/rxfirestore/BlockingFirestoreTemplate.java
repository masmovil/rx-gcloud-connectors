package com.masmovil.rxfirestore;

import static com.masmovil.rxfirestore.FirestoreTemplate.SCOPES;
import static io.vertx.core.Future.future;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.ListenerRegistration;
import io.reactivex.subjects.SingleSubject;
import io.vertx.core.Future;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QuerySnapshot;

import io.vertx.reactivex.core.Vertx;

public class BlockingFirestoreTemplate<E extends Entity> {

	private final Supplier<? extends Entity> supplier;
	private final FirestoreOptions.Builder firestoreBuilder;
	private final SingleSubject<Vertx> vertx;

	public BlockingFirestoreTemplate(Supplier<? extends Entity> entityConstructor, SingleSubject<Vertx> vertxSubject) {
		supplier = Objects.requireNonNull(entityConstructor);
		this.vertx = vertxSubject;

		try {

			String keyPath = Optional.ofNullable(System.getenv("GCLOUD_KEY_PATH")).orElseThrow(
					() -> new IllegalArgumentException("GCLOUD_KEY_PATH is not set in the environment"));

			firestoreBuilder = FirestoreOptions.newBuilder().setCredentials(
					GoogleCredentials.fromStream(new FileInputStream(new File(keyPath))).createScoped(SCOPES));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * addQueryListener, You can listen to a document changes (create, update and delete).
	 *
	 * @param query to subscribe. Build your query with queryBuilder method.
	 * @param eventsHandler will handler document changes. By default we provide an eventHandler that will give you a
	 * Flowable with all the document changes.
	 * @return EventListenerResponse, contains two object. "registration" will allow you to close the event flow
	 * <p>
	 * example:
	 * <p>
	 * listener.getRegistration().remove();
	 * <p>
	 * "eventsFlow" represent a flow of changes. Firstly you will get all the events that match with your query, and then
	 * all the changes until you close your listener.
	 * <p>
	 * example:
	 * <p>
	 * listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " +
	 * event.getModel()));
	 */

	public EventListenerResponse<E> addQueryListener(final Query query,
			final Optional<EventListener<QuerySnapshot>> eventsHandler)
			throws InterruptedException, ExecutionException, TimeoutException {
		Future<EventListenerResponse<E>> res = future();
		CompletableFuture<EventListenerResponse<E>> fut = new CompletableFuture<>();
		vertx.subscribe(vertx -> {
			vertx.executeBlocking(future -> {
				Firestore db = firestoreBuilder.build().getService();
				DefaultEventListener<E> defaultHandler = new DefaultEventListener<E>(supplier.get());
				CollectionReference q = db.collection(query.getCollectionName());
				com.google.cloud.firestore.Query queryBuilder;

				queryBuilder = q.offset(0);

				HashMap<String, Object> equalTo = query.getEqualTo();
				Iterator equalToIt = equalTo.entrySet().iterator();
				while (equalToIt.hasNext()) {
					Map.Entry pair = (Map.Entry) equalToIt.next();
					queryBuilder = queryBuilder.whereEqualTo((String) pair.getKey(), pair.getValue());
				}

				HashMap<String, Object> arrayContains = query.getArrayContains();
				Iterator arrayContainsIt = arrayContains.entrySet().iterator();
				while (arrayContainsIt.hasNext()) {
					Map.Entry pair = (Map.Entry) arrayContainsIt.next();
					queryBuilder = queryBuilder.whereArrayContains((String) pair.getKey(), pair.getValue());
				}

				HashMap<String, Object> greaterThan = query.getGreaterThan();
				Iterator greaterThanIt = greaterThan.entrySet().iterator();
				while (greaterThanIt.hasNext()) {
					Map.Entry pair = (Map.Entry) greaterThanIt.next();
					queryBuilder = queryBuilder.whereGreaterThan((String) pair.getKey(), pair.getValue());
				}

				HashMap<String, Object> lessThan = query.getLessThan();
				Iterator lessThanIt = lessThan.entrySet().iterator();
				while (lessThanIt.hasNext()) {
					Map.Entry pair = (Map.Entry) lessThanIt.next();
					queryBuilder = queryBuilder.whereLessThan((String) pair.getKey(), pair.getValue());
				}

				ListenerRegistration listener = queryBuilder.addSnapshotListener(eventsHandler.orElse(defaultHandler));
				future.complete(new EventListenerResponse<E>(defaultHandler.getSource(), listener));
				fut.complete(new EventListenerResponse<E>(defaultHandler.getSource(), listener));
			}, res);
		});

		return fut.get(10, TimeUnit.SECONDS);
	}
}
