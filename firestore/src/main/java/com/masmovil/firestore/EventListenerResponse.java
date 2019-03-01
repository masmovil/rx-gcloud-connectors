package com.masmovil.firestore;

import com.google.cloud.firestore.ListenerRegistration;

import io.reactivex.Flowable;

public class EventListenerResponse <E extends Entity> {

	private Flowable<E> eventsFlow;

	private ListenerRegistration registration;

	public EventListenerResponse(Flowable<E> e, ListenerRegistration r){
		eventsFlow = e;
		registration = r;
	}

	public Flowable<E> getEventsFlow() {
		return eventsFlow;
	}

	public void setEventsFlow(Flowable<E> eventsFlow) {
		this.eventsFlow = eventsFlow;
	}

	public ListenerRegistration getRegistration() {
		return registration;
	}

	public void setRegistration(ListenerRegistration registration) {
		this.registration = registration;
	}
}
