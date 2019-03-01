package com.masmovil.firestore;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.cloud.firestore.WriteResult;

import io.reactivex.subjects.SingleSubject;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCallbackHandlerTest {

	private UpdateCallbackHandler handler;

	@Test
	public void should_return_error(){
		handler = new UpdateCallbackHandler();

		handler.onFailure(new Exception("Error"));
		SingleSubject<Boolean> response = handler.isUpdated();

		assertTrue(response.hasThrowable());
		assertTrue(response.getThrowable().getMessage().equalsIgnoreCase("error"));
	}

	@Test
	public void should_success(){

		handler = new UpdateCallbackHandler();
		WriteResult wr = mock(WriteResult.class);

		handler.onSuccess(wr);
		SingleSubject<Boolean> response = handler.isUpdated();

		assertTrue(response.getValue());
	}
}
