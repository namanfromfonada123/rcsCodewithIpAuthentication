package com.messaging.rcs.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * 
 * @author RahulRajput
 *
 */
@Entity
@Table(name = "campaign")
public class CampaignEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long campaignId;
	private String campaignName;
	private long userId;
	private String campaignStatus;
	private String campaignType;
	private Date campaignStartTime;
	private Date campaignEndTime;
	@Column(columnDefinition = "integer default 0")
	private int isDeleted;
	private String description;
	private String channelPriorityScheme;
	private String rcsAgentId;
	private Long messageId;
	@Column(columnDefinition = "varchar(5000)")
	private String messageJson;
	@Column(name = "CREATED_BY")
	private String createdBy;
	@Column(name = "CREATED_DATE")
	private Date createdDate;
	@Column(name = "LAST_MODIFIED_BY")
	private String lastModifiedBy;
	@Column(name = "LAST_MODIFIED_DATE")
	private Date lastModifiedDate;
	private Long templateId;

	private String viResponse;
	private String deliveryStatus;
	private String sendToClient;
	private String rcspClientResponse;
	private Integer isSendToRmq;
	private String dataSourceName;
	private Long rcsMsgTypeId;
	private String msgCampaignType;
	private Long smsTemplateId;

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

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getViResponse() {
		return viResponse;
	}

	public void setViResponse(String viResponse) {
		this.viResponse = viResponse;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public String getSendToClient() {
		return sendToClient;
	}

	public void setSendToClient(String sendToClient) {
		this.sendToClient = sendToClient;
	}

	public String getRcspClientResponse() {
		return rcspClientResponse;
	}

	public void setRcspClientResponse(String rcspClientResponse) {
		this.rcspClientResponse = rcspClientResponse;
	}

	public Integer getIsSendToRmq() {
		return isSendToRmq;
	}

	public void setIsSendToRmq(Integer isSendToRmq) {
		this.isSendToRmq = isSendToRmq;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
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

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getRcsAgentId() {
		return rcsAgentId;
	}

	public void setRcsAgentId(String rcsAgentId) {
		this.rcsAgentId = rcsAgentId;
	}

	public String getChannelPriorityScheme() {
		return channelPriorityScheme;
	}

	public void setChannelPriorityScheme(String channelPriorityScheme) {
		this.channelPriorityScheme = channelPriorityScheme;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public String getMessageJson() {
		return messageJson;
	}

	public void setMessageJson(String messageJson) {
		this.messageJson = messageJson;
	}

	@Override
	public String toString() {
		return "CampaignEntity [campaignId=" + campaignId + ", campaignName=" + campaignName + ", userId=" + userId
				+ ", campaignStatus=" + campaignStatus + ", campaignType=" + campaignType + ", campaignStartTime="
				+ campaignStartTime + ", campaignEndTime=" + campaignEndTime + ", isDeleted=" + isDeleted
				+ ", description=" + description + ", channelPriorityScheme=" + channelPriorityScheme + ", rcsAgentId="
				+ rcsAgentId + ", messageId=" + messageId + ", messageJson=" + messageJson + ", createdBy=" + createdBy
				+ ", createdDate=" + createdDate + ", lastModifiedBy=" + lastModifiedBy + ", lastModifiedDate="
				+ lastModifiedDate + ", templateId=" + templateId + ", viResponse=" + viResponse + ", deliveryStatus="
				+ deliveryStatus + ", sendToClient=" + sendToClient + ", rcspClientResponse=" + rcspClientResponse
				+ ", isSendToRmq=" + isSendToRmq + ", dataSourceName=" + dataSourceName + ", rcsMsgTypeId="
				+ rcsMsgTypeId + ", msgCampaignType=" + msgCampaignType + ", smsTemplateId=" + smsTemplateId + "]";
	}

}
