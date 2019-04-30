package com.masmovil.rxfirestore;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class ListenerQueryIT {

	private VehicleRepository vehicleRepository = new VehicleRepository();

	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		VehicleRepository vehicleRepository = new VehicleRepository();
		Query query = vehicleRepository.queryBuilder(Vehicle.CARS_COLLECTION_NAME).blockingGet().whereEqualTo("brand","Toyota");

		EventListenerResponse<Vehicle> listener = vehicleRepository.addQueryListener(query, Optional.empty());
		listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()), error -> {error.printStackTrace();});

		Vehicle vehicle = new Vehicle("Toyota", "Auris", true);
		String ID = vehicleRepository.insert(vehicle).blockingGet();
		System.out.println(ID);
		Thread.sleep(10);

		vehicle = new Vehicle("Toyota", "Yaris", true);
		ID = vehicleRepository.insert(vehicle).blockingGet();
		System.out.println(ID);
		Thread.sleep(10);

		vehicle = new Vehicle("Toyota", "Hilux", true);
		ID = vehicleRepository.insert(vehicle).blockingGet();
		System.out.println(ID);
		Thread.sleep(5000);

		vehicle = new Vehicle("Toyota", "BRUTALLLL", true);
		ID = vehicleRepository.insert(vehicle).blockingGet();
		System.out.println(ID);
		Thread.sleep(1000);
		listener.getRegistration().remove();
	}

	@Ignore
	@Test
	public void should_subscribe_to_query() throws InterruptedException, TimeoutException, ExecutionException {

		Query query = vehicleRepository.queryBuilder(Vehicle.CARS_COLLECTION_NAME).blockingGet().whereEqualTo("brand","Toyota");
		EventListenerResponse<Vehicle> listener = vehicleRepository.addQueryListener(query, Optional.empty());
		listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()), error -> {error.printStackTrace();});

		Vehicle vehicle = new Vehicle("Toyota", "Auris", true);
		String ID = vehicleRepository.insert(vehicle).blockingGet();

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
