package com.masmovil.rxfirestore.examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.masmovil.rxfirestore.Vehicle;
import com.masmovil.rxfirestore.VehicleRepository;

public class InsertExample {

	@Test
	public void should_insert_100_vehicles() throws InterruptedException {
		final int iterations = 100;
		VehicleRepository vehicleRepository = new VehicleRepository();
		final CountDownLatch latch = new CountDownLatch(iterations);
		var vehicle = new Vehicle("Toyota", "Auris", true);
		var initTime = System.currentTimeMillis();

		for (int i = 0; i < iterations; i++) {
			vehicleRepository.insert(vehicle).subscribe(id ->{
				latch.countDown();
				if(latch.getCount() == 0){
					var amountTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - initTime);
					System.out.println("Result");
					System.out.println("========");
					System.out.println("Total time: " + (amountTime) + " sec");
					System.out.println("Op/sec: " + (iterations/ amountTime));
				}
			});
		}

		latch.await();
	}


	@Test
	public void should_insert_500_vehicles() throws InterruptedException {
		final int iterations = 500;
		VehicleRepository vehicleRepository = new VehicleRepository();
		final CountDownLatch latch = new CountDownLatch(iterations);
		var vehicle = new Vehicle("Toyota", "Auris", true);
		var initTime = System.currentTimeMillis();

		for (int i = 0; i < iterations; i++) {
			vehicleRepository.insert(vehicle).subscribe(id ->{
				latch.countDown();
				if(latch.getCount() == 0){
					var amountTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - initTime);
					System.out.println("Result");
					System.out.println("========");
					System.out.println("Total time: " + (amountTime) + " sec");
					System.out.println("Op/sec: " + (iterations/ amountTime));
				}
			});
		}

		latch.await();
	}

	@Test
	public void should_insert_1000_vehicles() throws InterruptedException {
		final int iterations = 1000;
		VehicleRepository vehicleRepository = new VehicleRepository();
		final CountDownLatch latch = new CountDownLatch(iterations);
		var vehicle = new Vehicle("Toyota", "Auris", true);
		var initTime = System.currentTimeMillis();

		for (int i = 0; i < iterations; i++) {
			vehicleRepository.insert(vehicle).subscribe(id ->{
				latch.countDown();
				if(latch.getCount() == 0){
					var amountTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - initTime);
					System.out.println("Result");
					System.out.println("========");
					System.out.println("Total time: " + (amountTime) + " sec");
					System.out.println("Op/sec: " + (iterations/ amountTime));
				}
			});
		}

		latch.await();
	}
}
