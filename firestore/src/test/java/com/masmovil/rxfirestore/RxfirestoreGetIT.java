package com.masmovil.rxfirestore;

import java.util.List;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

public class RxfirestoreGetIT {

	//TODO: You need to set your Gcloud creadentials as enviroment variable, example: GCLOUD_KEY_PATH=/Users/pablo/Desktop/keyfile.json
	private VehicleRepository vehicleRepository = new VehicleRepository();
	@Ignore
	@Test
	public void should_get_car(){

		TestObserver<Vehicle> testObserver = new TestObserver();
		String expectedModel = "Auris";
		Vehicle vehicle = new Vehicle("Toyota", expectedModel, true);
		Single<Vehicle> retrievedCar = vehicleRepository.insert(vehicle).flatMap(id -> vehicleRepository.get(id, Vehicle.CARS_COLLECTION_NAME));
		Observable<Vehicle> result = Observable.fromFuture(retrievedCar.toFuture());

		result.subscribe(testObserver);

		testObserver.assertComplete();
		testObserver.assertNoErrors();
		testObserver.assertValue(v -> v.getBrand().equalsIgnoreCase("Toyota"));

	}
	@Ignore
	@Test
	public void should_get_where(){
		TestObserver<List<Vehicle>> testObserver = new TestObserver();
		String expectedModel = "Auris";
		Vehicle vehicle = new Vehicle("Toyota", expectedModel, true);
		Single<List<Vehicle>> vehicles = vehicleRepository.insert(vehicle).flatMap(id -> vehicleRepository.queryBuilder(Vehicle.CARS_COLLECTION_NAME).flatMap(query -> vehicleRepository.get(query)));
		Observable<List<Vehicle>> result = Observable.fromFuture(vehicles.toFuture());

		result.subscribe(testObserver);

		testObserver.assertComplete();
		testObserver.assertNoErrors();

		testObserver.values().stream().forEach(vehicleList -> vehicleList.forEach(v -> v.getBrand().equalsIgnoreCase("Toyota")));

	}

}
