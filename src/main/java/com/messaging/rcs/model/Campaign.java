package com.messaging.rcs.model;

import java.util.Date;

/**
 * 
 * @author RahulRajput 26-05-2023
 *
 */
public class Campaign extends BaseModel {

	private Long campaignId;
	private String campaignName;
	private long userId;
	private String campaignStatus;
	private Date campaignStartTime;
	private Date campaignEndTime;
	private int isDeleted;
	private String description;
	private String channelPriorityScheme;
	private String usageType;
	private Long messageId;
	private Long templateId;
	private Long smsTemplateId;
	private Integer leadCount;
	private String templateName;
	private String campaignType;
	private String dataSourceName;
	private String msgCampaignType;
	private Long rcsMsgTypeId;

	public Long getSmsTemplateId() {
		return smsTemplateId;
	}

	public void setSmsTemplateId(Long smsTemplateId) {
		this.smsTemplateId = smsTemplateId;
	}

	public String getMsgCampaignType() {
		return msgCampaignType;
	}

	public void setMsgCampaignType(String msgCampaignType) {
		this.msgCampaignType = msgCampaignType;
	}

	public Long getRcsMsgTypeId() {
		return rcsMsgTypeId;
	}

	public void setRcsMsgTypeId(Long rcsMsgTypeId) {
		this.rcsMsgTypeId = rcsMsgTypeId;
	}

	/*
	 * public String getMsgCampaignType() { return msgCampaignType; }
	 * 
	 * public void setMsgCampaignType(String msgCampaignType) { this.msgCampaignType
	 * = msgCampaignType; }
	 */
	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public Integer getLeadCount() {
		return leadCount;
	}

	public void setLeadCount(Integer leadCount) {
		this.leadCount = leadCount;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public Long getCampaignId() {
		return campaignId;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getCampaignStatus() {
		return campaignStatus;
	}

	public void setCampaignStatus(String campaignStatus) {
		this.campaignStatus = campaignStatus;
	}

	public Date getCampaignStartTime() {
		return campaignStartTime;
	}

	public void setCampaignStartTime(Date campaignStartTime) {
		this.campaignStartTime = campaignStartTime;
	}

	public Date getCampaignEndTime() {
		return campaignEndTime;
	}

	public void setCampaignEndTime(Date campaignEndTime) {
		this.campaignEndTime = campaignEndTime;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUsageType() {
		return usageType;
	}

	public void setUsageType(String usageType) {
		this.usageType = usageType;
	}

	public String getChannelPriorityScheme() {
		return channelPriorityScheme;
	}

	public void setChannelPriorityScheme(String channelPriorityScheme) {
		this.channelPriorityScheme = channelPriorityScheme;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	@Override
	public String toString() {
		return "Campaign [campaignId=" + campaignId + ", campaignName=" + campaignName + ", userId=" + userId
				+ ", campaignStatus=" + campaignStatus + ", campaignStartTime=" + campaignStartTime
				+ ", campaignEndTime=" + campaignEndTime + ", isDeleted=" + isDeleted + ", description=" + description
				+ ", channelPriorityScheme=" + channelPriorityScheme + ", usageType=" + usageType + ", messageId="
				+ messageId + ", templateId=" + templateId + ", smsTemplateId=" + smsTemplateId + ", leadCount="
				+ leadCount + ", templateName=" + templateName + ", campaignType=" + campaignType + ", dataSourceName="
				+ dataSourceName + ", msgCampaignType=" + msgCampaignType + ", rcsMsgTypeId=" + rcsMsgTypeId + "]";
	}

}
