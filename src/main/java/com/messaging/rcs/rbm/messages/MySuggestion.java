package com.messaging.rcs.rbm.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Key;
import com.google.api.services.rcsbusinessmessaging.v1.model.SuggestedAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.SuggestedReply;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbsingh on Dec/26/2021.
 */
public class MySuggestion {

    private MySuggestedAction action;

    private SuggestedReply reply;

    @JsonProperty("action")
    public MySuggestedAction getAction() {
        return action;
    }

    public void setAction(MySuggestedAction action) {
        this.action = action;
    }

    @JsonProperty("reply")
    public SuggestedReply getReply() {
        return reply;
    }

    public void setReply(SuggestedReply reply) {
        this.reply = reply;
    }

}
