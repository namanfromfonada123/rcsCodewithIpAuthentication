package com.messaging.rcs.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by sbsingh on Nov/21/2021.
 */
@Entity
@Table(name = "message_response_logs")
public class MessageResponseLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long leadId;
    private String responseType;
    private String msisdn;
    private String response;
    private Date sendTime;
    private Date recvTime;
    private String completeResponse;

    public MessageResponseLogs() {
    }

    public String getCompleteResponse() {
        return completeResponse;
    }

    public void setCompleteResponse(String completeResponse) {
        this.completeResponse = completeResponse;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getRecvTime() {
        return recvTime;
    }

    public void setRecvTime(Date recvTime) {
        this.recvTime = recvTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
