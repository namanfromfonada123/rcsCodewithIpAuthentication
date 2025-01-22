package com.messaging.rcs.model;

import java.util.ArrayList;
import java.util.List;

import com.messaging.rcs.email.model.WebEngageLeadInfoDetail;
import com.messaging.rcs.email.model.WebEngageSchedule;

public class WebEnagagePojo {
	/**
	 * 
	 */
	private String leadName;
	private Long userId;
	private Long campaignId;
	private WebEngageSchedule leadSchedule;
	private String msgId;
	private boolean isDnd;
	private List<WebEngageLeadInfoDetail> leadInfoDetails = new ArrayList<>();
	private String uKey;

	

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

	@Override
	public String toString() {
		return "WebEnagagePojo [leadName=" + leadName + ", userId=" + userId + ", campaignId=" + campaignId
				+ ", leadSchedule=" + leadSchedule + ", msgId=" + msgId + ", isDnd=" + isDnd + ", leadInfoDetails="
				+ leadInfoDetails + ", uKey=" + uKey + "]";
	}

	

}
