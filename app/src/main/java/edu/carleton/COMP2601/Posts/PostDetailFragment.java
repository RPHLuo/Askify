package edu.carleton.COMP2601.Posts;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import edu.carleton.COMP2601.CustomAdapter;
import edu.carleton.COMP2601.MainActivity;
import edu.carleton.COMP2601.Message;
import edu.carleton.COMP2601.R;

/**
 * A fragment representing a single Post detail screen.
 * This fragment is either contained in a {@link PostListActivity}
 * in two-pane mode (on tablets) or a {@link PostDetailActivity}
 * on handsets.
 */
public class PostDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */

    private static boolean anon;

    public static final String ID = "ID";

    /**
     * The dummy content this fragment is presenting.
     */
    private Post mItem;

    private CustomAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostDetailFragment() {
        anon = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ID)) {
            mItem = Posts.POSTS_MAP.get(getArguments().getInt("ID"));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                if(mItem != null) {
                    appBarLayout.setTitle(mItem.getTitle());
                }else{
                    appBarLayout.setTitle("New Post");
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView;
        if (mItem != null) {
            if(mItem.isCommentsEnabled()) {
                rootView = inflater.inflate(R.layout.post_detail_comments, container, false);
                ((TextView) rootView.findViewById(R.id.post_detail_message)).setText(mItem.getMessage());
                ((TextView) rootView.findViewById(R.id.post_detail_creator)).setText(mItem.getCreator());
                adapter = new CustomAdapter(getActivity(),(LinkedList)mItem.getComments());
                ((ListView) rootView.findViewById(R.id.post_detail_hasComments)).setAdapter(adapter);
                rootView.findViewById(R.id.post_detail_fabAnon).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(anon){
                            anon = false;
                            ((FloatingActionButton) rootView.findViewById(R.id.post_detail_fabAnon)).setImageResource(R.drawable.named);
                        }else {
                            anon = true;
                            ((FloatingActionButton) rootView.findViewById(R.id.post_detail_fabAnon)).setImageResource(R.drawable.anon);
                        }
                    }
                });
                rootView.findViewById(R.id.post_detail_fabAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String message = ((TextView)rootView.findViewById(R.id.post_detail_new_comment)).getText().toString();
                        ((TextView)rootView.findViewById(R.id.post_detail_new_comment)).setText("");
                        Message newComment = new Message(message, anon, MainActivity.username);
                        final Message closureComment = newComment;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    PostListActivity.ns.commment(closureComment, mItem.getId());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });

            }else{
                rootView = inflater.inflate(R.layout.post_detail_nocomments, container, false);
                ((TextView) rootView.findViewById(R.id.post_detail_message)).setText(mItem.getMessage());
                ((TextView) rootView.findViewById(R.id.post_detail_creator)).setText(mItem.getCreator());
            }
        }else{
            //form for a new post
            rootView = inflater.inflate(R.layout.post_detail_form, container, false);
            final View closureView = rootView;
            rootView.findViewById(R.id.post_detail_upload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String title = ((EditText)closureView.findViewById(R.id.post_detail_title)).getText().toString();
                            String details = ((EditText)closureView.findViewById(R.id.post_detail_details)).getText().toString();
                            boolean anon = ((CheckBox)closureView.findViewById(R.id.post_detail_anon)).isChecked();
                            boolean hasComments = ((CheckBox)closureView.findViewById(R.id.post_detail_hasComments)).isChecked();
                            Post newPost;
                            if(anon){
                                newPost = new Post(title, details, hasComments);
                            }else{
                                newPost = new Post(title, details, hasComments, MainActivity.username);
                            }
                            final Post closurePost = newPost;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        PostListActivity.ns.post(closurePost);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            getActivity().navigateUpTo(new Intent(getActivity(), PostListActivity.class));
                        }
                    });
                }
            });
        }

        return rootView;
    }
}
