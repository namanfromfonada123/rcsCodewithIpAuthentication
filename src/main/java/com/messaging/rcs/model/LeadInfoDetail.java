package com.messaging.rcs.model;

import java.util.Date;

/**
 * 
 * @author RahulRajput
 *
 */
public class LeadInfoDetail extends BaseModel {

	private Long leadInfoDetailId;
	private Long leadId;
	private String phoneNumber;
	private Integer attemptNum;
	private String status;
	private String additonalDataInfoNumeric;
	private String additonalDataInfoDate;
	private String additonalDataInfoText2;
	private String additonalDataInfoText;
	private String cli;
	private String language;
	private Date nextCallDtm;
	private Integer retryAttempt;
	private Integer retryDuration;
	private String retryType;
	private String playWords;
	private String textFields;
	private String smsContent;
	private String uUid;
	private String unicode;
	private String sender;
	private String videoUrl;
	private String videoRequestId;
	private String mediaUrl;

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getVideoRequestId() {
		return videoRequestId;
	}

	public void setVideoRequestId(String videoRequestId) {
		this.videoRequestId = videoRequestId;
	}

	public String getUnicode() {
		return unicode;
	}

	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getuUid() {
		return uUid;
	}

	public void setuUid(String uUid) {
		this.uUid = uUid;
	}

	public String getSmsContent() {
		return smsContent;
	}

	public void setSmsContent(String smsContent) {
		this.smsContent = smsContent;
	}

	public String getTextFields() {
		return textFields;
	}

	public void setTextFields(String textFields) {
		this.textFields = textFields;
	}

	public Long getLeadInfoDetailId() {
		return leadInfoDetailId;
	}

	public void setLeadInfoDetailId(Long leadInfoDetailId) {
		this.leadInfoDetailId = leadInfoDetailId;
	}

	public Long getLeadId() {
		return leadId;
	}

	public void setLeadId(Long leadId) {
		this.leadId = leadId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Integer getAttemptNum() {
		return attemptNum;
	}

	public void setAttemptNum(Integer attemptNum) {
		this.attemptNum = attemptNum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAdditonalDataInfoNumeric() {
		return additonalDataInfoNumeric;
	}

	public void setAdditonalDataInfoNumeric(String additonalDataInfoNumeric) {
		this.additonalDataInfoNumeric = additonalDataInfoNumeric;
	}

	public String getAdditonalDataInfoDate() {
		return additonalDataInfoDate;
	}

	public void setAdditonalDataInfoDate(String additonalDataInfoDate) {
		this.additonalDataInfoDate = additonalDataInfoDate;
	}

	public String getAdditonalDataInfoText2() {
		return additonalDataInfoText2;
	}

	public void setAdditonalDataInfoText2(String additonalDataInfoText2) {
		this.additonalDataInfoText2 = additonalDataInfoText2;
	}

	public String getAdditonalDataInfoText() {
		return additonalDataInfoText;
	}

	public void setAdditonalDataInfoText(String additonalDataInfoText) {
		this.additonalDataInfoText = additonalDataInfoText;
	}

	public String getCli() {
		return cli;
	}

	public void setCli(String cli) {
		this.cli = cli;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Date getNextCallDtm() {
		return nextCallDtm;
	}

	public void setNextCallDtm(Date nextCallDtm) {
		this.nextCallDtm = nextCallDtm;
	}

	public Integer getRetryAttempt() {
		return retryAttempt;
	}

	public void setRetryAttempt(Integer retryAttempt) {
		this.retryAttempt = retryAttempt;
	}

	public Integer getRetryDuration() {
		return retryDuration;
	}

	public void setRetryDuration(Integer retryDuration) {
		this.retryDuration = retryDuration;
	}

	public String getRetryType() {
		return retryType;
	}

	public void setRetryType(String retryType) {
		this.retryType = retryType;
	}

	public String getPlayWords() {
		return playWords;
	}

	public void setPlayWords(String playWords) {
		this.playWords = playWords;
	}

	public String toRecord() {
		return phoneNumber + "::" + additonalDataInfoText + "::" + additonalDataInfoText2;
	}

	public String toSmsApiRecord() {
		return phoneNumber + "::" + smsContent + "::" + unicode + "::" + sender;
	}

	@Override
	public String toString() {
		return "LeadInfoDetail [leadInfoDetailId=" + leadInfoDetailId + ", leadId=" + leadId + ", phoneNumber="
				+ phoneNumber + ", attemptNum=" + attemptNum + ", status=" + status + ", additonalDataInfoNumeric="
				+ additonalDataInfoNumeric + ", additonalDataInfoDate=" + additonalDataInfoDate
				+ ", additonalDataInfoText2=" + additonalDataInfoText2 + ", additonalDataInfoText="
				+ additonalDataInfoText + ", cli=" + cli + ", language=" + language + ", nextCallDtm=" + nextCallDtm
				+ ", retryAttempt=" + retryAttempt + ", retryDuration=" + retryDuration + ", retryType=" + retryType
				+ ", playWords=" + playWords + ", textFields=" + textFields + "]";
	}
}
