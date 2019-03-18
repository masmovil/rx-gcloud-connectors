package com.masmovil.firestore.example;

import java.util.concurrent.Executors;

import com.google.cloud.firestore.Firestore;
import com.masmovil.firestore.CarModel;
import com.masmovil.firestore.FirestoreTemplate;

public class VehicleRepository extends FirestoreTemplate<String, Vehicle, Vehicle> {

	public VehicleRepository(String keyPath, int threadPoolSize) {
		super(keyPath, Executors.newFixedThreadPool(threadPoolSize), Vehicle::new);
	}

}
