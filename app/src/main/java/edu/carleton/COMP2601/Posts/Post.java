package edu.carleton.COMP2601.Posts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.carleton.COMP2601.Message;
import edu.carleton.COMP2601.User;

/**
 * Created by Luo on 2017-03-11.
 */

public class Post {

    private String creator;
    private String title;
    private String message;
    private int id;
    private LinkedList<Message> comments;
    private boolean commentsEnabled;
    private static int ID_COUNTER = 1;

    public Post(String title, String message, boolean commentsEnabled) {
        this.title = title;
        this.message = message;
        this.commentsEnabled = commentsEnabled;
        if(commentsEnabled) {
            comments = new LinkedList<>();
        }else{
            comments = null;
        }
        creator = null;
        this.id = ID_COUNTER++;
    }

    public Post(String title, String message, boolean commentsEnabled, String creator) {
        this.title = title;
        this.message = message;
        this.commentsEnabled = commentsEnabled;
        this.creator = creator;
        if(commentsEnabled) {
            comments = new LinkedList<>();
        }
        this.id = ID_COUNTER++;
    }

    public void comment(Message c){comments.add(c);}
    public String getCreator() {return creator;}
    public String getTitle() {return title;}
    public String getMessage() {return message;}
    public boolean isCommentsEnabled() {return commentsEnabled;}
    public LinkedList<Message> getComments() {return comments;}
    public int getId() {return id;}
}
