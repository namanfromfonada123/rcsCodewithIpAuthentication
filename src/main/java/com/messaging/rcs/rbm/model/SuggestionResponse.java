package com.messaging.rcs.rbm.model;

/**
 * Created by sbsingh on Nov/22/2021.
 */
/*
"suggestionResponse": {
 *     "postbackData": "suggestion_1",
 *     "text": "Suggestion 1",
 *     "type": "REPLY"
 *   }
 */
public class SuggestionResponse {
    private String postbackData;
    private String text;
    private String type;

    public String getPostbackData() {
        return postbackData;
    }

    public void setPostbackData(String postbackData) {
        this.postbackData = postbackData;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
