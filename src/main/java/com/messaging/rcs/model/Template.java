package com.messaging.rcs.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "template")
public class Template {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	private String templateCode;

	private String templateJson;

	private String templateType;

	private String templateMsgType;

	private Long templateUserId;

	private String templateCustomParam;

	private String inserttime;

	private Integer status;

	private String botId;
	private String sms_dlt_principle_id;
	private String sms_dlt_content_id;
	private String sms_senderId;
	private String sms_contentId;
	private String sms_content;
	private String msgType;
	private String rcsMsgTypeId;
	@Column(name = "operator_response")
	private String operatorResponse;
	@Column(name = "approve_response")
	private String approveResponse;
	private String tmplTransactionMode;
	private Long userBotMappingId;
	private String videoId;
	private String mediaUrl;
	private String thumbnailUrl;
	private String operator;
	private String templateMode;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getTemplateMode() {
		return templateMode;
	}

	public void setTemplateMode(String templateMode) {
		this.templateMode = templateMode;
	}

	public String getTmplTransactionMode() {
		return tmplTransactionMode;
	}

	public void setTmplTransactionMode(String tmplTransactionMode) {
		this.tmplTransactionMode = tmplTransactionMode;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public Long getUserBotMappingId() {
		return userBotMappingId;
	}

	public void setUserBotMappingId(Long userBotMappingId) {
		this.userBotMappingId = userBotMappingId;
	}

	public String getOperatorResponse() {
		return operatorResponse;
	}

	public void setOperatorResponse(String operatorResponse) {
		this.operatorResponse = operatorResponse;
	}

	public String getApproveResponse() {
		return approveResponse;
	}

	public void setApproveResponse(String approveResponse) {
		this.approveResponse = approveResponse;
	}

	public String getSms_dlt_principle_id() {
		return sms_dlt_principle_id;
	}

	public void setSms_dlt_principle_id(String sms_dlt_principle_id) {
		this.sms_dlt_principle_id = sms_dlt_principle_id;
	}

	public String getSms_dlt_content_id() {
		return sms_dlt_content_id;
	}

	public void setSms_dlt_content_id(String sms_dlt_content_id) {
		this.sms_dlt_content_id = sms_dlt_content_id;
	}

	public String getSms_senderId() {
		return sms_senderId;
	}

	public void setSms_senderId(String sms_senderId) {
		this.sms_senderId = sms_senderId;
	}

	public String getSms_contentId() {
		return sms_contentId;
	}

	public void setSms_contentId(String sms_contentId) {
		this.sms_contentId = sms_contentId;
	}

	public String getSms_content() {
		return sms_content;
	}

	public void setSms_content(String sms_content) {
		this.sms_content = sms_content;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getRcsMsgTypeId() {
		return rcsMsgTypeId;
	}

	public void setRcsMsgTypeId(String rcsMsgTypeId) {
		this.rcsMsgTypeId = rcsMsgTypeId;
	}

	public String getBotId() {
		return this.botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public Long getTemplateUserId() {
		return this.templateUserId;
	}

	public String getInserttime() {
		return this.inserttime;
	}

	public void setInserttime(String inserttime) {
		this.inserttime = inserttime;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTemplateCustomParam() {
		return this.templateCustomParam;
	}

	public void setTemplateCustomParam(String templateCustomParam) {
		this.templateCustomParam = templateCustomParam;
	}

	public void setTemplateUserId(Long templateUserId) {
		this.templateUserId = templateUserId;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTemplateCode() {
		return this.templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String getTemplateJson() {
		return this.templateJson;
	}

	public void setTemplateJson(String templateJson) {
		this.templateJson = templateJson;
	}

	public String getTemplateType() {
		return this.templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getTemplateMsgType() {
		return this.templateMsgType;
	}

	public void setTemplateMsgType(String templateMsgType) {
		this.templateMsgType = templateMsgType;
	}

	@Override
	public String toString() {
		return "Template [id=" + id + ", templateCode=" + templateCode + ", templateJson=" + templateJson
				+ ", templateType=" + templateType + ", templateMsgType=" + templateMsgType + ", templateUserId="
				+ templateUserId + ", templateCustomParam=" + templateCustomParam + ", inserttime=" + inserttime
				+ ", status=" + status + ", botId=" + botId + ", sms_dlt_principle_id=" + sms_dlt_principle_id
				+ ", sms_dlt_content_id=" + sms_dlt_content_id + ", sms_senderId=" + sms_senderId + ", sms_contentId="
				+ sms_contentId + ", sms_content=" + sms_content + ", msgType=" + msgType + ", rcsMsgTypeId="
				+ rcsMsgTypeId + ", operatorResponse=" + operatorResponse + ", approveResponse=" + approveResponse
				+ "]";
	}
}
