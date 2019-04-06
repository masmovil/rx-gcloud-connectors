package com.masmovil.rxfirestore;

import org.junit.jupiter.api.Test;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

public class RxFirestoreDeleteIT {

	//TODO: You need to set your Gcloud creadentials as enviroment variable, example: GCLOUD_KEY_PATH=/Users/pablo/Desktop/keyfile.json
	private VehicleRepository vehicleRepository = new VehicleRepository();

	@Test
	public void should_delete_car() {

		TestObserver<Boolean> testObserver = new TestObserver();
		var vehicle = new Vehicle("Toyota", "Auris", true);
		Single<Boolean> isDeleted  = vehicleRepository.insert(vehicle).flatMap(id -> vehicleRepository.delete(id, Vehicle.CARS_COLLECTION_NAME));
		Observable<Boolean> result = Observable.fromFuture(isDeleted.toFuture());

		result.subscribe(testObserver);

		testObserver.assertComplete();
		testObserver.assertNoErrors();
		testObserver.assertNever(r -> r == false);
	}
}
