package com.messaging.rcs.model;

import java.io.Serializable;

public class CommonReportPojo implements Serializable {
	private String action;
	private Integer clientId;
	private String username;
	private String role;
	private String fromDate;
	private String toDate;
	private String vmn;
	private String camId;
	private String camType;
	private String msisdn;
	private String count;

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public void setVmn(String vmn) {
		this.vmn = vmn;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Integer getClientId() {
		return clientId;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getVmn() {
		return vmn;
	}

	public void setVmd(String vmd) {
		this.vmn = vmd;
	}

	public String getCamId() {
		return camId;
	}

	public void setCamId(String camId) {
		this.camId = camId;
	}

	public String getCamType() {
		return camType;
	}

	public void setCamType(String camType) {
		this.camType = camType;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	@Override
	public String toString() {
		return "CommonReportPojo [action=" + action + ", clientId=" + clientId + ", username=" + username + ", role="
				+ role + ", fromDate=" + fromDate + ", toDate=" + toDate + ", vmn=" + vmn + ", camId=" + camId
				+ ", camType=" + camType + ", msisdn=" + msisdn + ", count=" + count + "]";
	}

}
