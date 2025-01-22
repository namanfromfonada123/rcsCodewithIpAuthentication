package com.messaging.rcs.model;

/**
 * 
 * @author RahulRajput
 *
 */
public class RetryInfo {

	private Long retryId;
	private String retryType;
	private Integer retryOnFail;
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

	public Integer getNoOfRetry() {
		return noOfRetry;
	}

	public void setNoOfRetry(Integer noOfRetry) {
		this.noOfRetry = noOfRetry;
	}
}
