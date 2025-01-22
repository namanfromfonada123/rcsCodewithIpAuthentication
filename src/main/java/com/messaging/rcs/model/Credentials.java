package com.messaging.rcs.model;

public class Credentials {

	private String panel_uid;
	private String panel_pwd;
	private String api_uid;
	private String api_pwd;
	private String sms_api_uid;
	private String sms_api_pwd;
	private String pro_yulore_uid;
	private String pro_yulore_pwd;

	// Getter Methods

	public String getPanel_uid() {
		return panel_uid;
	}

	public String getPanel_pwd() {
		return panel_pwd;
	}

	public String getApi_uid() {
		return api_uid;
	}

	public String getApi_pwd() {
		return api_pwd;
	}

	public String getSms_api_uid() {
		return sms_api_uid;
	}

	public String getSms_api_pwd() {
		return sms_api_pwd;
	}

	public String getPro_yulore_uid() {
		return pro_yulore_uid;
	}

	public String getPro_yulore_pwd() {
		return pro_yulore_pwd;
	}

	// Setter Methods

	public void setPanel_uid(String panel_uid) {
		this.panel_uid = panel_uid;
	}

	public void setPanel_pwd(String panel_pwd) {
		this.panel_pwd = panel_pwd;
	}

	public void setApi_uid(String api_uid) {
		this.api_uid = api_uid;
	}

	public void setApi_pwd(String api_pwd) {
		this.api_pwd = api_pwd;
	}

	public void setSms_api_uid(String sms_api_uid) {
		this.sms_api_uid = sms_api_uid;
	}

	public void setSms_api_pwd(String sms_api_pwd) {
		this.sms_api_pwd = sms_api_pwd;
	}

	public void setPro_yulore_uid(String pro_yulore_uid) {
		this.pro_yulore_uid = pro_yulore_uid;
	}

	public void setPro_yulore_pwd(String pro_yulore_pwd) {
		this.pro_yulore_pwd = pro_yulore_pwd;
	}

	@Override
	public String toString() {
		return "Credentials [panel_uid=" + panel_uid + ", panel_pwd=" + panel_pwd + ", api_uid=" + api_uid
				+ ", api_pwd=" + api_pwd + ", sms_api_uid=" + sms_api_uid + ", sms_api_pwd=" + sms_api_pwd
				+ ", pro_yulore_uid=" + pro_yulore_uid + ", pro_yulore_pwd=" + pro_yulore_pwd + "]";
	}
}
