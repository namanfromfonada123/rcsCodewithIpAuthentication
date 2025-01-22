package com.messaging.rcs.consumer;

import java.io.Serializable;

public class WebRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String msgid;
	private String templatecode;
	private String boatid;
	private String customeparam;
	private String customeparamValue;
	private String usercontact;

	public String getUsercontact() {
		return usercontact;
	}

	public void setUsercontact(String usercontact) {
		this.usercontact = usercontact;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getTemplatecode() {
		return templatecode;
	}

	public void setTemplatecode(String templatecode) {
		this.templatecode = templatecode;
	}

	public String getBoatid() {
		return boatid;
	}

	public void setBoatid(String boatid) {
		this.boatid = boatid;
	}

	public String getCustomeparam() {
		return customeparam;
	}

	public void setCustomeparam(String customeparam) {
		this.customeparam = customeparam;
	}

	public String getCustomeparamValue() {
		return customeparamValue;
	}

	public void setCustomeparamValue(String customeparamValue) {
		this.customeparamValue = customeparamValue;
	}

	@Override
	public String toString() {
		return "WebRequest [msgid=" + msgid + ", templatecode=" + templatecode + ", boatid=" + boatid
				+ ", customeparam=" + customeparam + ", customeparamValue=" + customeparamValue + ", usercontact="
				+ usercontact + "]";
	}

}
