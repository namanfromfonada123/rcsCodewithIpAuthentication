package com.messaging.rcs.vi.bot.request;

public class StandAlone {
	private String cardID;
	private String cardTitle;
	private String cardDescription;
	private String mediaURL;
	private Suggestion[] suggestions;

	public String getCardID() {
		return cardID;
	}

	public void setCardID(String value) {
		this.cardID = value;
	}

	public String getCardTitle() {
		return cardTitle;
	}

	public void setCardTitle(String value) {
		this.cardTitle = value;
	}

	public String getCardDescription() {
		return cardDescription;
	}

	public void setCardDescription(String value) {
		this.cardDescription = value;
	}

	public String getMediaURL() {
		return mediaURL;
	}

	public void setMediaURL(String value) {
		this.mediaURL = value;
	}

	public Suggestion[] getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(Suggestion[] value) {
		this.suggestions = value;
	}
}
