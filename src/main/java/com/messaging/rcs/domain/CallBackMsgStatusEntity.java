package com.messaging.rcs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "demo_rcs.call_back_msg_status")
public class CallBackMsgStatusEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private Long id;
	@Column(name = "blacklist_msisdn")
	private String blacklistMsisdn;
	private String msgId;
	private String textMessage;
	private Date timestamp;
	private String displayText;
	private String data;
	private String event;
	private String status;
	private String botId;
	private String leadName;
	private String campaignName;
	private String dataSourceName;
	private String campaignType;
	private String templateCode;
	private String userName;
	private String isComplete;
	private Integer sendDlr;
	private String request;
	private String response;
	private String updateCallback;
	private String clickid;

	public String getClickid() {
		return clickid;
	}

	public void setClickid(String clickid) {
		this.clickid = clickid;
	}

	public String getUpdateCallback() {
		return updateCallback;
	}

	public void setUpdateCallback(String updateCallback) {
		this.updateCallback = updateCallback;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Integer getSendDlr() {
		return sendDlr;
	}

	public void setSendDlr(Integer sendDlr) {
		this.sendDlr = sendDlr;
	}

	public String getIsComplete() {
		return isComplete;
	}

	public void setIsComplete(String isComplete) {
		this.isComplete = isComplete;
	}

	public String getBotId() {
		return botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public String getLeadName() {
		return leadName;
	}

	public void setLeadName(String leadName) {
		this.leadName = leadName;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Column(name = "created_date")
	private String createdDate;

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBlacklistMsisdn() {
		return blacklistMsisdn;
	}

	public void setBlacklistMsisdn(String blacklistMsisdn) {
		this.blacklistMsisdn = blacklistMsisdn;
	}

	@Override
	public String toString() {
		return "CallBackMsgStatusEntity [id=" + id + ", blacklistMsisdn=" + blacklistMsisdn + ", msgId=" + msgId
				+ ", textMessage=" + textMessage + ", timestamp=" + timestamp + ", displayText=" + displayText
				+ ", data=" + data + ", event=" + event + ", status=" + status + ", botId=" + botId + ", leadName="
				+ leadName + ", campaignName=" + campaignName + ", dataSourceName=" + dataSourceName + ", campaignType="
				+ campaignType + ", templateCode=" + templateCode + ", userName=" + userName + ", isComplete="
				+ isComplete + ", sendDlr=" + sendDlr + ", request=" + request + ", response=" + response
				+ ", updateCallback=" + updateCallback + ", createdDate=" + createdDate + "]";
	}

}
