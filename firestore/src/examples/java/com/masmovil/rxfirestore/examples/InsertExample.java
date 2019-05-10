/*
 * Copyright 2019 RxFirestore.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.masmovil.rxfirestore.examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

import com.masmovil.rxfirestore.Vehicle;
import com.masmovil.rxfirestore.VehicleRepository;
import org.junit.Test;

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

	@Ignore
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
