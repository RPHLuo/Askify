package edu.carleton.COMP2601.Questions;

import edu.carleton.COMP2601.Message;

/**
 * Created by Luo on 2017-04-07.
 */

public class Question {
    private String title;
    private String question;
    private String asker;
    private Message answer;
    private int rating;
    private boolean answered;

    public boolean isAnswered() {
        return answered;
    }

    public int getId() {
        return id;
    }

    private int id;
    private static int ID_COUNTER = 1;

    public Question(String title, String question, String asker){
        this.title = title;
        this.question = question;
        this.asker = asker;
        this.answered = false;
        this.id = ID_COUNTER++;
    }
    public Question(String title, String question){
        this.title = title;
        this.question = question;
        this.asker = "";
        this.answered = false;
        this.id = ID_COUNTER++;
    }

    public void answer(Message answer){
        if(!answered) {
            rating = 0;
            this.answer = answer;
            answered = true;
        }
    }

    public void upvote(){
        rating++;
    }

    public void downvote(){
        rating--;
    }

    public String getQuestion() {
        return question;
    }

    public String getAsker() {
        return asker;
    }

    public Message getAnswer() {
        return answer;
    }

    public String getTitle() {
        return title;
    }

    public int getRating() {
        return rating;
    }
}
