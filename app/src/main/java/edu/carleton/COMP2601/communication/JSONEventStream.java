package edu.carleton.COMP2601.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by Luo on 2017-02-28.
 */

public class JSONEventStream implements EventStream {

    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Gson gson = new GsonBuilder().create();
    //for client
    public JSONEventStream(Socket s){
        this.s = s;
        try {
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONEventStream(InputStream is, OutputStream os) throws IOException {
        ois = new ObjectInputStream(is);
        oos = new ObjectOutputStream(os);
    }

    public JSONEventStream(OutputStream os, InputStream is) throws IOException {
        oos = new ObjectOutputStream(os);
        ois = new ObjectInputStream(is);
    }

    @Override
    public void close() {
        try {
            if (s != null)
                s.close();
            if (oos != null)
                oos.close();
            if (ois != null)
                ois.close();
            s = null;
            oos = null;
            ois = null;
        } catch (IOException e) {
            // Fail quietly
        }
    }

    @Override
    public void putEvent(Event e) throws IOException {
        try {
            oos.writeObject(gson.toJson(e));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public Event getEvent() throws IOException, ClassNotFoundException {
        return gson.fromJson((String)ois.readObject(),Event.class);
    }
}
