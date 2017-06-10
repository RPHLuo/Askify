package edu.carleton.COMP2601.communication;


public interface EventStream extends EventInputStream, EventOutputStream {
	public void close();
}
