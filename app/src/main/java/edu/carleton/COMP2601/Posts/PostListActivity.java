package edu.carleton.COMP2601.Posts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.reflect.TypeToken;

import edu.carleton.COMP2601.MainActivity;
import edu.carleton.COMP2601.Message;
import edu.carleton.COMP2601.NetworkService;
import edu.carleton.COMP2601.R;
import edu.carleton.COMP2601.communication.Event;
import edu.carleton.COMP2601.communication.EventHandler;
import edu.carleton.COMP2601.communication.Reactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Posts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PostListActivity extends AppCompatActivity {

    public static NetworkService ns;
    public static Reactor reactor;

    public static PostListActivity postListActivity;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        setupListView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.post_list_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PostDetailActivity.class);
                //give null id to create a new post
                intent.putExtra(PostDetailFragment.ID,-1);
                startActivity(intent);
            }
        });

        if (findViewById(R.id.post_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        setup();
    }

    public void setupListView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final View recyclerView = findViewById(R.id.post_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);

                ((SwipeRefreshLayout)findViewById(R.id.post_list_refresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        ((RecyclerView) recyclerView).getAdapter().notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),String.valueOf(Posts.POSTS.size()),Toast.LENGTH_LONG).show();
                        ((SwipeRefreshLayout) findViewById(R.id.post_list_refresh)).setRefreshing(false);
                    }
                });
            }
        });
    }

    private void setup() {
        postListActivity = this;
        if (MainActivity.isHost) {
            ns = MainActivity.hs;
        } else {
            ns = MainActivity.cs;
        }
        reactor = MainActivity.reactor;
        setupListView();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(Posts.POSTS));
    }

    //Host callbacks
    public void recievePost(Post p){
        Posts.addItem(p);
    }
    //takes event to make it simpler instead of passing a postid and comment
    public void recieveComment(Event event){
        Posts.POSTS_MAP.get(Integer.parseInt(event.get("post_id"))).comment(MainActivity.gson.fromJson(event.get("comment"),Message.class));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private List<Post> mValues;

        public SimpleItemRecyclerViewAdapter(List<Post> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(String.valueOf(holder.mItem.getId()));
            System.out.println(holder.mItem.getId());
            holder.mContentView.setText(holder.mItem.getTitle());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(PostDetailFragment.ID, String.valueOf(holder.mItem.getId()));
                        PostDetailFragment fragment = new PostDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.post_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, PostDetailActivity.class);
                        intent.putExtra(PostDetailFragment.ID, holder.mItem.getId());
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public Post mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.post_title);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
