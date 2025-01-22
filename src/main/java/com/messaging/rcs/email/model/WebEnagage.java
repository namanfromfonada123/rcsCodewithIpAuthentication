package com.messaging.rcs.email.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.model.LeadInfo;
import com.messaging.rcs.schedular.TokenPojo;

public class WebEnagage implements Serializable {
	/**
	 * 
	 */
	private static long serialVersionUID = 1L;
	private String leadName;
	private Long userId;
	private Long campaignId;
	private WebEngageSchedule leadSchedule;
	private String msgId;
	private boolean isDnd;
	private List<WebEngageLeadInfoDetail> leadInfoDetails = new ArrayList<>();
	private String uKey;

	private String checkRcs;

	private LeadInfo leadInfo;
	private TokenPojo token;
	private UserEntity userEntity;
	private List<LeadInfoDetailEntity> leadInfoDetailEntities = new ArrayList<>();

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static void setSerialversionuid(long serialversionuid) {
		serialVersionUID = serialversionuid;
	}

	public String getCheckRcs() {
		return checkRcs;
	}

	public void setCheckRcs(String checkRcs) {
		this.checkRcs = checkRcs;
	}

	public List<LeadInfoDetailEntity> getLeadInfoDetailEntities() {
		return leadInfoDetailEntities;
	}

	public void setLeadInfoDetailEntities(List<LeadInfoDetailEntity> leadInfoDetailEntities) {
		this.leadInfoDetailEntities = leadInfoDetailEntities;
	}

	public String getuKey() {
		return uKey;
	}

	public void setuKey(String uKey) {
		this.uKey = uKey;
	}

	public String getLeadName() {
		return leadName;
	}

	public void setLeadName(String leadName) {
		this.leadName = leadName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public WebEngageSchedule getLeadSchedule() {
		return leadSchedule;
	}

	public void setLeadSchedule(WebEngageSchedule leadSchedule) {
		this.leadSchedule = leadSchedule;
	}

	public List<WebEngageLeadInfoDetail> getLeadInfoDetails() {
		return leadInfoDetails;
	}

	public void setLeadInfoDetails(List<WebEngageLeadInfoDetail> leadInfoDetails) {
		this.leadInfoDetails = leadInfoDetails;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public boolean getIsDnd() {
		return isDnd;
	}

	public void setIsDnd(boolean isDnd) {
		this.isDnd = isDnd;
	}

	public LeadInfo getLeadInfo() {
		return leadInfo;
	}

	public void setLeadInfo(LeadInfo leadInfo) {
		this.leadInfo = leadInfo;
	}

	public TokenPojo getToken() {
		return token;
	}

	public void setToken(TokenPojo token) {
		this.token = token;
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public void setUserEntity(UserEntity userEntity) {
		this.userEntity = userEntity;
	}

	public void setDnd(boolean isDnd) {
		this.isDnd = isDnd;
	}

	@Override
	public String toString() {
		return "WebEnagage [leadName=" + leadName + ", userId=" + userId + ", campaignId=" + campaignId
				+ ", leadSchedule=" + leadSchedule + ", msgId=" + msgId + ", isDnd=" + isDnd + ", leadInfoDetails="
				+ leadInfoDetails + ", uKey=" + uKey + ", checkRcs=" + checkRcs + ", leadInfo=" + leadInfo + ", token="
				+ token + ", userEntity=" + userEntity + ", leadInfoDetailEntities=" + leadInfoDetailEntities + "]";
	}

}
