package com.masmovil.rxfirestore;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;

public class DefaultEventListener<E extends Entity> implements EventListener<QuerySnapshot> {

	private PublishProcessor<E> source = PublishProcessor.create();
	private Entity response;

	public DefaultEventListener(Entity response){
		this.response = Objects.requireNonNull(response);
	}

	@Override
	public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException e) {

		if (e != null) {
			source.onError(e);
			return;
		}

		for (DocumentChange dc : snapshots.getDocumentChanges()) {
			switch (dc.getType()) {
			case ADDED:
				var dataAdded = dc.getDocument().getData();
				dataAdded.put("_id", Optional.ofNullable(dc.getDocument().getId()).orElse("NONE"));
				dataAdded.put("_eventType",DocumentChange.Type.ADDED.toString());
				source.onNext((E)response.fromJsonAsMap(dataAdded));
				break;

			case MODIFIED:
				var dataUpdated = dc.getDocument().getData();
				dataUpdated.put("_id", Optional.ofNullable(dc.getDocument().getId()).orElse("NONE"));
				dataUpdated.put("_eventType",DocumentChange.Type.MODIFIED.toString());
				source.onNext((E)response.fromJsonAsMap(dataUpdated));
				break;

			case REMOVED:
				var dataRemoved = dc.getDocument().getData();
				dataRemoved.put("_id", Optional.ofNullable(dc.getDocument().getId()).orElse("NONE"));
				dataRemoved.put("_eventType",DocumentChange.Type.REMOVED.toString());
				source.onNext((E)response.fromJsonAsMap(dataRemoved));
				break;

			default:
				break;
			}
		}
	}

	public Flowable<E> getSource() {
		return source.onBackpressureLatest();
	}


}
