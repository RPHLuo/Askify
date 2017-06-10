package edu.carleton.COMP2601.communication;

public interface ReactorInterface {
	public void register(String type, EventHandler event);
	public void deregister(String type);
	public void dispatch(Event event) throws NoEventHandler;
}
