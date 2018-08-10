package com.parabit.beacon.feedback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by williamsnyder on 11/27/17.
 */

public class Feedback {

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_PROBLEM = "problem";

    private String username;
    private String feedback;
    private String category;
    private Map<String,String> context;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public static Feedback createGeneral(String message, String username) {
        Feedback feedback = new Feedback();
        feedback.setUsername(username);
        feedback.setFeedback(message);
        feedback.setCategory(CATEGORY_GENERAL);
        feedback.setContext(new HashMap<String, String>());
        return feedback;
    }

    public static Feedback createProblem(String message, String username) {
        Feedback feedback = new Feedback();
        feedback.setUsername(username);
        feedback.setFeedback(message);
        feedback.setCategory(CATEGORY_PROBLEM);
        return feedback;
    }
}
