package com.masmovil.rxfirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import io.reactivex.Observable;
import io.reactivex.subjects.SingleSubject;

public class QueryCallbackHandler implements ApiFutureCallback<QuerySnapshot> {

	private SingleSubject<List<Map<String, Object>>> entities = SingleSubject.create();

	@Override
	public void onFailure(Throwable throwable) {
		entities.onError(throwable);
	}

	@Override
	public void onSuccess(QuerySnapshot futureDocuments) {
		List<Map<String, Object>> result = new ArrayList<>();
		List<QueryDocumentSnapshot> documents =  futureDocuments.getDocuments();
		for (DocumentSnapshot document : documents) {
			Map<String,Object> data = document.getData();
			data.put("_id", Optional.ofNullable(document.getId()).orElse("NONE"));
			result.add(data);
		}

		entities.onSuccess(result);
	}

	public SingleSubject<List<Map<String, Object>>> getEntities() {
		return entities;
	}
}
