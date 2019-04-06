package com.masmovil.rxfirestore;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.DocumentReference;

import io.reactivex.subjects.SingleSubject;

public class SingleEntityIdCallbackHandler<K> implements ApiFutureCallback<DocumentReference> {

	private SingleSubject<K> entityID = SingleSubject.create();

	@Override
	public void onFailure(Throwable throwable) {
		entityID.onError(throwable);
	}

	@Override
	public void onSuccess(DocumentReference documentReference) {
		entityID.onSuccess((K)documentReference.getId());
	}

	public SingleSubject<K> getEntityID() {
		return entityID;
	}
}
