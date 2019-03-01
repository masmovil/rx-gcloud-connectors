package com.masmovil.firestore;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import io.reactivex.subjects.SingleSubject;

@RunWith(MockitoJUnitRunner.class)
public class QueryCallbackHandlerTest {

	private QueryCallbackHandler handler;

	@Test
	public void should_return_error(){
		Supplier<? extends CarModel> supplier = Objects.requireNonNull(CarModel::new);
		handler = new QueryCallbackHandler(supplier.get());

		handler.onFailure(new Exception("Error"));
		SingleSubject<List<CarModel>> response = handler.getEntities();

		assertTrue(response.hasThrowable());
		assertTrue(response.getThrowable().getMessage().equalsIgnoreCase("error"));
	}

	@Test
	public void should_success(){

		var car = new CarModel("Toyota", "Land Cruiser", false);
		QueryDocumentSnapshot qDoc = mock(QueryDocumentSnapshot.class);
		QuerySnapshot qs = mock(QuerySnapshot.class);
		List<QueryDocumentSnapshot> listDoc = new ArrayList<>();
		listDoc.add(qDoc);

		Supplier<? extends CarModel> supplier = Objects.requireNonNull(CarModel::new);
		handler = new QueryCallbackHandler((CarModel)supplier.get());

		doReturn(car.toMap()).when(qDoc).getData();
		doReturn(listDoc).when(qs).getDocuments();

		handler.onSuccess(qs);
		SingleSubject<List<CarModel>> response = handler.getEntities();

		assertTrue(response.getValue().get(0).getModel().equalsIgnoreCase("Land Cruiser"));
		verify(qs).getDocuments();
		verify(qDoc).getData();
		verify(qDoc).getId();
	}

}
