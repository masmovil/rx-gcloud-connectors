package com.masmovil.rxfirestore.examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import com.masmovil.rxfirestore.Vehicle;
import com.masmovil.rxfirestore.VehicleRepository;

public class InsertExample {

	@Ignore
	@Test
	public void should_insert_100_vehicles() throws InterruptedException {
		final int iterations = 100;
		VehicleRepository vehicleRepository = new VehicleRepository();
		final CountDownLatch latch = new CountDownLatch(iterations);
		Vehicle vehicle = new Vehicle("Toyota", "Auris", true);
		Long initTime = System.currentTimeMillis();

		for (int i = 0; i < iterations; i++) {
			vehicleRepository.insert(vehicle).subscribe(id ->{
				latch.countDown();
				if(latch.getCount() == 0){
					Long amountTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - initTime);
					System.out.println("Result");
					System.out.println("========");
					System.out.println("Total time: " + (amountTime) + " sec");
					System.out.println("Op/sec: " + (iterations/ amountTime));
				}
			});
		}

		latch.await();
	}

	@Ignore
	@Test
	public void should_insert_500_vehicles() throws InterruptedException {
		final int iterations = 500;
		VehicleRepository vehicleRepository = new VehicleRepository();
		final CountDownLatch latch = new CountDownLatch(iterations);
		Vehicle vehicle = new Vehicle("Toyota", "Auris", true);
		Long initTime = System.currentTimeMillis();

		for (int i = 0; i < iterations; i++) {
			vehicleRepository.insert(vehicle).subscribe(id ->{
				latch.countDown();
				if(latch.getCount() == 0){
					Long amountTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - initTime);
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
	public void should_insert_1M_vehicles() throws InterruptedException {
		VehicleRepository vehicleRepository = new VehicleRepository();
		for(int j = 0; j < 200; j++) {
			final int iterations = 5000;
			final CountDownLatch latch = new CountDownLatch(iterations);
			Vehicle vehicle = new Vehicle("Toyota", "Auris", true);
			Long initTime = System.currentTimeMillis();
			System.out.println("Iteration " + j);
			for (int i = 0; i < iterations; i++) {
				vehicleRepository.insert(vehicle).subscribe(id -> {
					latch.countDown();
					if (latch.getCount() == 0) {
						Long amountTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - initTime);

						System.out.println("Result");
						System.out.println("========");
						System.out.println("Total time: " + (amountTime) + " sec");
						System.out.println("Op/sec: " + (iterations / amountTime));
					}
				},error -> System.err.println(error.getMessage()));
			}

			latch.await();
		}

	}
}
