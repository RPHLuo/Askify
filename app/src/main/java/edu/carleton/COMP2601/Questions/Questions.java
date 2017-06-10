package edu.carleton.COMP2601.Questions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.carleton.COMP2601.Posts.Post;

/**
 * Created by Luo on 2017-04-07.
 */

public class Questions {
    public static List<Question> QUESTIONS = new ArrayList<>();

    public static Map<Integer,Question> QUESTIONS_MAP = new HashMap<>();

    public static void setQUESTIONS(List<Question> posts){
        QUESTIONS = posts;
        QUESTIONS_MAP = new HashMap<>();
        for (Question p:QUESTIONS){
            QUESTIONS_MAP.put(p.getId(),p);
        }
    }

    public static void addItem(Question item) {
        QUESTIONS.add(item);
        QUESTIONS_MAP.put(item.getId(),item);
    }

    public static void removeItem() {

    }
}
