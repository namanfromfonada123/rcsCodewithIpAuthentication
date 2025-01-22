package com.messaging.rcs.vi.bot.request;

/**
 * createdOn:: 2023-04-01
 * 
 * @author Rahul
 *
 */
public class MessageContact {
	public String userContact;

	public String getUserContact() {
		return userContact;
	}

	public void setUserContact(String userContact) {
		this.userContact = userContact;
	}

	@Override
	public String toString() {
		return "MessageContact [userContact=" + userContact + "]";
	}

}
