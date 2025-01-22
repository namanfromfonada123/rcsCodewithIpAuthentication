package com.messaging.rcs.rbm.model;

/**
 * Created by sbsingh on Nov/22/2021.
 */

/**
 {
 "senderPhoneNumber": "+919650095032",
 "eventType": "DELIVERED",
 "eventId": "MxJdOFFXN4QBmdGn1GLbqnpw",
 "messageId": "bd6fa8b5-66bb-4f8b-a876-6827a6bc86f2",
 "sendTime": "2021-11-28T06:40:09.328065Z",
 "agentId": "shivtel@rbm.goog"
 }
 */
public class PubSubDelieveryResponse {
    private String senderPhoneNumber;
    private String eventType;
    private String eventId;
    private String messageId;
    private String sendTime;
    private String agentId;


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
