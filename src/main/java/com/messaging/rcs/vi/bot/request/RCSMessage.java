package com.messaging.rcs.vi.bot.request;

/**
 * createdOn:: 2023-04-01
 * 
 * @author Rahul
 *
 */
public class RCSMessage {
	public TemplateMessage templateMessage;

	public TemplateMessage getTemplateMessage() {
		return templateMessage;
	}

	public void setTemplateMessage(TemplateMessage templateMessage) {
		this.templateMessage = templateMessage;
	}

	@Override
	public String toString() {
		return "RCSMessage [templateMessage=" + templateMessage + "]";
	}

}
