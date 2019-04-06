package com.masmovil.rxfirestore;

public class VehicleRepository extends RxFirestoreSDK<Vehicle> {

	public VehicleRepository() {
		super(Vehicle::new);
	}

}
