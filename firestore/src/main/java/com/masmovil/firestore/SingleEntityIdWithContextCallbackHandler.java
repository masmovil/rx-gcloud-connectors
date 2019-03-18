package com.masmovil.firestore;

import java.util.Objects;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import io.reactivex.subjects.SingleSubject;

public class SingleEntityIdWithContextCallbackHandler <K> implements ApiFutureCallback<DocumentReference> {

	private SingleSubject<K> entityID = SingleSubject.create();
	private Firestore db;

	public SingleEntityIdWithContextCallbackHandler(Firestore firestore){
		this.db = firestore;
	}

	@Override
	public void onFailure(Throwable throwable) {
		entityID.onError(throwable);
		try {
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSuccess(DocumentReference documentReference) {
		entityID.onSuccess((K)documentReference.getId());
		try {
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

	}

	public SingleSubject<K> getEntityID() {
		return entityID;
	}
}
