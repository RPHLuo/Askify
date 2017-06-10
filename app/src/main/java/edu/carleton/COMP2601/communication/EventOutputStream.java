package edu.carleton.COMP2601.communication;

import java.io.IOException;

public interface EventOutputStream {
	public void putEvent(Event e) throws IOException, ClassNotFoundException;
}
