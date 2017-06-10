package edu.carleton.COMP2601;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.carleton.COMP2601.Posts.Post;
import edu.carleton.COMP2601.Posts.Posts;
import edu.carleton.COMP2601.communication.Event;
import edu.carleton.COMP2601.communication.EventHandler;
import edu.carleton.COMP2601.communication.Fields;
import edu.carleton.COMP2601.communication.Reactor;

public class MainActivity extends AppCompatActivity {
    public static String addr;
    public static int port;
    public static boolean isHost;
    public static String username;

    public static HostService hs;
    public static ClientService cs;
    public static Reactor reactor;

    public static Gson gson = new GsonBuilder().create();

    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(name.getClassName().contains("HostService")){
                //hosting
                HostService.CustomBinder b = (HostService.CustomBinder)service;
                hs = b.getService();
                hs.listen(port);
            }else{
                //connecting
                ClientService.CustomBinder b = (ClientService.CustomBinder)service;
                cs = b.getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(name.getClassName().contains("HostService")){
                //hosting
                hs = null;
            }else{
                //connecting
                cs = null;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
        findViewById(R.id.btnHost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host();
            }
        });
        findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    protected void setup(){
        username = "";
        addr = getIPAddress(getApplicationContext());
        Toast.makeText(getApplicationContext(),addr,Toast.LENGTH_LONG).show();
        System.out.println(addr);
        ((EditText)findViewById(R.id.address)).setText(addr);
        reactor = new Reactor();
        reactor.register("connect", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                cs.id = event.get(Fields.ID);
                Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
                startActivity(chatIntent);
            }
        });
    }

    //code found online that uses wifiManager to get wifi address
    private String getIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

    protected void host(){
        port = Integer.parseInt(((EditText)findViewById(R.id.port)).getText().toString());
        Intent newIntent = new Intent(getApplicationContext(),HostService.class);
        startService(newIntent);
        bindService(newIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        //connects to the chat as a client too
        isHost = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
                startActivity(chatIntent);
            }
        },1000);
    }

    protected void connect() {
        try {
            addr = ((EditText) findViewById(R.id.address)).getText().toString();
            port = Integer.parseInt(((EditText) findViewById(R.id.port)).getText().toString());
            Intent newIntent = new Intent(getApplicationContext(),ClientService.class);
            startService(newIntent);
            bindService(newIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            isHost = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cs.connect(addr, port);
                }
            },1000);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Invalid IP address or Port", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
