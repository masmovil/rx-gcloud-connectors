package com.masmovil.rxfirestore;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class ListenerQueryIT {

	private VehicleRepository vehicleRepository = new VehicleRepository();

	@Ignore
	@Test
	public void should_subscribe_to_query() throws InterruptedException, TimeoutException, ExecutionException {

		var query = vehicleRepository.queryBuilder(Vehicle.CARS_COLLECTION_NAME).blockingGet().whereEqualTo("brand","Toyota");
		var listener = vehicleRepository.addQueryListener(query, Optional.empty());
		listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()), error -> {error.printStackTrace();});

		var vehicle = new Vehicle("Toyota", "Auris", true);
		var ID = vehicleRepository.insert(vehicle).blockingGet();

		Thread.sleep(10);

		vehicle = new Vehicle("Toyota", "Yaris", true);
		ID = vehicleRepository.insert(vehicle).blockingGet();

		Thread.sleep(10);

		vehicle = new Vehicle("Toyota", "Hilux", true);
		ID = vehicleRepository.insert(vehicle).blockingGet();

		Thread.sleep(5000);

		vehicle = new Vehicle("Toyota", "BRUTALLLL", true);
		ID = vehicleRepository.insert(vehicle).blockingGet();

		Thread.sleep(1000);
		listener.getRegistration().remove();
	}

}
