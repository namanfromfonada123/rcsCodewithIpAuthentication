package com.messaging.rcs.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * 
 * @author RahulRajput 2023-05-26
 *
 */
@Entity
@Table(name = "lead_info_detail_feb")
public class LeadInfoDetailEntityBackUp {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "LEAD_INFO_DETAIL_ID")
	private Long leadInfoDetailId;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "LEAD_ID")
	private long leadId;

	@Column(name = "PHONE_NUMBER")
	private String phoneNumber;

	@Column(name = "ATTEMPT_NUM")
	private Integer attemptNum;

	@Column(name = "ADDITIONAL_DATA_INFO_NUMERIC")
	private String additonalDataInfoNumeric;

	@Column(name = "ADDITIONAL_DATA_INFO_DATE")
	private String additonalDataInfoDate;

	@Lob
	@Column(columnDefinition = "TEXT", name = "ADDITIONAL_DATA_INFO_TEXT2")
	private String additonalDataInfoText2;

	@Column(name = "ADDITIONAL_DATA_INFO_TEXT")
	private String additonalDataInfoText;

	private String cli;

	private String language;

	@Column(name = "PHONE_NUMBER_STATUS")
	private String phoneNumberStatus;

	@Column(name = "NEXT_CALL_DTM")
	private Date nextCallDtm;

	private Integer retryAttempt;
	private Integer retryDuration;
	private String retryType;
	private String playWords;

	@Column(name = "CREATED_BY")
	private String createdBy;
	@Column(name = "CREATED_DATE")
	private Date createdDate;
	@Column(name = "LAST_MODIFIED_BY")
	private String lastModifiedBy;
	@Column(name = "LAST_MODIFIED_DATE")
	private Date lastModifiedDate;
	private String templateCustomParamKey;
	private String templateCustomParamValue;
	private String textFields;
	private Integer sendDlrStatus;
	@Column(name = "send_to_queue")
	private Integer sendToQueue;

	public Integer getSendToQueue() {
		return sendToQueue;
	}

	public void setSendToQueue(Integer sendToQueue) {
		this.sendToQueue = sendToQueue;
	}

	public Integer getSendDlrStatus() {
		return sendDlrStatus;
	}

	public void setSendDlrStatus(Integer sendDlrStatus) {
		this.sendDlrStatus = sendDlrStatus;
	}

	public String getTextFields() {
		return textFields;
	}

	public void setTextFields(String textFields) {
		this.textFields = textFields;
	}

	public String getTemplateCustomParamKey() {
		return templateCustomParamKey;
	}

	public void setTemplateCustomParamKey(String templateCustomParamKey) {
		this.templateCustomParamKey = templateCustomParamKey;
	}

	public String getTemplateCustomParamValue() {
		return templateCustomParamValue;
	}

	public void setTemplateCustomParamValue(String templateCustomParamValue) {
		this.templateCustomParamValue = templateCustomParamValue;
	}

	public Long getLeadInfoDetailId() {
		return leadInfoDetailId;
	}

	public void setLeadInfoDetailId(Long leadInfoDetailId) {
		this.leadInfoDetailId = leadInfoDetailId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getLeadId() {
		return leadId;
	}

	public void setLeadId(long leadId) {
		this.leadId = leadId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getPhoneNumberStatus() {
		return phoneNumberStatus;
	}

	public void setPhoneNumberStatus(String phoneNumberStatus) {
		this.phoneNumberStatus = phoneNumberStatus;
	}

	public Integer getAttemptNum() {
		return attemptNum;
	}

	public void setAttemptNum(Integer attemptNum) {
		this.attemptNum = attemptNum;
	}

	public String getAdditonalDataInfoText2() {
		return additonalDataInfoText2;
	}

	public void setAdditonalDataInfoText2(String additonalDataInfoText2) {
		this.additonalDataInfoText2 = additonalDataInfoText2;
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

	public String getRetryType() {
		return retryType;
	}

	public void setRetryType(String retryType) {
		this.retryType = retryType;
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

	public String getPlayWords() {
		return playWords;
	}

	public void setPlayWords(String playFields) {
		this.playWords = playFields;
	}

	@Override
	public String toString() {
		return "LeadInfoDetailEntity [leadInfoDetailId=" + leadInfoDetailId + ", status=" + status + ", leadId="
				+ leadId + ", phoneNumber=" + phoneNumber + ", attemptNum=" + attemptNum + ", additonalDataInfoNumeric="
				+ additonalDataInfoNumeric + ", additonalDataInfoDate=" + additonalDataInfoDate
				+ ", additonalDataInfoText2=" + additonalDataInfoText2 + ", additonalDataInfoText="
				+ additonalDataInfoText + ", cli=" + cli + ", language=" + language + ", phoneNumberStatus="
				+ phoneNumberStatus + ", nextCallDtm=" + nextCallDtm + ", retryAttempt=" + retryAttempt
				+ ", retryDuration=" + retryDuration + ", retryType=" + retryType + ", playWords=" + playWords
				+ ", createdBy=" + createdBy + ", createdDate=" + createdDate + ", lastModifiedBy=" + lastModifiedBy
				+ ", lastModifiedDate=" + lastModifiedDate + ", templateCustomParamKey=" + templateCustomParamKey
				+ ", templateCustomParamValue=" + templateCustomParamValue + ", textFields=" + textFields
				+ ", sendDlrStatus=" + sendDlrStatus + "]";
	}

}
