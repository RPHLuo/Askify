package edu.carleton.COMP2601.communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class Event implements EventStream {
	/**
	 * Class can be used to write itself or read/write other events from/to
	 * an EventStream. The es attribute is made public so that events which
	 * are read from an event stream are marked as having been obtained from 
	 * a particular stream. The es attribute is transient as it makes no
	 * sense to share it when serializing the event.
	 */
	public final String type;
	public transient JSONEventStream es;
	private HashMap<String, String> map;
	
	public Event(String type) {
		this.type = type;
		this.es = null;
		this.map = new HashMap<String, String>();
	}

	public Event(String type, JSONEventStream es) {
		this.type = type;
		this.es = es;
		this.map = new HashMap<String, String>();
	}
	
	public Event(String type, JSONEventStream es, HashMap<String, String> map) {
		this.type = type;
		this.es = es;
		this.map = map;
	}
	
	public void put(String key, String value) {
		map.put(key, value);
	}
	
	public String get(String key) {
		return map.get(key);
	}
	
	public void putEvent() throws ClassNotFoundException, IOException {
		putEvent(this);
	}
	
	public void putEvent(Event e) throws IOException, ClassNotFoundException {
		if (es != null)
			es.putEvent(e);
		else
			throw new IOException("No event stream defined");
	}
	
	public Event getEvent() throws ClassNotFoundException, IOException {
		if (es != null) {
			return es.getEvent();
		} else 
			throw new IOException("No event stream defined");
	}

	public void close() {
		if (es != null) {
			es.close();
		}
	}
}
