package com.masmovil.rxfirestore.examples;

import java.util.HashMap;
import java.util.Map;

import com.masmovil.rxfirestore.Entity;

public class Vehicle implements Entity {

	public final static String CARS_COLLECTION_NAME = "cars";
	public final static String BRAND = "brand";
	public final static String MODEL = "model";
	public final static String ELECTRIC = "electric";


	private transient String id;
	private transient String eventType;
	private String brand;
	private String model;
	private Boolean electric;

	public Vehicle(){}

	public Vehicle(String brand, String model, Boolean electric){
		this.brand = brand;
		this.model = model;
		this.electric = electric;

	}

	@Override
	public HashMap<String, Object> toMap() {
		return new HashMap<String, Object>(){{put(BRAND,brand);put(MODEL,model);put(ELECTRIC,electric); }};
	}

	@Override
	public String getCollectionName() {
		return CARS_COLLECTION_NAME;
	}

	@Override
	public Entity fromJsonAsMap(Map<String, Object> json) {

		this.brand = (String)json.get(BRAND);
		this.model = (String)json.get(MODEL);
		this.electric = (Boolean)json.get(ELECTRIC);
		this.id = (String)json.get("_id");
		this.eventType = (String)json.get("_eventType");

		return this;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Boolean getElectric() {
		return electric;
	}

	public void setElectric(Boolean electric) {
		this.electric = electric;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
}