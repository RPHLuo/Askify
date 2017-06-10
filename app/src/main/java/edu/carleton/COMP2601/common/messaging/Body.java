package edu.carleton.COMP2601.common.messaging;

import java.io.Serializable;
import java.util.HashMap;

public class Body implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5728956330855011743L;
	private HashMap<String,Serializable> map;	// Contains all properties for the body of the message
	
	Body() {
		map = new HashMap<String, Serializable>();
	}
	
	public void addField(String name, Serializable value) {
		map.put(name, value);
	}
	
	public void removeField(String name) {
		map.remove(name);
	}
	
	public Serializable getField(String name) {
		return map.get(name);
	}
	
	public HashMap<String, Serializable> getMap() {
		return map;
	}
}
