package com.messaging.rcs.email.model;

public class EmailRequestModel {

	private String emailFrom;
	private String emailTo;
	private String emailCC;
	private String emailBCC;
	private String eventType;

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailCC() {
		return emailCC;
	}

	public void setEmailCC(String emailCC) {
		this.emailCC = emailCC;
	}

	public String getEmailBCC() {
		return emailBCC;
	}

	public void setEmailBCC(String emailBCC) {
		this.emailBCC = emailBCC;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	@Override
	public String toString() {
		return "EmailRequestModel{" +
				"emailFrom='" + emailFrom + '\'' +
				", emailTo='" + emailTo + '\'' +
				", emailCC='" + emailCC + '\'' +
				", emailBCC='" + emailBCC + '\'' +
				", eventType='" + eventType + '\'' +
				'}';
	}
}
