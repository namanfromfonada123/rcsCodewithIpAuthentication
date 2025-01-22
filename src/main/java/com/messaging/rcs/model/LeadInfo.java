package com.messaging.rcs.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author RahulRajput
 *
 */
public class LeadInfo extends BaseModel {
	private Long leadId;
	private String leadName;
	private String campaignName;

	private Long userId;
	private Long campaignId;
	private Schedule leadSchedule;
	private RetryInfo retryInfo;
	private String leadStatus;
	private String leadCompletionStatus;
	private String leadAction;
	private Integer countOfNumbers;
	private Integer countOfInvalidNumbers;
	private Integer countOfNonRcsNumbers;
	private Integer countOfDuplicateNumbers;
	private Integer countOfBlackListNumbers;
	private Date insertDtm;
	private Date processDtm;
	private String leadPriorityType;
	private Integer leadPriority;
	private List<LeadInfoDetail> leadInfoDetails = new ArrayList<>();
	private Long rcsMsgTypeId;
	private Long fileTotalRecord;
	private String templateVideoId;

	public String getTemplateVideoId() {
		return templateVideoId;
	}

	public void setTemplateVideoId(String templateVideoId) {
		this.templateVideoId = templateVideoId;
	}

	public Long getFileTotalRecord() {
		return fileTotalRecord;
	}

	public void setFileTotalRecord(Long fileTotalRecord) {
		this.fileTotalRecord = fileTotalRecord;
	}

	public Long getRcsMsgTypeId() {
		return rcsMsgTypeId;
	}

	public void setRcsMsgTypeId(Long rcsMsgTypeId) {
		this.rcsMsgTypeId = rcsMsgTypeId;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public Long getLeadId() {
		return leadId;
	}

	public void setLeadId(Long leadId) {
		this.leadId = leadId;
	}

	public String getLeadName() {
		return leadName;
	}

	public void setLeadName(String leadName) {
		this.leadName = leadName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public Schedule getLeadSchedule() {
		return leadSchedule;
	}

	public void setLeadSchedule(Schedule leadSchedule) {
		this.leadSchedule = leadSchedule;
	}

	public RetryInfo getRetryInfo() {
		return retryInfo;
	}

	public void setRetryInfo(RetryInfo retryInfo) {
		this.retryInfo = retryInfo;
	}

	public String getLeadStatus() {
		return leadStatus;
	}

	public void setLeadStatus(String leadStatus) {
		this.leadStatus = leadStatus;
	}

	public String getLeadCompletionStatus() {
		return leadCompletionStatus;
	}

	public void setLeadCompletionStatus(String leadCompletionStatus) {
		this.leadCompletionStatus = leadCompletionStatus;
	}

	public String getLeadAction() {
		return leadAction;
	}

	public void setLeadAction(String leadAction) {
		this.leadAction = leadAction;
	}

	public Integer getCountOfNumbers() {
		return countOfNumbers;
	}

	public void setCountOfNumbers(Integer countOfNumbers) {
		this.countOfNumbers = countOfNumbers;
	}

	public Integer getCountOfInvalidNumbers() {
		return countOfInvalidNumbers;
	}

	public void setCountOfInvalidNumbers(Integer countOfInvalidNumbers) {
		this.countOfInvalidNumbers = countOfInvalidNumbers;
	}

	public Integer getCountOfNonRcsNumbers() {
		return countOfNonRcsNumbers;
	}

	public void setCountOfNonRcsNumbers(Integer countOfNonRcsNumbers) {
		this.countOfNonRcsNumbers = countOfNonRcsNumbers;
	}

	public Integer getCountOfDuplicateNumbers() {
		return countOfDuplicateNumbers;
	}

	public void setCountOfDuplicateNumbers(Integer countOfDuplicateNumbers) {
		this.countOfDuplicateNumbers = countOfDuplicateNumbers;
	}

	public Integer getCountOfBlackListNumbers() {
		return countOfBlackListNumbers;
	}

	public void setCountOfBlackListNumbers(Integer countOfBlackListNumbers) {
		this.countOfBlackListNumbers = countOfBlackListNumbers;
	}

	public Date getInsertDtm() {
		return insertDtm;
	}

	public void setInsertDtm(Date insertDtm) {
		this.insertDtm = insertDtm;
	}

	public Date getProcessDtm() {
		return processDtm;
	}

	public void setProcessDtm(Date processDtm) {
		this.processDtm = processDtm;
	}

	public String getLeadPriorityType() {
		return leadPriorityType;
	}

	public void setLeadPriorityType(String leadPriorityType) {
		this.leadPriorityType = leadPriorityType;
	}

	public Integer getLeadPriority() {
		return leadPriority;
	}

	public void setLeadPriority(Integer leadPriority) {
		this.leadPriority = leadPriority;
	}

	public List<LeadInfoDetail> getLeadInfoDetails() {
		return leadInfoDetails;
	}

	public void setLeadInfoDetails(List<LeadInfoDetail> leadInfoDetails) {
		this.leadInfoDetails = leadInfoDetails;
	}

	@Override
	public String toString() {
		return "LeadInfo [leadId=" + leadId + ", leadName=" + leadName + ", campaignName=" + campaignName + ", userId="
				+ userId + ", campaignId=" + campaignId + ", leadSchedule=" + leadSchedule + ", retryInfo=" + retryInfo
				+ ", leadStatus=" + leadStatus + ", leadCompletionStatus=" + leadCompletionStatus + ", leadAction="
				+ leadAction + ", countOfNumbers=" + countOfNumbers + ", countOfInvalidNumbers=" + countOfInvalidNumbers
				+ ", countOfNonRcsNumbers=" + countOfNonRcsNumbers + ", countOfDuplicateNumbers="
				+ countOfDuplicateNumbers + ", countOfBlackListNumbers=" + countOfBlackListNumbers + ", insertDtm="
				+ insertDtm + ", processDtm=" + processDtm + ", leadPriorityType=" + leadPriorityType
				+ ", leadPriority=" + leadPriority + ", leadInfoDetails=" + leadInfoDetails + ", rcsMsgTypeId="
				+ rcsMsgTypeId + ", fileTotalRecord=" + fileTotalRecord + "]";
	}

}
