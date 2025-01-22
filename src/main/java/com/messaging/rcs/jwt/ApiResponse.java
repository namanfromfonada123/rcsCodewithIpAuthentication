package com.messaging.rcs.jwt;

import javax.persistence.Lob;

public class ApiResponse<T> {
	private int status;
	private String role;
	private String userType;
	private String msisdn;
	private long userId;
	private int dailyUsageLimit;
	private Object result;
	private String botId;
	private String copyRight;
	private String aiVedio;

	public String getAiVedio() {
		return aiVedio;
	}

	public void setAiVedio(String aiVedio) {
		this.aiVedio = aiVedio;
	}

	@Lob
	private byte[] companyLogo; // image

	public ApiResponse(String aiVedio,int status, String role, String userType, String msisdn, long userId, int dailyUsageLimit,
			String botId, byte[] companyLogo, String copyRight, Object result) {
		this.status = status;
		this.role = role;
		this.userType = userType;
		this.msisdn = msisdn;
		this.userId = userId;
		this.dailyUsageLimit = dailyUsageLimit;
		this.botId = botId;
		this.result = result;
		this.companyLogo = companyLogo;
		this.copyRight = copyRight;
		this.aiVedio = aiVedio;
	}

	public String getCopyRight() {
		return copyRight;
	}

	public void setCopyRight(String copyRight) {
		this.copyRight = copyRight;
	}

	public byte[] getCompanyLogo() {
		return companyLogo;
	}

	public void setCompanyLogo(byte[] companyLogo) {
		this.companyLogo = companyLogo;
	}

	public String getBotId() {
		return botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getDailyUsageLimit() {
		return dailyUsageLimit;
	}

	public void setDailyUsageLimit(int dailyUsageLimit) {
		this.dailyUsageLimit = dailyUsageLimit;
	}

	@Override
	public String toString() {
		return "ApiResponse [status=" + status + ", role=" + role + ", userType=" + userType + ", msisdn=" + msisdn
				+ ", userId=" + userId + ", dailyUsageLimit=" + dailyUsageLimit + ", result=" + result + ", botId="
				+ botId + "]";
	}

}
