package com.messaging.rcs.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.messaging.rcs.email.model.WebEngageLeadInfoDetail;
import com.messaging.rcs.email.model.WebEngageSchedule;

public class ClientLeadCreatedPojo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2736911235490297622L;
	private String leadName;
	private Long userId;
	private Long campaignId;
	private WebEngageSchedule leadSchedule;
	private String msgId;
	private boolean isDnd;
	private List<WebEngageLeadInfoDetail> leadInfoDetails = new ArrayList<>();
	private String uKey;
	private String checkRcs;
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
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public boolean isDnd() {
		return isDnd;
	}
	public void setDnd(boolean isDnd) {
		this.isDnd = isDnd;
	}
	public List<WebEngageLeadInfoDetail> getLeadInfoDetails() {
		return leadInfoDetails;
	}
	public void setLeadInfoDetails(List<WebEngageLeadInfoDetail> leadInfoDetails) {
		this.leadInfoDetails = leadInfoDetails;
	}
	public String getuKey() {
		return uKey;
	}
	public void setuKey(String uKey) {
		this.uKey = uKey;
	}
	public String getCheckRcs() {
		return checkRcs;
	}
	public void setCheckRcs(String checkRcs) {
		this.checkRcs = checkRcs;
	}
	@Override
	public String toString() {
		return "ClientLeadCreatedPojo [leadName=" + leadName + ", userId=" + userId + ", campaignId=" + campaignId
				+ ", leadSchedule=" + leadSchedule + ", msgId=" + msgId + ", isDnd=" + isDnd + ", leadInfoDetails="
				+ leadInfoDetails + ", uKey=" + uKey + ", checkRcs=" + checkRcs + "]";
	}

}
