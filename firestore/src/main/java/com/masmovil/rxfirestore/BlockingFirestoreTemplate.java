package com.masmovil.rxfirestore;

import static com.masmovil.rxfirestore.FirestoreTemplate.SCOPES;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
	private final Vertx vertx;

	public BlockingFirestoreTemplate(Supplier<? extends Entity> entityConstructor, Vertx vertx){
		supplier = Objects.requireNonNull(entityConstructor);
		this.vertx = vertx;

		try {

			var keyPath = Optional.ofNullable(System.getenv("GCLOUD_KEY_PATH")).orElseThrow(
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

	public EventListenerResponse<E> addQueryListener(final Query query, final Optional<EventListener<QuerySnapshot>> eventsHandler) {
		var wrapper = new Object(){ EventListenerResponse<E> eventListener;};
		vertx.executeBlocking(future ->{
			try (Firestore db = firestoreBuilder.build().getService()) {
				var defaultHandler = new DefaultEventListener<E>(supplier.get());

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

				var listener = queryBuilder.addSnapshotListener(eventsHandler.orElse(defaultHandler));
				future.complete(new EventListenerResponse<E>(defaultHandler.getSource(), listener));

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}, resonse ->{
			wrapper.eventListener = (EventListenerResponse<E>)resonse.result();
		});

		return wrapper.eventListener;
	}

}
