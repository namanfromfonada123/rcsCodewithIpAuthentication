package com.messaging.rcs.rbm.model;

/**
 * Created by sbsingh on Nov/22/2021.
 */

/**
 * {
 *   "senderPhoneNumber": "+919811487154",
 *   "messageId": "Ms-cRHWj6NTCqHcLFvcfIvbg",
 *   "sendTime": "2021-11-21T09:23:17.465483Z",
 *   "suggestionResponse": {
 *     "postbackData": "suggestion_1",
 *     "text": "Suggestion 1",
 *     "type": "REPLY"
 *   },
 *   "agentId": "shivtel@rbm.goog"
 * }
 */
public class PubSubReplyResponse {
    private String senderPhoneNumber;
    private String messageId;
    private String sendTime;
    private SuggestionResponse suggestionResponse;
    private String agentId;

    public String getSenderPhoneNumber() {
        return senderPhoneNumber;
    }

    public void setSenderPhoneNumber(String senderPhoneNumber) {
        this.senderPhoneNumber = senderPhoneNumber;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public SuggestionResponse getSuggestionResponse() {
        return suggestionResponse;
    }

    public void setSuggestionResponse(SuggestionResponse suggestionResponse) {
        this.suggestionResponse = suggestionResponse;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
