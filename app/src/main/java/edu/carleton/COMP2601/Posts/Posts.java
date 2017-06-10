package edu.carleton.COMP2601.Posts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Luo on 2017-03-11.
 */

public class Posts {
    public static List<Post> POSTS = new ArrayList<>();

    public static Map<Integer,Post> POSTS_MAP = new HashMap<>();

    public static void setPOSTS(List<Post> posts){
        POSTS = posts;
        POSTS_MAP = new HashMap<>();
        for (Post p:POSTS){
            POSTS_MAP.put(p.getId(),p);
        }
    }

    public static void addItem(Post item) {
        POSTS.add(item);
        POSTS_MAP.put(item.getId(),item);
    }

    public static void removeItem(){

    }
}
