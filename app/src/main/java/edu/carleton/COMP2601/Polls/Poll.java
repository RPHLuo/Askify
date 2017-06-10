package edu.carleton.COMP2601.Polls;

import java.util.HashSet;
import java.util.Set;

import edu.carleton.COMP2601.User;

/**
 * Created by Luo on 2017-03-11.
 */

public class Poll {
    private String title;
    private int target;
    private HashSet<User> participants;
    public Poll(String title){
        this.title = title;
        target = -1;
        participants = new HashSet<>();
    }
    public Poll(String title, int target){
        this.title = title;
        this.target = target;
        participants = new HashSet<>();
    }

    
}
