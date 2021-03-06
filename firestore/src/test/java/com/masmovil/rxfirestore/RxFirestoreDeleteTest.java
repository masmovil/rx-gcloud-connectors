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

package com.masmovil.rxfirestore;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;

public class RxFirestoreDeleteTest {

	//TODO: You need to set your Gcloud creadentials as enviroment variable, example: GOOGLE_APPLICATION_CREDENTIALS=/Users/pablo/Desktop/keyfile.json
	private VehicleRepository vehicleRepository = new VehicleRepository();
	private final String brandName = "Toyota";

	@Before
	public void clean_scenario() {
		Query query = vehicleRepository.queryBuilderSync(Vehicle.CARS_COLLECTION_NAME);
		vehicleRepository.get(query.whereEqualTo("brand", brandName)).blockingGet().forEach(vehicle -> {
			vehicleRepository.delete(vehicle.getId(), Vehicle.CARS_COLLECTION_NAME).blockingGet();
		});
		System.out.println("Done!");
	}

	@Test
	public void should_delete_car() {

		TestObserver<Boolean> testObserver = new TestObserver();
		Vehicle vehicle = new Vehicle(brandName, "Auris", true);
		Single<Boolean> isDeleted  = vehicleRepository.insert(vehicle).flatMap(id -> vehicleRepository.delete(id, Vehicle.CARS_COLLECTION_NAME));
		Observable<Boolean> result = Observable.fromFuture(isDeleted.toFuture());

		result.subscribe(testObserver);

		testObserver.assertComplete();
		testObserver.assertNoErrors();
		testObserver.assertNever(r -> r == false);
	}
}
