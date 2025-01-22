package com.messaging.rcs.vi.bot.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class BulkTokenResponse {
	@JsonProperty("rcsEnabledContacts")
	private List<String> rcsEnabledContacts;

	public List<String> getRcsEnabledContacts() {
		return rcsEnabledContacts;
	}

	public void setRcsEnabledContacts(List<String> rcsEnabledContacts) {
		this.rcsEnabledContacts = rcsEnabledContacts;
	}

	@Override
	public String toString() {
		return "BulkTokenResponse [rcsEnabledContacts=" + rcsEnabledContacts + "]";
	}

}
