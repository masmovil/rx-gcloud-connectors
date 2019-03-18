package com.masmovil.firestore;

import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.cloud.firestore.DocumentReference;

import io.reactivex.subjects.SingleSubject;

@RunWith(MockitoJUnitRunner.class)
public class SingleEntityIdCallbackHandlerTest {

	private SingleEntityIdCallbackHandler singleEntityIdCallbackHandler;

	@Test
	public void should_return_error(){
		singleEntityIdCallbackHandler = new SingleEntityIdCallbackHandler();

		singleEntityIdCallbackHandler.onFailure(new Exception("Error"));
		SingleSubject<CarModel> response = singleEntityIdCallbackHandler.getEntityID();

		assertTrue(response.hasThrowable());
		assertTrue(response.getThrowable().getMessage().equalsIgnoreCase("error"));
	}

	@Test
	public void should_success(){

		singleEntityIdCallbackHandler = new SingleEntityIdCallbackHandler();
		DocumentReference doc = mock(DocumentReference.class);
		var id = "bcjsrkjcwecw";

		//var car = new Vehicle("Toyota", "Land Cruiser", false);
		doReturn(id).when(doc).getId();

		singleEntityIdCallbackHandler.onSuccess(doc);
		SingleSubject<String> response = singleEntityIdCallbackHandler.getEntityID();

		assertTrue(response.getValue().equalsIgnoreCase(id));
		verify(doc).getId();
	}

}
