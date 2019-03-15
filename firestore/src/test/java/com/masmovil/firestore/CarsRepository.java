package com.masmovil.firestore;

import java.util.concurrent.Executors;

import com.google.cloud.firestore.Firestore;

public class CarsRepository extends FirestoreTemplate<String, CarModel, CarModel> {

	public CarsRepository(String keyPath, int threadPoolSize) {
		super(keyPath, Executors.newFixedThreadPool(threadPoolSize), CarModel::new);
	}

	public CarsRepository(Firestore fs) {
		super(fs, Executors.newFixedThreadPool(10), CarModel::new);
	}
}
