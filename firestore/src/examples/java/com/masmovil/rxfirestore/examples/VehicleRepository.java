package com.masmovil.rxfirestore.examples;

import com.masmovil.rxfirestore.RxFirestoreSDK;

public class VehicleRepository extends RxFirestoreSDK<Vehicle> {

	public VehicleRepository() {
		super(Vehicle::new);
	}

}