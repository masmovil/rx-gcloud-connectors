package com.masmovil.firestore;

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
	private final static String CREDENTIALS_PATH = "~/Desktop/keyfile.json";
	private final static Logger LOG = LoggerFactory.getLogger(FirestoreTemplateIT.class);
	private CarsRepository carsRepository = new CarsRepository(CREDENTIALS_PATH, 1);

	@Ignore
	@Test
	public void should_insert_car(){

		var carExample = new CarModel("Toyota", "Auris", true);
		var ID = carsRepository.insert(carExample).blockingGet();

		LOG.info("retrieved ID " + ID);
		assertNotNull(ID, "Insert car FAIL. ID must be not null");
	}

	@Ignore
	@Test
	public void should_get_car(){

		String expectedModel = "Auris";
		var carExample = new CarModel("Toyota", expectedModel, true);
		var ID = carsRepository.insert(carExample).blockingGet();
		var retrievedCar = carsRepository.get(ID, CarModel.CARS_COLLECTION_NAME).blockingGet();

		LOG.info("retrieved ID " + retrievedCar.getModel());
		assertEquals(retrievedCar.getModel(), expectedModel);
	}

	@Ignore
	@Test
	public void should_update_car(){

		String expectedModel = "Auris_updated";
		var carExample = new CarModel("Toyota", "Auris", true);
		var ID = carsRepository.insert(carExample).blockingGet();
		carExample.setModel(expectedModel);
		var isUpdated = carsRepository.update(ID, carExample).blockingGet();
		if (isUpdated) {
			var retrievedCar = carsRepository.get(ID, CarModel.CARS_COLLECTION_NAME).blockingGet();
			assertEquals(retrievedCar.getModel(), expectedModel);
		}else{
			assertTrue(false, "Should update vehicle model, but return isUpdated false.");
		}
	}

	@Ignore
	@Test
	public void should_delete_car() {

		String expectedModel = "Auris_deleted";
		var carExample = new CarModel("Toyota", "Auris", true);
		var ID = carsRepository.insert(carExample).blockingGet();
		carExample.setModel(expectedModel);
		var isDeleted = carsRepository.delete(ID, CarModel.CARS_COLLECTION_NAME).blockingGet();
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

		var carExample = new CarModel("Toyota", "Auris", true);
		var ID = carsRepository.insert(carExample).blockingGet();
		carExample.setModel(expectedModel);
		var isUpdated = carsRepository.update(ID, CarModel.CARS_COLLECTION_NAME, u).blockingGet();

		if (isUpdated) {
			var retrievedCar = carsRepository.get(ID, CarModel.CARS_COLLECTION_NAME).blockingGet();
			assertEquals(retrievedCar.getModel(), expectedModel);
		}else{
			assertTrue(false, "Should update vehicle model, but return isUpdated false.");
		}
	}

	@Ignore
	@Test
	public void should_get_where(){
		String expectedModel = "Auris";
		var carExample = new CarModel("Toyota", expectedModel, true);
		var ID = carsRepository.insert(carExample).blockingGet();


		var query = carsRepository.queryBuilder(CarModel.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");

		var retrievedCar = carsRepository.get(query).blockingGet();

		LOG.info("Size " + retrievedCar.size());
		retrievedCar.stream().forEach(r -> assertEquals(r.getModel(), expectedModel));
	}


	@Ignore
	@Test
	public void should_subscribe_to_query() throws InterruptedException {

		var query = carsRepository.queryBuilder(CarModel.CARS_COLLECTION_NAME).whereEqualTo("brand","Toyota");
		var listener = carsRepository.addQueryListener(query, Optional.empty());

		listener.getEventsFlow().subscribe(event -> System.out.println("Event Type:"+ event.getEventType() + " model: " + event.getModel()));

		var carExample = new CarModel("Toyota", "Auris", true);
		var ID = carsRepository.insert(carExample).blockingGet();

		Thread.sleep(10);

		carExample = new CarModel("Toyota", "Yaris", true);
		ID = carsRepository.insert(carExample).blockingGet();

		Thread.sleep(10);

		carExample = new CarModel("Toyota", "Hilux", true);
		ID = carsRepository.insert(carExample).blockingGet();

		Thread.sleep(5000);

		carExample = new CarModel("Toyota", "BRUTALLLL", true);
		ID = carsRepository.insert(carExample).blockingGet();

		Thread.sleep(1000);
		listener.getRegistration().remove();
	}

}
