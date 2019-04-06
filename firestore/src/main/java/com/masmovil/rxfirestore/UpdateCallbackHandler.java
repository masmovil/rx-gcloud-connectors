package com.masmovil.rxfirestore;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.WriteResult;

import io.reactivex.subjects.SingleSubject;

public class UpdateCallbackHandler implements ApiFutureCallback<WriteResult> {

	private SingleSubject<Boolean> updated = SingleSubject.create();

	@Override
	public void onFailure(Throwable throwable) {
		updated.onError(throwable);
	}

	@Override
	public void onSuccess(WriteResult writeResult) {
		updated.onSuccess(true);
	}

	public SingleSubject<Boolean> isUpdated() {
		return updated;
	}
}
