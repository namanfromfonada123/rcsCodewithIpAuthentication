package com.messaging.rcs.model;

public class LeadAndCampaingListing {
	private String campId;
	private String leadName;
	private String campaignName;
	private String leadId;

	public String getCampId() {
		return campId;
	}

	public void setCampId(String campId) {
		this.campId = campId;
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

	public String getLeadId() {
		return leadId;
	}

	public void setLeadId(String leadId) {
		this.leadId = leadId;
	}

	@Override
	public String toString() {
		return "LeadAndCampaingListing [campId=" + campId + ", leadName=" + leadName + ", campaignName=" + campaignName
				+ ", leadId=" + leadId + "]";
	}

}
