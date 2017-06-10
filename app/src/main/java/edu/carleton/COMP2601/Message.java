package edu.carleton.COMP2601;

/**
 * Created by Luo on 2017-03-10.
 */

public class Message {
    private String name;
    private String message;
    private boolean anon;

    public Message(String message, boolean anon, String name){
        this.message = message;
        this.anon = anon;
        this.name = name;
    }

    public String getName() {return name;}

    public String getMessage() {return message;}

    public boolean isAnon() {return anon;}
}
