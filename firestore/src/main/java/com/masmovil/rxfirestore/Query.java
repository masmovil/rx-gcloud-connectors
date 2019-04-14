package com.masmovil.rxfirestore;

import java.io.Serializable;
import java.util.HashMap;

public class Query implements Serializable {

	private final String collectionName;
	private HashMap<String, Object> equalTo = new HashMap<>();
	private HashMap<String, Object> arrayContains = new HashMap<>();
	private HashMap<String,Object> greaterThan = new HashMap<>();
	private HashMap<String,Object> lessThan = new HashMap<>();
	private Integer limit;
	private Integer offset;
	private boolean limitSet;
	private boolean offsetSet;

	protected Query(String collecitonName) {
		this.collectionName = collecitonName;
	}

	public Query whereEqualTo(String field, Object value){
		equalTo.put(field, value);
		return this;
	}

	public Query whereGreaterThan(String field, Object value){
		greaterThan.put(field, value);
		return this;
	}

	public Query whereLessThan(String field, Object value){
		lessThan.put(field, value);
		return this;
	}

	public Query whereArrayContains(String field, Object value){
		arrayContains.put(field, value);
		return this;
	}

	public Query withLimit(Integer limit){
		this.limit = limit;
		limitSet = true;
		return this;
	}

	public Query withOffset(Integer offset){
		this.offset = offset;
		offsetSet = true;
		return this;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public boolean isLimitSet() {
		return limitSet;
	}

	public boolean isOffsetSet() {
		return offsetSet;
	}

	public Integer getLimit() {
		return limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public HashMap<String, Object> getEqualTo() {
		return equalTo;
	}

	public HashMap<String, Object> getArrayContains() {
		return arrayContains;
	}

	public HashMap<String, Object> getGreaterThan() {
		return greaterThan;
	}

	public HashMap<String, Object> getLessThan() {
		return lessThan;
	}
}
