package com.messaging.rcs.vi.bot.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BotApiResponse {

	@JsonProperty("RCSMessage")
	public RCSMessage RCSMessage;
	public Reason reason;

	public RCSMessage getRCSMessage() {
		return RCSMessage;
	}

	public void setRCSMessage(RCSMessage rCSMessage) {
		RCSMessage = rCSMessage;
	}

	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return "BotApiResponse [RCSMessage=" + RCSMessage + ", reason=" + reason + "]";
	}

}
