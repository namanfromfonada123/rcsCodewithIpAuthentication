package com.messaging.rcs.vi.bot.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * createdOn:: 2023-04-01
 * 
 * @author Rahul
 *
 */
public class BotApiRequest {
	@JsonProperty("RCSMessage")
	public RCSMessage RCSMessage;
	public MessageContact messageContact;

	public RCSMessage getRCSMessage() {
		return RCSMessage;
	}

	public void setRCSMessage(RCSMessage rCSMessage) {
		RCSMessage = rCSMessage;
	}

	public MessageContact getMessageContact() {
		return messageContact;
	}

	public void setMessageContact(MessageContact messageContact) {
		this.messageContact = messageContact;
	}

	@Override
	public String toString() {
		return "BotApiRequest [RCSMessage=" + RCSMessage + ", messageContact=" + messageContact + "]";
	}

}
