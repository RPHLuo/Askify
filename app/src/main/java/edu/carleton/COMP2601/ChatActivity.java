package edu.carleton.COMP2601;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.carleton.COMP2601.Posts.Post;
import edu.carleton.COMP2601.Posts.PostListActivity;
import edu.carleton.COMP2601.Posts.Posts;
import edu.carleton.COMP2601.Questions.Question;
import edu.carleton.COMP2601.Questions.QuestionListActivity;
import edu.carleton.COMP2601.Questions.Questions;
import edu.carleton.COMP2601.communication.Event;
import edu.carleton.COMP2601.communication.EventHandler;
import edu.carleton.COMP2601.communication.Reactor;

public class ChatActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static ChatActivity chatActivity;

    public NetworkService service;
    public Reactor reactor;
    private boolean isAnonymous;
    private CustomAdapter adapter;
    private LinkedList<Message> messages;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerToggle = toggle;
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        chatActivity = this;
        setup();
    }

    private void setup(){
        isAnonymous = true;
        if(MainActivity.isHost){
            service = MainActivity.hs;
        }else{
            service = MainActivity.cs;
        }

        //set up listview
        messages = new LinkedList<>();
        adapter = new CustomAdapter(this,messages);
        ((ListView)findViewById(R.id.lstMessages)).setAdapter(adapter);

        reactor = MainActivity.reactor;
        //reactor listeners for client

        reactor.register("username", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                if(Boolean.parseBoolean(event.get("result"))) {
                    final Event closureEvent = event;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.head_name)).setText( closureEvent.get("name"));
                            ((TextView)findViewById(R.id.head_ipaddr)).setText(MainActivity.addr);
                            ((TextView)findViewById(R.id.head_port)).setText(String.valueOf(MainActivity.port));
                            MainActivity.username = closureEvent.get("name");
                            enableDrawer();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Username already taken", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
        });
        reactor.register("message", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                final Event closureEvent = event;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(Boolean.parseBoolean(closureEvent.get("anon"))){
                            messages.add(new Message( closureEvent.get("message"), true, ""));
                        }else {
                            messages.add(new Message( closureEvent.get("message"), false, closureEvent.get("name")));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        reactor.register("post", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Post newPost = MainActivity.gson.fromJson(event.get("post"), Post.class);
                Posts.addItem(newPost);
            }
        });
        reactor.register("comment", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Posts.POSTS_MAP.get(Integer.parseInt(event.get("post_id"))).comment(MainActivity.gson.fromJson(event.get("comment"),Message.class));
            }
        });
        reactor.register("question", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Question newQuestion = MainActivity.gson.fromJson(event.get("question"), Question.class);
                Questions.addItem(newQuestion);
            }
        });
        reactor.register("answer", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Questions.QUESTIONS_MAP.get(Integer.parseInt(event.get("question_id"))).answer(MainActivity.gson.fromJson(event.get("answer"),Message.class));
            }
        });
        reactor.register("disconnect", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                finish();
            }
        });

        reactor.register("catchupposts", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Posts.setPOSTS((List<Post>)MainActivity.gson.fromJson(event.get("posts"), new TypeToken<List<Post>>(){}.getType()));
            }
        });

        reactor.register("catchupquestions", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                Questions.setQUESTIONS((List<Question>)MainActivity.gson.fromJson(event.get("questions"), new TypeToken<List<Question>>(){}.getType()));
            }
        });
        try {
            service.catchupPosts();
            service.catchupQuestions();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //UI listeners to send messages
        (findViewById(R.id.fabIncog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAnonymous){
                    ((FloatingActionButton)v).setImageResource(R.drawable.named);
                    isAnonymous = false;
                }else{
                    ((FloatingActionButton)v).setImageResource(R.drawable.anon);
                    isAnonymous = true;
                }
            }
        });
        (findViewById(R.id.fabSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.username.equals("")){
                    String name = ((EditText) findViewById(R.id.messagebox)).getText().toString();
                    try{
                        service.setUsername(name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    final String message = ((EditText) findViewById(R.id.messagebox)).getText().toString();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                service.message(message, isAnonymous);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                ((EditText) findViewById(R.id.messagebox)).setText("");
            }
        });
    }

    public void enableDrawer() {
        drawerToggle.setDrawerIndicatorEnabled(true);
    }

    //For HOST Use ONLY, Acts as event handling for Host, mainly copied from reactor event handlers
    public void recieveMessage(Event message) {
        if(Boolean.parseBoolean(message.get("anon"))){
            messages.add(new Message(message.get("message"), true, ""));
        }else {
            messages.add(new Message(message.get("message"), false, message.get("name")));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void setUsername(final String username, boolean available){
        if(available) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.head_name)).setText(username);
                    ((TextView)findViewById(R.id.head_ipaddr)).setText(MainActivity.addr);
                    ((TextView)findViewById(R.id.head_port)).setText(String.valueOf(MainActivity.port));
                    MainActivity.username = username;
                    enableDrawer();
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Username already taken", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Implement features in new activities

        if (id == R.id.nav_post) {
            Intent postIntent = new Intent(getApplicationContext(),PostListActivity.class);
            startActivity(postIntent);
        } else if (id == R.id.nav_poll) {
            //not implemented
        } else if (id == R.id.nav_ask) {
            Intent askIntent = new Intent(getApplicationContext(),QuestionListActivity.class);
            startActivity(askIntent);
        } else if (id == R.id.nav_users) {
            //not implemented
        } else if (id == R.id.nav_permissions) {
            //not implemented
        }else if (id == R.id.nav_exit){
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    service.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        MainActivity.username = "";
        super.onDestroy();
    }
}
