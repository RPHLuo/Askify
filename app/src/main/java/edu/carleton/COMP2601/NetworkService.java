package edu.carleton.COMP2601;

import java.io.IOException;

import edu.carleton.COMP2601.Posts.Post;
import edu.carleton.COMP2601.Questions.Question;

/**
 * Created by Luo on 2017-03-19.
 */

public interface NetworkService {
    void setUsername(String name) throws IOException;
    void message(String message, boolean isAnonymous) throws IOException;
    void post(Post newPost) throws IOException;
    void catchupPosts() throws IOException;
    void catchupQuestions() throws IOException;
    void commment(Message comment, int pid) throws IOException;
    void question(Question question) throws IOException;
    void answer(Message answer, int qid) throws IOException;
    void disconnect() throws IOException;
}
