package edu.carleton.COMP2601;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Luo on 2017-02-21.
 */

public class CustomAdapter extends BaseAdapter {
    private Activity activity;
    private LinkedList<Message> data;
    private static LayoutInflater inflater=null;

    public CustomAdapter(Activity a, LinkedList d){
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {return data.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null){
            vi = inflater.inflate(R.layout.message,null);
        }
        Message m = (Message)getItem(position);
        ((TextView)vi.findViewById(R.id.messageText)).setText(m.getMessage());
        if(m.isAnon()){
            ((ImageView)vi.findViewById(R.id.anon)).setImageResource(R.drawable.anon);
            ((TextView)vi.findViewById(R.id.user)).setText("");
        }else{
            ((ImageView)vi.findViewById(R.id.anon)).setImageResource(R.drawable.named);
            ((TextView)vi.findViewById(R.id.user)).setText(m.getName());
        }
        return vi;
    }
}
