package com.messaging.rcs.domain;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by sbsingh on Oct/31/2021.
 */
@Entity
@Table(name = "ATTEMPT_INFO")
public class AttemptInfoEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ATTEMPT_ID")
    private Long attempId;

    private Long retryId;
    private Integer round;
    private Integer qualifiedCount;
    private Integer processedNumCount;
    private String status;

    public Long getAttempId() {
        return attempId;
    }


    public Long getRetryId() {
        return retryId;
    }

    public void setRetryId(Long retryId) {
        this.retryId = retryId;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Integer getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(Integer qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public Integer getProcessedNumCount() {
        return processedNumCount;
    }

    public void setProcessedNumCount(Integer processedNumCount) {
        this.processedNumCount = processedNumCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
