package com.masmovil.firestore.example;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;


import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class FirestoreTemplateIT {

	//TODO: replace CREDENTIALS_PATH with your credentials.
	private final static String CREDENTIALS_PATH = "/Users/pjgg/Desktop/keyfile.json";
	private final static Logger LOG = LoggerFactory.getLogger(FirestoreTemplateIT.class);
	private VehicleRepository vehicleRepository = new VehicleRepository(CREDENTIALS_PATH, 1);


	@Test
	public void should_insert_car(){

		var vehicle = new Vehicle("Toyota", "Auris", true);
		while(true){
			vehicleRepository.insert(vehicle).subscribe(id -> System.out.println(id), error -> System.out.println(error.getMessage()));
		}
		/*var ID = vehicleRepository.insert(vehicle).blockingGet();

		LOG.info("retrieved ID " + ID);
		assertNotNull(ID, "Insert car FAIL. ID must be not null");*/
	}

	@Ignore
	@Test
	public void should_get_car(){

		String expectedModel = "Auris";
		var vehicle = new Vehicle("Toyota", expectedModel, true);
		var ID = vehicleRepository.insert(vehicle).blockingGet();
		var retrievedCar = vehicleRepository.get(ID, Vehicle.CARS_COLLECTION_NAME).blockingGet();

		LOG.info("retrieved ID " + retrievedCar.getModel());
		assertEquals(retrievedCar.getModel(), expectedModel);
	}

	@Ignore
	@Test
	public void should_update_car(){

		String expectedModel = "Auris_updated";
		var vehicle = new Vehicle("Toyota", "Auris", true);
		var ID = vehicleRepository.insert(vehicle).blockingGet();
		vehicle.setModel(expectedModel);
		var isUpdated = vehicleRepository.update(ID, vehicle).blockingGet();
		if (isUpdated) {
			var retrievedCar = vehicleRepository.get(ID, Vehicle.CARS_COLLECTION_NAME).blockingGet();
			assertEquals(retrievedCar.getModel(), expectedModel);
		}else{
			assertTrue(false, "Should update vehicle model, but return isUpdated false.");
		}
	}

	@Ignore
	@Test
	public void should_delete_car() {

		String expectedModel = "Auris_deleted";
		var vehicle = new Vehicle("Toyota", "Auris", true);
		var ID = vehicleRepository.insert(vehicle).blockingGet();
		vehicle.setModel(expectedModel);
		var isDeleted = vehicleRepository.delete(ID, Vehicle.CARS_COLLECTION_NAME).blockingGet();
		if (!isDeleted) {
			assertTrue(false, "Vehicle should be deleted");
		}
	}

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

	@Ignore
	@Test
	public void should_get_where(){
		String expectedModel = "Auris";
		var vehicle = new Vehicle("Toyota", expectedModel, true);
		var ID = vehicleRepository.insert(vehicle).blockingGet();


		var query = vehicleRepository.queryBuilder(Vehicle.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");

		var retrievedCar = vehicleRepository.get(query).blockingGet();

		LOG.info("Size " + retrievedCar.size());
		retrievedCar.stream().forEach(r -> assertEquals(r.getModel(), expectedModel));
	}


	@Ignore
	@Test
	public void should_subscribe_to_query() throws InterruptedException {

		var query = vehicleRepository.queryBuilder(Vehicle.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");
		var listener = vehicleRepository.addQueryListener(query, Optional.empty());

		listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()));

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
