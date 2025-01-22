package com.messaging.rcs.domain;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by sbsingh on 3/22/19.
 */
@Entity
@Table(name = "RETRY_INFO")
public class RetryInfoEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "RETRY_ID")
    private Long retryId;
    @Column(name = "RETRY_TYPE")
    private String retryType;
    @Column(name = "RETRY_ON_FAIL")
    private Integer retryOnFail;
    @Column(name = "RETRY_TIME_ON_FAIL")
    private Integer retryTimeOnFail;
    @Column(name = "RETRY_ON_BUSY")
    private Integer retryOnBusy;
    @Column(name = "RETRY_TIME_ON_BUSY")
    private Integer retryTimeOnBusy;
    @Column(name = "RETRY_ON_ANS")
    private Integer retryOnAns;
    @Column(name = "RETRY_TIME_ON_ANS")
    private Integer retryTimeOnAns;
    @Column(name = "RETRY_ON_NO_ANS")
    private Integer retryOnNoAns;
    @Column(name = "RETRY_TIME_ON_NO_ANS")
    private Integer retryTimeOnNoAns;
    @Column(name = "NO_OF_RETRY")
    private Integer noOfRetry;

    public Long getRetryId() {
        return retryId;
    }

    public void setRetryId(Long retryId) {
        this.retryId = retryId;
    }

    public String getRetryType() {
        return retryType;
    }

    public void setRetryType(String retryType) {
        this.retryType = retryType;
    }

    public Integer getRetryOnFail() {
        return retryOnFail;
    }

    public void setRetryOnFail(Integer retryOnFail) {
        this.retryOnFail = retryOnFail;
    }

    public Integer getRetryTimeOnFail() {
        return retryTimeOnFail;
    }

    public void setRetryTimeOnFail(Integer retryTimeOnFail) {
        this.retryTimeOnFail = retryTimeOnFail;
    }

    public Integer getRetryOnBusy() {
        return retryOnBusy;
    }

    public void setRetryOnBusy(Integer retryOnBusy) {
        this.retryOnBusy = retryOnBusy;
    }

    public Integer getRetryTimeOnBusy() {
        return retryTimeOnBusy;
    }

    public void setRetryTimeOnBusy(Integer retryTimeOnBusy) {
        this.retryTimeOnBusy = retryTimeOnBusy;
    }

    public Integer getRetryOnAns() {
        return retryOnAns;
    }

    public void setRetryOnAns(Integer retryOnAns) {
        this.retryOnAns = retryOnAns;
    }

    public Integer getRetryTimeOnAns() {
        return retryTimeOnAns;
    }

    public void setRetryTimeOnAns(Integer retryTimeOnAns) {
        this.retryTimeOnAns = retryTimeOnAns;
    }

    public Integer getRetryOnNoAns() {
        return retryOnNoAns;
    }

    public void setRetryOnNoAns(Integer retryOnNoAns) {
        this.retryOnNoAns = retryOnNoAns;
    }

    public Integer getRetryTimeOnNoAns() {
        return retryTimeOnNoAns;
    }

    public void setRetryTimeOnNoAns(Integer retryTimeOnNoAns) {
        this.retryTimeOnNoAns = retryTimeOnNoAns;
    }

    public Integer getNoOfRetry() {
        return noOfRetry;
    }

    public void setNoOfRetry(Integer noOfRetry) {
        this.noOfRetry = noOfRetry;
    }

    @Override
    public String toString() {
        return "RetryInfoEntity{" +
                "retryId=" + retryId +
                ", retryType='" + retryType + '\'' +
                ", retryOnFail=" + retryOnFail +
                ", retryTimeOnFail=" + retryTimeOnFail +
                ", retryOnBusy=" + retryOnBusy +
                ", retryTimeOnBusy=" + retryTimeOnBusy +
                ", retryOnAns=" + retryOnAns +
                ", retryTimeOnAns=" + retryTimeOnAns +
                ", retryOnNoAns=" + retryOnNoAns +
                ", retryTimeOnNoAns=" + retryTimeOnNoAns +
                ", noOfRetry=" + noOfRetry +
                '}';
    }
}
