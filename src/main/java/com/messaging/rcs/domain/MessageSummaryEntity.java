package com.messaging.rcs.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by sbsingh on Oct/31/2021.
 */
@Entity
@Table(name = "message_summary")
public class MessageSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long mid;
    private long leadId;
    private long attemptId;
    private Date callStartDate;
    private int totalMessages;
    private int delieveredMessages;
    private int failedMessages;

    public long getMid() {
        return mid;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public long getLeadId() {
        return leadId;
    }

    public void setLeadId(long leadId) {
        this.leadId = leadId;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(long attemptId) {
        this.attemptId = attemptId;
    }

    public Date getCallStartDate() {
        return callStartDate;
    }

    public void setCallStartDate(Date callStartDate) {
        this.callStartDate = callStartDate;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getDelieveredMessages() {
        return delieveredMessages;
    }

    public void setDelieveredMessages(int delieveredMessages) {
        this.delieveredMessages = delieveredMessages;
    }

    public int getFailedMessages() {
        return failedMessages;
    }

    public void setFailedMessages(int failedMessages) {
        this.failedMessages = failedMessages;
    }
}
