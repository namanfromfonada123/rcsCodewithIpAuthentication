package com.messaging.rcs.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "users" })
public class RcsMsisdnRequestPojo {
	@JsonProperty("users")
	public List<String> users;

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	@Override
	public String toString() {
		return "RcsMsisdnRequestPojo [users=" + users + "]";
	}

}
