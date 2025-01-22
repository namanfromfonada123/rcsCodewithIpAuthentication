package com.messaging.rcs.model;

public class LeadListing {

	private long leadId;
	private String leadName;

	public long getLeadId() {
		return leadId;
	}

	public void setLeadId(long leadId) {
		this.leadId = leadId;
	}

	public String getLeadName() {
		return leadName;
	}

	public void setLeadName(String leadName) {
		this.leadName = leadName;
	}

	@Override
	public String toString() {
		return "LeadListing [leadId=" + leadId + ", leadName=" + leadName + "]";
	}

}
