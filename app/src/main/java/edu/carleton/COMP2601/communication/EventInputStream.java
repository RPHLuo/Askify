package edu.carleton.COMP2601.communication;

import java.io.IOException;

public interface EventInputStream {
	public Event getEvent() throws IOException, ClassNotFoundException;
}
