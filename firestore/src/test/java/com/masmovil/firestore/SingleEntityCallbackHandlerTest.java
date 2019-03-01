package com.masmovil.firestore;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Objects;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.cloud.firestore.DocumentSnapshot;

import io.reactivex.subjects.SingleSubject;

@RunWith(MockitoJUnitRunner.class)
public class SingleEntityCallbackHandlerTest {

	private SingleEntityCallbackHandler singleEntityCallbackHandler;

	@Test
	public void should_return_error(){

		Supplier<? extends CarModel> supplier = Objects.requireNonNull(CarModel::new);
		singleEntityCallbackHandler = new SingleEntityCallbackHandler(supplier.get());
		singleEntityCallbackHandler.onFailure(new Exception("Error"));
		SingleSubject<CarModel> response = singleEntityCallbackHandler.getEntity();

		assertTrue(response.hasThrowable());
		assertTrue(response.getThrowable().getMessage().equalsIgnoreCase("error"));
	}

	@Test
	public void should_success(){
		var car = new CarModel("Toyota", "Land Cruiser", false);
		Supplier<? extends CarModel> supplier = Objects.requireNonNull(CarModel::new);
		singleEntityCallbackHandler = new SingleEntityCallbackHandler((CarModel)supplier.get());
		DocumentSnapshot doc = mock(DocumentSnapshot.class);

		doReturn(true).when(doc).exists();
		doReturn(car.toMap()).when(doc).getData();

		singleEntityCallbackHandler.onSuccess(doc);
		SingleSubject<CarModel> response = singleEntityCallbackHandler.getEntity();

		assertTrue(response.getValue().getModel().equalsIgnoreCase("Land Cruiser"));
		verify(doc).exists();
		verify(doc).getData();
		verify(doc).getId();

	}

}
