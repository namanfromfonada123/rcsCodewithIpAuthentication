package com.messaging.rcs.email.model;

public class CampaignListing {

	private long campaignId;
	private String campaignName;
	private Long rcsMsgTypeId;

	public Long getRcsMsgTypeId() {
		return rcsMsgTypeId;
	}

	public void setRcsMsgTypeId(Long rcsMsgTypeId) {
		this.rcsMsgTypeId = rcsMsgTypeId;
	}

	public long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(long campaignId) {
		this.campaignId = campaignId;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	@Override
	public String toString() {
		return "CampaignListing [campaignId=" + campaignId + ", campaignName=" + campaignName + ", rcsMsgTypeId="
				+ rcsMsgTypeId + "]";
	}
}
