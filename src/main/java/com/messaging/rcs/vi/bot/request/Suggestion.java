package com.messaging.rcs.vi.bot.request;

public class Suggestion {
	private String suggestionID;
	private String suggestionType;
	private String displayText;
	private String postback;
	private String url;

	public String getSuggestionID() {
		return suggestionID;
	}

	public void setSuggestionID(String value) {
		this.suggestionID = value;
	}

	public String getSuggestionType() {
		return suggestionType;
	}

	public void setSuggestionType(String value) {
		this.suggestionType = value;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String value) {
		this.displayText = value;
	}

	public String getPostback() {
		return postback;
	}

	public void setPostback(String value) {
		this.postback = value;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String value) {
		this.url = value;
	}
}
