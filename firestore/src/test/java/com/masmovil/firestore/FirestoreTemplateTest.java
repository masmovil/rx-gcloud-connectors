package com.masmovil.firestore;

import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.api.core.ApiFuture;
import com.google.api.core.SettableApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Precondition;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;

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
	public void insertTest() {

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(ApiFuture.class);
		var singleSubject = SingleSubject.create();
		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		SingleEntityIdCallbackHandler<CarModel> callbackHandler = mock(SingleEntityIdCallbackHandler.class);

		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).add(vehicle.toMap());

		repository.insert(vehicle);


		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).add(vehicle.toMap());
		verify(docRef).addListener(any(), any());

	}

	@Test
	public void emptyTest() {

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

	@Test
	public void upsertTest(){

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var writeResult = mock(WriteResult.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document("1234");

		SettableApiFuture<WriteResult> futureFirestoreResp = SettableApiFuture.<WriteResult>create();
		futureFirestoreResp.set(writeResult);

		doReturn(futureFirestoreResp).when(docRef).set(vehicle.toMap());

		var resp = repository.upsert(vehicle,"1234");

		assertTrue(resp.blockingGet());

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document("1234");
		verify(docRef).set(vehicle.toMap());
	}

	@Test
	public void upsertWithOptionsTest() {
		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var writeResult = mock(WriteResult.class);
		var options = mock(SetOptions.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document("1234");

		SettableApiFuture<WriteResult> futureFirestoreResp = SettableApiFuture.<WriteResult>create();
		futureFirestoreResp.set(writeResult);

		doReturn(futureFirestoreResp).when(docRef).set(vehicle.toMap(), options);
		var resp = repository.upsert(options, vehicle,"1234");

		assertTrue(resp.blockingGet());

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document("1234");
		verify(docRef).set(vehicle.toMap(), options);
	}

	@Test
	public void getTest(){

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var documentSnapshot = mock(DocumentSnapshot.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document("1234");

		SettableApiFuture<DocumentSnapshot> futureFirestoreResp = SettableApiFuture.<DocumentSnapshot>create();
		futureFirestoreResp.set(documentSnapshot);

		doReturn(true).when(documentSnapshot).exists();
		doReturn(vehicle.toMap()).when(documentSnapshot).getData();
		doReturn(futureFirestoreResp).when(docRef).get();

		var resp = repository.get("1234", vehicle.getCollectionName());

		assertTrue(resp.blockingGet().getModel().equalsIgnoreCase("Land Cruiser"));
		verify(documentSnapshot).exists();
		verify(documentSnapshot).getData();
		verify(documentSnapshot).getId();
		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document("1234");
	}

	@Test
	public void queryBuilderTest(){

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var vehicle = new CarModel("Toyota", "Land Cruiser", false);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		CarsRepository repository = new CarsRepository(firestore);

		var resp = repository.queryBuilder(vehicle.getCollectionName());

		assertTrue(resp instanceof CollectionReference);
		verify(firestore).collection(vehicle.getCollectionName());
	}

	//TODO complete addQueryListenerTest unitTest
	@Ignore
	@Test
	public void addQueryListenerTest(){

	}

	//TODO complete partialUpdateTest unitTest
	@Ignore
	@Test
	public void partialUpdateTest() {

	}

	@Test
	public void getListTest(){
		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var qDoc = mock(QueryDocumentSnapshot.class);
		List<QueryDocumentSnapshot> listDoc = new ArrayList<>();
		listDoc.add(qDoc);
		var querySnapshot = mock(QuerySnapshot.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());

		SettableApiFuture<QuerySnapshot> futureFirestoreResp = SettableApiFuture.<QuerySnapshot>create();
		futureFirestoreResp.set(querySnapshot);

		doReturn(futureFirestoreResp).when(collectionRef).get();
		doReturn(listDoc).when(querySnapshot).getDocuments();
		doReturn(vehicle.toMap()).when(qDoc).getData();

		var query = repository.queryBuilder(vehicle.getCollectionName());
		var response = repository.get(query);

		assertTrue(response.blockingGet().get(0).getModel().equalsIgnoreCase("Land Cruiser"));
		verify(querySnapshot).getDocuments();
		verify(qDoc).getData();
		verify(qDoc).getId();
		verify(firestore).collection(vehicle.getCollectionName());
	}

	@Test
	public void updateTest(){

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var writeResult = mock(WriteResult.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document("1234");

		SettableApiFuture<WriteResult> futureFirestoreResp = SettableApiFuture.<WriteResult>create();
		futureFirestoreResp.set(writeResult);
		doReturn(futureFirestoreResp).when(docRef).update(vehicle.toMap());

		var resp = repository.update("1234", vehicle);

		assertTrue(resp.blockingGet());

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document("1234");
		verify(docRef).update(vehicle.toMap());
	}

	@Test
	public void updateWithPreconditionsTest(){

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var writeResult = mock(WriteResult.class);
		var preconditions = mock(Precondition.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document("1234");

		SettableApiFuture<WriteResult> futureFirestoreResp = SettableApiFuture.<WriteResult>create();
		futureFirestoreResp.set(writeResult);
		doReturn(futureFirestoreResp).when(docRef).update(vehicle.toMap(), preconditions);

		var resp = repository.update(preconditions, "1234", vehicle);

		assertTrue(resp.blockingGet());

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document("1234");
		verify(docRef).update(vehicle.toMap(), preconditions);

	}

	@Test
	public void deleteTest(){

		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var writeResult = mock(WriteResult.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document("1234");

		SettableApiFuture<WriteResult> futureFirestoreResp = SettableApiFuture.<WriteResult>create();
		futureFirestoreResp.set(writeResult);
		doReturn(futureFirestoreResp).when(docRef).delete();

		var resp = repository.delete("1234", vehicle.getCollectionName());

		assertTrue(resp.blockingGet());

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document("1234");
		verify(docRef).delete();
	}

	@Test
	public void deleteWithPreconditionsTest(){


		var firestore = mock(Firestore.class);
		var collectionRef = mock(CollectionReference.class);
		var docRef = mock(DocumentReference.class);
		var writeResult = mock(WriteResult.class);
		var preconditions = mock(Precondition.class);

		var vehicle = new CarModel("Toyota", "Land Cruiser", false);
		CarsRepository repository = new CarsRepository(firestore);

		doReturn(collectionRef).when(firestore).collection(vehicle.getCollectionName());
		doReturn(docRef).when(collectionRef).document("1234");

		SettableApiFuture<WriteResult> futureFirestoreResp = SettableApiFuture.<WriteResult>create();
		futureFirestoreResp.set(writeResult);
		doReturn(futureFirestoreResp).when(docRef).delete(preconditions);

		var resp = repository.delete(preconditions, "1234", vehicle.getCollectionName());

		assertTrue(resp.blockingGet());

		verify(firestore).collection(vehicle.getCollectionName());
		verify(collectionRef).document("1234");
		verify(docRef).delete(preconditions);
	}

}
