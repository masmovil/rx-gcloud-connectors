package com.masmovil.firestore;

import java.util.Objects;
import java.util.Optional;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.DocumentSnapshot;

import io.reactivex.subjects.SingleSubject;

public class SingleEntityCallbackHandler<E extends Entity> implements ApiFutureCallback<DocumentSnapshot> {

	private SingleSubject<E> entity = SingleSubject.create();
	private Entity response;

	public SingleEntityCallbackHandler(Entity response){
		this.response = Objects.requireNonNull(response);
	}

	@Override
	public void onFailure(Throwable throwable) {
		entity.onError(throwable);
	}

	@Override
	public void onSuccess(DocumentSnapshot document) {
		if (document.exists()) {
			var data = document.getData();
			data.put("_id", Optional.ofNullable(document.getId()).orElse("NONE"));
			entity.onSuccess((E)response.fromJsonAsMap(data));
		}else{
			entity.onError(new RuntimeException("Not Found"));
		}
	}

	public SingleSubject<E> getEntity() {
		return entity;
	}

}
