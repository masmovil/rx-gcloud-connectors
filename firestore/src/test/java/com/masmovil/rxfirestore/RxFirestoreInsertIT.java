package com.masmovil.rxfirestore;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

public class RxFirestoreInsertIT {

	//TODO: You need to set your Gcloud creadentials as enviroment variable, example: GCLOUD_KEY_PATH=/Users/pablo/Desktop/keyfile.json
	private VehicleRepository vehicleRepository = new VehicleRepository();

	@Test
	public void should_insert_car() throws InterruptedException {

		TestObserver<String> testObserver = new TestObserver();
		var vehicle = new Vehicle("Toyota", "Auris", true);
		Single<String> ID = vehicleRepository.insert(vehicle);
		Observable<String> result = Observable.fromFuture(ID.toFuture());

        result.subscribe(testObserver);

		testObserver.assertComplete();
		testObserver.assertNoErrors();
		testObserver.assertOf(id -> {
			System.out.println(id.values());
			assertNotNull(id);
		});
	}

}
