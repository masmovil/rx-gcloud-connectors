package com.masmovil.rxfirestore;

import java.util.Map;
import java.util.Optional;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.DocumentSnapshot;

import io.reactivex.subjects.SingleSubject;

public class SingleEntityCallbackHandler implements ApiFutureCallback<DocumentSnapshot> {

	private SingleSubject<Map<String, Object>> entity = SingleSubject.create();

	@Override
	public void onFailure(Throwable throwable) {
		entity.onError(throwable);
	}

	@Override
	public void onSuccess(DocumentSnapshot document) {
		if (document.exists()) {
			Map<String, Object> data = document.getData();
			data.put("_id", Optional.ofNullable(document.getId()).orElse("NONE"));
			entity.onSuccess(data);
		}else{
			entity.onError(new RuntimeException("Not Found"));
		}
	}

	public SingleSubject<Map<String, Object>> getEntity() {
		return entity;
	}

}
