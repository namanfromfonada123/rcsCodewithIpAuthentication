package com.messaging.rcs.vi.bot.request;

public class TemplateMessage {
	public String templateCode;

	public String customParams;

	public String getCustomParams() {
		return customParams;
	}

	public void setCustomParams(String customParams) {
		this.customParams = customParams;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	@Override
	public String toString() {
		return "TemplateMessage [templateCode=" + templateCode + "]";
	}

}
