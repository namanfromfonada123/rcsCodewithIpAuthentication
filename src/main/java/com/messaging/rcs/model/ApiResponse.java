package com.messaging.rcs.model;

public class ApiResponse {
	 private String transactionId;
	 private String state;
	 private String description;
	 private String pdu;
	 private String corelationid;

	 // Getter Methods 

	 public String getTransactionId() {
	  return transactionId;
	 }

	 public String getState() {
	  return state;
	 }

	 public String getDescription() {
	  return description;
	 }

	 // Setter Methods 

	 public void setTransactionId(String transactionId) {
	  this.transactionId = transactionId;
	 }

	 public void setState(String state) {
	  this.state = state;
	 }

	 public void setDescription(String description) {
	  this.description = description;
	 }

	public String getPdu() {
		return pdu;
	}

	public void setPdu(String pdu) {
		this.pdu = "";
	}

	public String getCorelationid() {
		if(corelationid==null)
			return "";
		else
		return corelationid;
	}

	public void setCorelationid(String corelationid) {
		this.corelationid = corelationid;
	}
	}