package com.masmovil.firestore;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;

@RunWith(MockitoJUnitRunner.class)
public class PartialUpdateCallbackHandlerTest {

	private PartialUpdateCallbackHandler handler;

	@Test
	public void should_return_error(){
		handler = new PartialUpdateCallbackHandler();
		handler.onFailure(new Exception("Error"));
		SingleSubject<Boolean> response = handler.isUpdated();

		assertTrue(response.hasThrowable());
		assertTrue(response.getThrowable().getMessage().equalsIgnoreCase("error"));
	}

	@Test
	public void should_success(){
		var car = new CarModel("Toyota", "Land Cruiser", false);
		handler = new PartialUpdateCallbackHandler();
		HashMap<String, Single<Boolean>> input = new HashMap();
		input.put("id", Single.just(true));

		handler.onSuccess(input);
		SingleSubject<Boolean> response = handler.isUpdated();

		assertTrue(response.getValue());
	}

}
