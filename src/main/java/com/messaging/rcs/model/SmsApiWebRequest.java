package com.messaging.rcs.model;

import java.io.Serializable;

public class SmsApiWebRequest implements Serializable {
	/**
		 * 
		 */
	private static long serialVersionUID = 1L;
	private String username;
	private String password;
	private String From;
	private String templateid;
	private String corelationid;
	private String msisdn;
	private String message;
	private String unicode;

	private String params;

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFrom() {
		return From;
	}

	public void setFrom(String from) {
		From = from;
	}

	public String getTemplateid() {
		return templateid;
	}

	public void setTemplateid(String templateid) {
		this.templateid = templateid;
	}

	public String getCorelationid() {
		return corelationid;
	}

	public void setCorelationid(String corelationid) {
		this.corelationid = corelationid;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUnicode() {
		return unicode;
	}

	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}

	@Override
	public String toString() {
		return "SmsApiWebRequest [username=" + username + ", password=" + password + ", From=" + From + ", templateid="
				+ templateid + ", corelationid=" + corelationid + ", msisdn=" + msisdn + ", message=" + message
				+ ", unicode=" + unicode + ", params=" + params + "]";
	}

}