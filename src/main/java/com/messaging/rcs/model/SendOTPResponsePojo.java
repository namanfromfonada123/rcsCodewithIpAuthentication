package com.messaging.rcs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SendOTPResponsePojo {
	private String transactionId;
	private String state;
	private String description;
	private String pdu;
	private String corelationid;
	private String otp;
	
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPdu() {
		return pdu;
	}
	public void setPdu(String pdu) {
		this.pdu = pdu;
	}
	public String getCorelationid() {
		return corelationid;
	}
	public void setCorelationid(String corelationid) {
		this.corelationid = corelationid;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	@Override
	public String toString() {
		return "SendOTPResponsePojo [transactionId=" + transactionId + ", state=" + state + ", description="
				+ description + ", pdu=" + pdu + ", corelationid=" + corelationid + ", otp=" + otp + "]";
	}
	
	
	}
