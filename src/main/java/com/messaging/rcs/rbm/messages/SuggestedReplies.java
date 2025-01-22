package com.messaging.rcs.rbm.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbsingh on Nov/20/2021.
 */
public class SuggestedReplies {

    private String textMessage;
    private List<MySuggestion> suggestions = new ArrayList<>();
    List<String> suggestedReplies =  new ArrayList<>();


    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public List<MySuggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<MySuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getSuggestedReplies() {
        return suggestedReplies;
    }

    public void setSuggestedReplies(List<String> suggestedReplies) {
        this.suggestedReplies = suggestedReplies;
    }
}
