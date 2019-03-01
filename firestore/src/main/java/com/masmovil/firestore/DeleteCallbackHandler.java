package com.masmovil.firestore;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.WriteResult;

import io.reactivex.subjects.SingleSubject;

public class DeleteCallbackHandler implements ApiFutureCallback<WriteResult> {

	private SingleSubject<Boolean> deleted = SingleSubject.create();

	@Override
	public void onFailure(Throwable throwable) {
		deleted.onError(throwable);
	}

	@Override
	public void onSuccess(WriteResult writeResult) {
		deleted.onSuccess(true);
	}

	public SingleSubject<Boolean> isDeleted() {
		return deleted;
	}
}
