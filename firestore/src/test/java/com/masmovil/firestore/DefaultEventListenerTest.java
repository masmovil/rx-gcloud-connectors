package com.masmovil.firestore;


import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventListenerTest {

	private DefaultEventListener hanler;

	@Test
	public void onError(){
		FirestoreException exception = mock(FirestoreException.class);
		QuerySnapshot query = mock(QuerySnapshot.class);

		Supplier<? extends CarModel> supplier = Objects.requireNonNull(CarModel::new);
		hanler = new DefaultEventListener(supplier.get());
		hanler.onEvent(query, exception);

		TestObserver testObserver = new TestObserver();
		hanler.getSource().singleOrError().subscribe(testObserver);

		testObserver.assertNotComplete();
		testObserver.assertNoValues();
		testObserver.assertError(FirestoreException.class);
	}

	// TODO complete flowables unitTest
	@Ignore
	@Test
	public void onEvent_ADD(){
		var car = new CarModel("Toyota", "Land Cruiser", false);
		QuerySnapshot query = mock(QuerySnapshot.class);
		QueryDocumentSnapshot queryDoc = mock(QueryDocumentSnapshot.class);
		List<DocumentChange> docs = mock(List.class);
		Iterator<DocumentChange> itDocChange = mock(Iterator.class);
		DocumentChange singleDoc = mock(DocumentChange.class);

		var add = DocumentChange.Type.ADDED;

		doReturn(docs).when(query).getDocumentChanges();
		doReturn(itDocChange).when(docs).iterator();
		doReturn(singleDoc).when(itDocChange).next();
		doReturn(true).when(itDocChange).hasNext();

		doReturn(add).when(singleDoc).getType();
		doReturn(queryDoc).when(singleDoc).getDocument();
		doReturn(car.toMap()).when(queryDoc).getData();

		Supplier<? extends CarModel> supplier = Objects.requireNonNull(CarModel::new);
		hanler = new DefaultEventListener(supplier.get());
		hanler.onEvent(query, null);

		TestScheduler scheduler = new TestScheduler();
		scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

		TestSubscriber<List<CarModel>> subscriber = new TestSubscriber<>();
		hanler.getSource().subscribe(subscriber);
		CompletableFuture.runAsync(() -> hanler.getSource().subscribeOn(scheduler).subscribe(subscriber));

		scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

		car.setEventType("ADDED");
		car.setId("NONE");

		subscriber.assertNoErrors();
		//subscriber.assertValue(car);

		//assertThat(subscriber.getEvents().get(0), is(Arrays.asList(car)));

	}
}
