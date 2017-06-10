package edu.carleton.COMP2601.Questions;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import edu.carleton.COMP2601.MainActivity;
import edu.carleton.COMP2601.Message;
import edu.carleton.COMP2601.Posts.PostListActivity;
import edu.carleton.COMP2601.R;

/**
 * A fragment representing a single Question detail screen.
 * This fragment is either contained in a {@link QuestionListActivity}
 * in two-pane mode (on tablets) or a {@link QuestionDetailActivity}
 * on handsets.
 */
public class QuestionDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */

    public static final String ID = "ID";

    private static boolean anon;

    /**
     * The dummy content this fragment is presenting.
     */
    private Question mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QuestionDetailFragment() {
        anon = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = Questions.QUESTIONS_MAP.get(Integer.valueOf(getArguments().getString(ID)));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                if(mItem!= null) {
                    appBarLayout.setTitle(mItem.getTitle());
                }else {
                    appBarLayout.setTitle("New Question");
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView;
        if(mItem!= null) {
            if (mItem.isAnswered()) {
                rootView = inflater.inflate(R.layout.question_detail_answer, container, false);
                ((TextView)rootView.findViewById(R.id.question_detail_answer)).setText(mItem.getAnswer().getMessage());
                if(mItem.getAnswer().isAnon()) {
                    ((TextView) rootView.findViewById(R.id.question_detail_answerer)).setText("anonymous");
                }else {
                    ((TextView) rootView.findViewById(R.id.question_detail_answerer)).setText(mItem.getAnswer().getName());
                }
            } else {
                rootView = inflater.inflate(R.layout.question_detail_noanswer, container, false);

                rootView.findViewById(R.id.question_detail_fabAnon).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(anon){
                            anon = false;
                            ((FloatingActionButton) rootView.findViewById(R.id.question_detail_fabAnon)).setImageResource(R.drawable.named);
                        }else {
                            anon = true;
                            ((FloatingActionButton) rootView.findViewById(R.id.question_detail_fabAnon)).setImageResource(R.drawable.anon);
                        }
                    }
                });

                rootView.findViewById(R.id.question_detail_fabAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String answer = ((EditText)rootView.findViewById(R.id.question_detail_answer)).getText().toString();
                        Message answerMessage = new Message(answer,anon,MainActivity.username);
                        final Message closureMessage = answerMessage;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    QuestionListActivity.ns.answer(closureMessage, mItem.getId());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        getActivity().navigateUpTo(new Intent(getActivity(), PostListActivity.class));
                    }
                });
            }
            ((TextView) rootView.findViewById(R.id.question_detail_question)).setText(mItem.getQuestion());
            if(mItem.getAsker().equals("")){
                ((TextView) rootView.findViewById(R.id.question_detail_creator)).setText("anonymous");
            }else {
                ((TextView) rootView.findViewById(R.id.question_detail_creator)).setText(mItem.getAsker());
            }
        }else {
            rootView = inflater.inflate(R.layout.question_detail_form, container, false);
            final View closureView = rootView;
            rootView.findViewById(R.id.question_detail_upload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = ((EditText)closureView.findViewById(R.id.question_detail_title)).getText().toString();
                    String question = ((EditText)closureView.findViewById(R.id.question_detail_question)).getText().toString();
                    boolean anon = ((CheckBox)closureView.findViewById(R.id.question_detail_anon)).isChecked();
                    Question newQuestion;
                    if(anon){
                        newQuestion = new Question(title,question);
                    }else{
                        newQuestion = new Question(title,question,MainActivity.username);
                    }
                    final Question closureQuestion = newQuestion;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                QuestionListActivity.ns.question(closureQuestion);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    getActivity().navigateUpTo(new Intent(getActivity(), PostListActivity.class));
                }
            });
        }
        return rootView;
    }
}
