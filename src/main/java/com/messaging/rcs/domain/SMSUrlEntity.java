package com.messaging.rcs.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sms_url")
public class SMSUrlEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "smsUrlId")
	private Long smsUrlId;
	@Column(name = "title")
	private String title;
	@Column(name = "url")
	private String url;
	@Column(name = "trackingId")
	private String trackingId;
	@Column(name = "createdBy")
	private String createdBy;
	@Column(name = "isActive")
	private String isActive;
	@Column(name = "createdDate")
	private String createdDate;
	@Column(name = "client_id")
	private Integer clientId;
	private String messageType;

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Long getSmsUrlId() {
		return smsUrlId;
	}

	public void setSmsUrlId(Long smsUrlId) {
		this.smsUrlId = smsUrlId;
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

	public String getTrackingId() {
		return trackingId;
	}

	public void setTrackingId(String trackingId) {
		this.trackingId = trackingId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getClientId() {
		return clientId;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "SMSUrlEntity [smsUrlId=" + smsUrlId + ", title=" + title + ", url=" + url + ", trackingId=" + trackingId
				+ ", createdBy=" + createdBy + ", isActive=" + isActive + ", createdDate=" + createdDate + ", clientId="
				+ clientId + ", messageType=" + messageType + "]";
	}

}
