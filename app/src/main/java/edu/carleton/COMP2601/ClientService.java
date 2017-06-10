package edu.carleton.COMP2601;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.Socket;

import edu.carleton.COMP2601.Posts.Post;
import edu.carleton.COMP2601.Questions.Question;
import edu.carleton.COMP2601.communication.Event;
import edu.carleton.COMP2601.communication.Fields;
import edu.carleton.COMP2601.communication.JSONEventStream;
import edu.carleton.COMP2601.communication.ThreadWithReactor;

/**
 * Created by Luo on 2017-03-04.
 */

public class ClientService extends Service implements NetworkService {

    //Authorization
    public static String id;

    protected ThreadWithReactor twr;
    protected JSONEventStream es;

    private IBinder binder = new CustomBinder();
    public class CustomBinder extends Binder {
        ClientService getService() {
            return ClientService.this;
        }
    }

    private Socket s;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public void connect(final String addr, final int port) {
        AsyncTask<Object, Void, String> asyncTask = new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object[] params) {
                try {
                    s = new Socket(addr, port);
                    es = new JSONEventStream(s.getOutputStream(), s.getInputStream());
                    twr = new ThreadWithReactor(es, MainActivity.reactor);
                    twr.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void setUsername(String name) throws IOException {
        Event e = new Event("username",es);
        e.put(Fields.ID,id);
        e.put("name", name);
        es.putEvent(e);
    }

    @Override
    public void message(String message, boolean isAnonymous) throws IOException{
        Event e = new Event("message",es);
        e.put(Fields.ID,id);
        e.put("anon", String.valueOf(isAnonymous));
        e.put("message",message);
        es.putEvent(e);
    }

    @Override
    public void post(Post newPost) throws IOException {
        Event postEvent = new Event("post");
        postEvent.put("post", MainActivity.gson.toJson(newPost));
        es.putEvent(postEvent);
    }

    @Override
    public void commment(Message comment, int pid) throws IOException {
        Event commentEvent = new Event("comment");
        commentEvent.put("comment", MainActivity.gson.toJson(comment));
        commentEvent.put("post_id", String.valueOf(pid));
        es.putEvent(commentEvent);
    }

    @Override
    public void question(Question question) throws IOException {
        Event postEvent = new Event("question");
        postEvent.put("question", MainActivity.gson.toJson(question));
        es.putEvent(postEvent);
    }

    @Override
    public void answer(Message answer, int qid) throws IOException {
        Event answerEvent = new Event("answer");
        answerEvent.put("answer", MainActivity.gson.toJson(answer));
        answerEvent.put("question_id", String.valueOf(qid));
        es.putEvent(answerEvent);
    }


    @Override
    public void catchupPosts() throws IOException {
        Event catchupEvent = new Event("catchupposts");
        catchupEvent.put(Fields.ID,String.valueOf(id));
        es.putEvent(catchupEvent);
    }

    @Override
    public void catchupQuestions() throws IOException {
        Event catchupEvent = new Event("catchupquestions");
        catchupEvent.put(Fields.ID,String.valueOf(id));
        es.putEvent(catchupEvent);
    }

    @Override
    public void disconnect() throws IOException {
        Event e = new Event("disconnect",es);
        e.put(Fields.ID,id);
        es.putEvent(e);
    }
}
