package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created By Rahul Kumar 12-01-2024
 */
@Entity
@Table(name = "rcs_msg_type")
public class RcsMsgTypeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long messageId;
	@Column(columnDefinition = "varchar(255)")
	private String messageType;
	@Column(columnDefinition = "varchar(255)")
	private String description;
	private Integer msgTypeId;

	public Integer getMsgTypeId() {
		return msgTypeId;
	}

	public void setMsgTypeId(Integer msgTypeId) {
		this.msgTypeId = msgTypeId;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "RcsMsgTypeEntity [messageId=" + messageId + ", messageType=" + messageType + ", description="
				+ description + ", msgTypeId=" + msgTypeId + "]";
	}

}
