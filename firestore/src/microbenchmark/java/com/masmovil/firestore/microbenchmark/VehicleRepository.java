package com.masmovil.firestore.microbenchmark;

import java.util.concurrent.Executors;

import com.masmovil.firestore.FirestoreTemplate;

public class VehicleRepository extends FirestoreTemplate<String, Vehicle, Vehicle> {

	public VehicleRepository(String keyPath, int threadPoolSize) {
		super(keyPath, Executors.newFixedThreadPool(threadPoolSize), Vehicle::new);
	}

}
