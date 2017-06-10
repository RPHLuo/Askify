package edu.carleton.COMP2601;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.carleton.COMP2601.Posts.Post;
import edu.carleton.COMP2601.Posts.PostListActivity;
import edu.carleton.COMP2601.Posts.Posts;
import edu.carleton.COMP2601.Questions.Question;
import edu.carleton.COMP2601.Questions.QuestionListActivity;
import edu.carleton.COMP2601.Questions.Questions;
import edu.carleton.COMP2601.communication.Event;
import edu.carleton.COMP2601.communication.EventHandler;
import edu.carleton.COMP2601.communication.Fields;
import edu.carleton.COMP2601.communication.JSONEventStream;
import edu.carleton.COMP2601.communication.Reactor;
import edu.carleton.COMP2601.communication.ThreadWithReactor;

/**
 * Created by Luo on 2017-03-04.
 */

public class HostService extends Service implements NetworkService{
    private Reactor reactor;
    private JSONEventStream es;
    private ThreadWithReactor twr;
    private ConcurrentHashMap<Integer, User> users;
    private ArrayList<String> usernames;
    private ServerSocket listener;

    private IBinder binder = new CustomBinder();

    @Override
    public void setUsername(String name) throws IOException {
        try {
            if (strictContains(name)) {
                //same username
                ChatActivity.chatActivity.setUsername(name, false);
            } else {
                usernames.add(name);
                ChatActivity.chatActivity.setUsername(name, true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void message(String message, boolean isAnonymous) throws IOException {
        Event messageEvent = new Event("message");
        if(isAnonymous) {
            messageEvent.put("anon", String.valueOf(true));
        }else{
            messageEvent.put("anon", String.valueOf(false));
            messageEvent.put("name", MainActivity.username);
        }
        messageEvent.put("message",message);
        try {
            for(int id : users.keySet()){
                users.get(id).getEs().putEvent(messageEvent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        ChatActivity.chatActivity.recieveMessage(messageEvent);
    }

    @Override
    public void post(Post newPost) throws IOException {
        Event postEvent = new Event("post");
        postEvent.put("post", MainActivity.gson.toJson(newPost));
        try {
            for(int id : users.keySet()){
                users.get(id).getEs().putEvent(postEvent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        PostListActivity.postListActivity.recievePost(newPost);
    }

    @Override
    public void commment(Message comment, int pid) throws IOException {
        Event commentEvent = new Event("comment");
        commentEvent.put("comment", MainActivity.gson.toJson(comment));
        commentEvent.put("post_id", String.valueOf(pid));
        try {
            for(int uid : users.keySet()){
                users.get(uid).getEs().putEvent(commentEvent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        PostListActivity.postListActivity.recieveComment(commentEvent);
    }

    @Override
    public void question(Question question) throws IOException {
        Event postEvent = new Event("question");
        postEvent.put("question", MainActivity.gson.toJson(question));
        try {
            for(int id : users.keySet()){
                users.get(id).getEs().putEvent(postEvent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        QuestionListActivity.questionListActivity.recieveQuestion(question);
    }

    @Override
    public void answer(Message answer, int qid) throws IOException {
        Event answerEvent = new Event("answer");
        answerEvent.put("answer", MainActivity.gson.toJson(answer));
        answerEvent.put("question_id", String.valueOf(qid));
        try {
            for(int uid : users.keySet()){
                users.get(uid).getEs().putEvent(answerEvent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        QuestionListActivity.questionListActivity.recieveAnswer(answerEvent);
    }

    @Override
    public void catchupPosts() throws IOException {
        //do nothng
    }

    @Override
    public void catchupQuestions() throws IOException {
        //do nothing
    }

    @Override
    public void disconnect(){
        Event disconnectEvent = new Event("disconnect");
        try {
            for(int uid : users.keySet()){
                users.get(uid).getEs().putEvent(disconnectEvent);
                users.get(uid).getEs().close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        es.close();
    }

    public class CustomBinder extends Binder {
        HostService getService() {
            return HostService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void listen(final int port){
        setup();
        AsyncTask<Object, Void, String> asyncTask = new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object[] params) {
                try{
                    System.out.println(MainActivity.addr);
                    listener = new ServerSocket(port,50,InetAddress.getByName(MainActivity.addr));
                    while(true){
                        System.out.println("waiting");
                        Socket s = listener.accept();
                        System.out.println("connection!");
                        InputStream is = s.getInputStream();
                        OutputStream os = s.getOutputStream();
                        es = new JSONEventStream(is,os);
                        twr = new ThreadWithReactor(es, reactor);
                        twr.start();
                        User u = new User(twr,es);
                        users.put(u.getId(),u);
                        welcomeMessage(es,u.getId());
                    }
                } catch (Exception e){
                    System.err.println(e);
                }
                return null;
            }
        }.execute();
    }

    private void welcomeMessage(JSONEventStream esi, int id) throws IOException {
        Event welcome = new Event("connect",esi);
        welcome.put(Fields.ID,String.valueOf(id));
        esi.putEvent(welcome);
    }

    public void setup(){
        usernames = new ArrayList<>();
        users = new ConcurrentHashMap<>();
        reactor = new Reactor();
        reactor.register("username", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                try {
                    String name = event.get("name");
                    User user = users.get(Integer.parseInt(event.get(Fields.ID)));
                    Event response = new Event("username");
                    if (strictContains(name)) {
                        //same username
                        response.put("result",String.valueOf(false));
                    } else {
                        usernames.add(name);
                        user.setUsername(name);
                        response.put("result",String.valueOf(true));
                        response.put("name",name);
                    }
                    user.getEs().putEvent(response);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        //send and recieve messages
        reactor.register("message", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Event message = new Event("message");
                if(Boolean.parseBoolean(event.get("anon"))) {
                    message.put("anon", String.valueOf(true));
                }else{
                    message.put("anon", String.valueOf(false));
                    message.put("name", users.get(Integer.parseInt(event.get(Fields.ID))).getUsername());
                }
                message.put("message",event.get("message"));
                try {
                    for(int id : users.keySet()){
                        users.get(id).getEs().putEvent(message);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                ChatActivity.chatActivity.recieveMessage(message);
            }
        });
        reactor.register("disconnect", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Event disconnectEvent = new Event("disconnect");
                try {
                    users.get(Integer.parseInt(event.get(Fields.ID))).getEs().putEvent(disconnectEvent);
                    users.remove(Integer.parseInt(event.get(Fields.ID)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reactor.register("post", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                try {
                    Event postEvent = new Event("post");
                    postEvent.put("post",event.get("post"));
                    for (int i : users.keySet()) {
                        users.get(i).getEs().putEvent(postEvent);
                    }
                    //host post event
                    if(PostListActivity.postListActivity != null){
                        PostListActivity.postListActivity.recievePost(MainActivity.gson.fromJson(event.get("post"),Post.class));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        reactor.register("comment", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                try {
                    Event commentEvent = new Event("comment");
                    commentEvent.put("comment", event.get("comment"));
                    commentEvent.put("post_id", event.get("post_id"));
                    for (int i : users.keySet()) {
                        users.get(i).getEs().putEvent(commentEvent);
                    }
                    //host post event
                    if(PostListActivity.postListActivity != null){
                        PostListActivity.postListActivity.recieveComment(event);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        reactor.register("catchupposts", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                try {
                    Event catchupEvent = new Event("catchupposts");
                    catchupEvent.put("posts",MainActivity.gson.toJson(Posts.POSTS));
                    findUser(Integer.parseInt(event.get(Fields.ID))).getEs().putEvent(catchupEvent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reactor.register("catchupquestions", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                try {
                    Event catchupEvent = new Event("catchupquestions");
                    catchupEvent.put("questions",MainActivity.gson.toJson(Questions.QUESTIONS));
                    findUser(Integer.parseInt(event.get(Fields.ID))).getEs().putEvent(catchupEvent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reactor.register("question", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                try {
                    Event postEvent = new Event("question");
                    postEvent.put("question",event.get("question"));
                    for (int i : users.keySet()) {
                        users.get(i).getEs().putEvent(postEvent);
                    }
                    //host post event
                    if(QuestionListActivity.questionListActivity != null){
                        QuestionListActivity.questionListActivity.recieveQuestion(MainActivity.gson.fromJson(event.get("question"),Question.class));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        reactor.register("answer", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                try {
                    Event answerEvent = new Event("answer");
                    answerEvent.put("answer", event.get("answer"));
                    answerEvent.put("question_id", event.get("question_id"));
                    for (int i : users.keySet()) {
                        users.get(i).getEs().putEvent(answerEvent);
                    }
                    //host post event
                    if(QuestionListActivity.questionListActivity != null){
                        QuestionListActivity.questionListActivity.recieveAnswer(answerEvent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    //usernames may not differ by extra whitespaces or caps
    private boolean strictContains(String checkUsername){
        String[] usernameArray = checkUsername.split(" ");
        String newUsername = "";
        for(String part:usernameArray){
            newUsername += part;
        }
        newUsername = newUsername.toLowerCase();
        for(String username:usernames){
            usernameArray = username.split(" ");
            String oldUsername="";
            for(String part:usernameArray){
                oldUsername += part;
            }
            if (oldUsername.toLowerCase().equals(newUsername)) {
                return true;
            }
        }
        return false;
    }

    private User findUser(int id){
        for (Integer u:users.keySet()) {
            if(users.get(u).getId()==id){
                return users.get(u);
            }
        }
        return null;
    }

    private User findUser(String username){
        for (Integer u:users.keySet()) {
            if(users.get(u).getUsername().equals(username)){
                return users.get(u);
            }
        }
        return null;
    }

    /*
    obsolete find by event stream

    private String findUsername(JSONEventStream es){
        for (int i : users.keySet()) {
            if(users.get(i).getEs().equals(es)){
                return users.get(i).getUsername();
            }
        }
        return "";
    }

    private User findUser(JSONEventStream es){
        for (int i : users.keySet()) {
            if(users.get(i).getEs().equals(es)){
                return users.get(i);
            }
        }
        return null;
    }
    */
}
