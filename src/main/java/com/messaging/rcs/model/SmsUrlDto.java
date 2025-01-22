package com.messaging.rcs.model;

import java.io.Serializable;

public class SmsUrlDto implements Serializable {

	// @NotBlank(message = "Title is mandatory")
	public String title;
	// @NotBlank(message = "URL is mandatory")
	public String url;
	private String messageType;

	public Integer clientId;

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Integer getClientId() {
		return clientId;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "SmsUrlDto [title=" + title + ", url=" + url + ", messageType=" + messageType + ", clientId=" + clientId
				+ "]";
	}

}
