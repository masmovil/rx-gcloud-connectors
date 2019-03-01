package com.masmovil.firestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import io.reactivex.subjects.SingleSubject;

public class QueryCallbackHandler <E extends Entity> implements ApiFutureCallback<QuerySnapshot> {

	private SingleSubject<List<E>> entities = SingleSubject.create();
	private Entity response;

	public QueryCallbackHandler(Entity response){
		this.response = Objects.requireNonNull(response);
	}

	@Override
	public void onFailure(Throwable throwable) {
		entities.onError(throwable);
	}

	@Override
	public void onSuccess(QuerySnapshot futureDocuments) {
		List<E> result = new ArrayList<>();
		List<QueryDocumentSnapshot> documents =  futureDocuments.getDocuments();
		for (DocumentSnapshot document : documents) {
			var data = document.getData();
			data.put("_id", Optional.ofNullable(document.getId()).orElse("NONE"));
			result.add((E)response.fromJsonAsMap(data));
		}

		entities.onSuccess(result);
	}

	public SingleSubject<List<E>> getEntities() {
		return entities;
	}
}
