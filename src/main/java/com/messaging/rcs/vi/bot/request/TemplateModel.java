package com.messaging.rcs.vi.bot.request;

public class TemplateModel {
	private String name;
	private String type;
	private String status;
	private String orientation;
	private String height;
	private String botID;
	private String lastUpdate;
	private StandAlone standAlone;

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		this.type = value;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String value) {
		this.status = value;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String value) {
		this.orientation = value;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String value) {
		this.height = value;
	}

	public String getBotID() {
		return botID;
	}

	public void setBotID(String value) {
		this.botID = value;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String value) {
		this.lastUpdate = value;
	}

	public StandAlone getStandAlone() {
		return standAlone;
	}

	public void setStandAlone(StandAlone value) {
		this.standAlone = value;
	}
}