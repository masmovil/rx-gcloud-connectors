package com.masmovil.firestore;

import java.util.HashMap;
import java.util.Map;

public interface Entity {

	HashMap<String, Object> toMap();

	String getCollectionName();

	Entity fromJsonAsMap(Map<String, Object> json);

}
