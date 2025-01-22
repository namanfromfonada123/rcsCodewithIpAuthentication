package com.messaging.rcs.model;

public class DemoRcsModel {

	private String userName;
	private String botId;
	private String leadName;
	private String templateCode;
    private String campaignName;
    private String dataSourceName;
    private String campaignType;
    
    
	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBotId() {
		return botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public String getLeadName() {
		return leadName;
	}

	public void setLeadName(String leadName) {
		this.leadName = leadName;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	@Override
	public String toString() {
		return "DemoRcsModel [userName=" + userName + ", botId=" + botId + ", leadName=" + leadName + ", templateCode="
				+ templateCode + ", campaignName=" + campaignName + ", dataSourceName=" + dataSourceName
				+ ", campaignType=" + campaignType + "]";
	}

}
