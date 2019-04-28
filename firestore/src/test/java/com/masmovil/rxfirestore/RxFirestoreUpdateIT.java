package com.masmovil.rxfirestore;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

public class RxFirestoreUpdateIT {

	//TODO: You need to set your Gcloud creadentials as enviroment variable, example: GCLOUD_KEY_PATH=/Users/pablo/Desktop/keyfile.json
	private VehicleRepository vehicleRepository = new VehicleRepository();

	@Ignore
	@Test
	public void should_update_car() {

		TestObserver<Boolean> testObserver = new TestObserver();
		String expectedModel = "Auris_updated";
		Vehicle vehicle = new Vehicle("Toyota", "Auris", true);
		Single<Boolean> isUpdated = vehicleRepository.insert(vehicle).flatMap(id -> {
			vehicle.setModel(expectedModel);
			return vehicleRepository.update(id, vehicle.getCollectionName(), vehicle);
		});

		Observable<Boolean> result = Observable.fromFuture(isUpdated.toFuture());
		result.subscribe(testObserver);

		testObserver.assertComplete();
		testObserver.assertNoErrors();
		testObserver.assertNever(r -> r == false);
	}


/*
	@Ignore
	@Test
	public void should_update_partial_car(){

		String expectedModel = "Auris_updated";
		HashMap<String, Object> u = new HashMap();
		u.put("model", expectedModel);

		var vehicle = new Vehicle("Toyota", "Auris", true);
		var ID = vehicleRepository.insert(vehicle).blockingGet();
		vehicle.setModel(expectedModel);
		var isUpdated = vehicleRepository.update(ID, Vehicle.CARS_COLLECTION_NAME, u).blockingGet();

		if (isUpdated) {
			var retrievedCar = vehicleRepository.get(ID, Vehicle.CARS_COLLECTION_NAME).blockingGet();
			assertEquals(retrievedCar.getModel(), expectedModel);
		}else{
			assertTrue(false, "Should update vehicle model, but return isUpdated false.");
		}
	}


*/
}
