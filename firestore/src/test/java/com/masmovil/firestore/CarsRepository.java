package com.masmovil.firestore;

import java.util.concurrent.Executors;

public class CarsRepository extends FirestoreTemplate<String, CarModel, CarModel> {

	public CarsRepository(String keyPath, int threadPoolSize) {
		super(keyPath, Executors.newFixedThreadPool(10), CarModel::new);
	}
}
