package com.messaging.rcs.vi.bot.request;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "rcsEnabledContacts" })
public class RcsEnableContactsResponse {
	@JsonProperty("rcsEnabledContacts")
	public ArrayList<String> rcsEnabledContacts;

	public ArrayList<String> getRcsEnabledContacts() {
		return rcsEnabledContacts;
	}

	public void setRcsEnabledContacts(ArrayList<String> rcsEnabledContacts) {
		this.rcsEnabledContacts = rcsEnabledContacts;
	}

	@Override
	public String toString() {
		return "RcsEnableContactsResponse [rcsEnabledContacts=" + rcsEnabledContacts + "]";
	}

}
