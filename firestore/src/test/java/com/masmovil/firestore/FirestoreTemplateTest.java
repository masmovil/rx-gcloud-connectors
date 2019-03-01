package com.masmovil.firestore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import io.reactivex.subjects.SingleSubject;

@RunWith(MockitoJUnitRunner.class)
public class FirestoreTemplateTest{

	public class CarsRepository extends FirestoreTemplate<String, CarModel, CarModel> {
		public Firestore firestore;
		public CarsRepository(Firestore f) {
			super(f, Executors.newFixedThreadPool(1), CarModel::new);
			firestore = f;
		}
	}

	@Test
	public void insert() {

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(ApiFuture.class);
		var singleSubject = SingleSubject.create();
		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		SingleEntityIdCallbackHandler<CarModel> callbackHandler = mock(SingleEntityIdCallbackHandler.class);

		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).add(vehicle.toMap());
		doReturn(singleSubject).when(callbackHandler).getEntityID();

		repository.insert(vehicle);

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).add(vehicle.toMap());
		verify(docRef).addListener(any(), any());

	}

	@Test
	public void empty() {

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var vehicle = new CarModel("Toyota", "Land Cruiser", false);

		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document();
		doReturn("1234").when(docRef).getId();


		var id = repository.empty(vehicle.getCollectionName()).blockingGet();

		assertTrue(id.equalsIgnoreCase("1234"));

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document();
		verify(docRef).getId();

	}
}
