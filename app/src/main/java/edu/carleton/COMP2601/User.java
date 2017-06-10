package edu.carleton.COMP2601;

import java.io.Serializable;

import edu.carleton.COMP2601.communication.EventStream;
import edu.carleton.COMP2601.communication.JSONEventStream;
import edu.carleton.COMP2601.communication.ThreadWithReactor;

/**
 * Created by Luo on 2017-03-05.
 */

public class User implements Serializable {
    private static int ID_COUNTER = 0;
    private ThreadWithReactor twr;
    private JSONEventStream es;
    private String username;
    private int id;
    private boolean canAddUsername;
    public User(ThreadWithReactor twr, JSONEventStream es){
        this.id = ID_COUNTER++;
        //default username
        this.username = "USER#" + id;
        this.twr = twr;
        this.es = es;
        canAddUsername = true;
    }
    public ThreadWithReactor getTwr(){return twr;}

    public String getUsername() {return username;}

    //can only set the username once
    public void setUsername(String username) {
        if(canAddUsername) {
            this.username = username;
            canAddUsername = false;
        }
    }

    public int getId() {return id;}
    public JSONEventStream getEs(){return es;}
}
