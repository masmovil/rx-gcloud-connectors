package com.masmovil.firestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.api.core.ApiFutureCallback;

import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;

public class PartialUpdateCallbackHandler implements ApiFutureCallback<HashMap<String, Single<Boolean>>> {

	private SingleSubject<Boolean> updated = SingleSubject.create();

	@Override
	public void onFailure(Throwable throwable) {
		updated.onError(throwable);
	}

	@Override
	public void onSuccess(HashMap<String, Single<Boolean>> result) {
		final AtomicReference<Boolean> allOperationSuccess = new AtomicReference<>();
		allOperationSuccess.set(true);
		for (Map.Entry<String, Single<Boolean>> entry : result.entrySet()){
			if(allOperationSuccess.get()) {
				entry.getValue().subscribe(isUpdated -> allOperationSuccess.set(isUpdated));
			}
		}

		updated.onSuccess(allOperationSuccess.get());
	}

	public SingleSubject<Boolean> isUpdated() {
		return updated;
	}
}
