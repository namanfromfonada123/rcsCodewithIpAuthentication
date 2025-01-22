package com.messaging.rcs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author RahulRajput
 *
 */
@Entity
@Table(name = "LEAD_INFO")
public class LeadInfoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "LEAD_ID")
	private Long leadId;
	@Column(name = "LEAD_NAME")
	private String leadName;
	@Column(name = "LEAD_USER_ID")
	private Long userId;
	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "LEAD_USER_ID", insertable = false, updatable = false)
	 * private UserEntity user;
	 */
	@Column(name = "LEAD_CAMPAIGN_ID")
	private Long campaignId;

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "LEAD_CAMPAIGN_ID", insertable = false, updatable = false)
	 * private CampaignEntity campaignEntity;
	 */

	@Column(name = "LEAD_SCHEDULE_ID")
	private Long scheduleId;

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "LEAD_SCHEDULE_ID", insertable = false, updatable = false)
	 * private ScheduleEntity leadSchedule;
	 */
	@Column(name = "LEAD_RETRY_ID")
	private Long retryId;

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "LEAD_RETRY_ID", insertable = false, updatable = false)
	 * private RetryInfoEntity retryInfo;
	 */
	@Column(name = "LEAD_STATUS")
	private String leadStatus;
	@Column(name = "LEAD_COMPLETION_STATUS")
	private String leadCompletionStatus;
	@Column(name = "LEAD_ACTION")
	private String leadAction;
	@Column(name = "COUNT_OF_NUMBERS")
	private Integer countOfNumbers;
	@Column(name = "COUNT_OF_INVALID_NUMBERS")
	private Integer countOfInvalidNumbers;
	@Column(name = "COUNT_OF_NON_RCS_NUMBERS")
	private Integer countOfNonRcsNumbers;
	@Column(name = "COUNT_OF_DUPLICATE_NUMBERS")
	private Integer countOfDuplicateNumbers;
	@Column(name = "COUNT_OF_BLACKLIST_NUMBERS")
	private Integer countOfBlackListNumbers;
	@Column(name = "INSERT_DTM")
	private Date insertDtm;
	@Column(name = "PROCESS_DTM")
	private Date processDtm;
	@Column(name = "LEAD_PRIORITY_TYPE")
	private String leadPriorityType;
	@Column(name = "LEAD_PRIORITY")
	private Integer leadPriority;
	@Column(name = "CREATED_BY")
	private String createdBy;
	@Column(name = "CREATED_DATE")
	private Date createdDate;
	@Column(name = "LAST_MODIFIED_BY")
	private String lastModifiedBy;
	@Column(name = "LAST_MODIFIED_DATE")
	private Date lastModifiedDate;
	@Column(name = "PROCESS_END_DTM")
	private Date processEndDtm;
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

	public Long getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Long getRetryId() {
		return retryId;
	}

	public void setRetryId(Long retryId) {
		this.retryId = retryId;
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

	public Date getProcessEndDtm() {
		return processEndDtm;
	}

	public void setProcessEndDtm(Date processEndDtm) {
		this.processEndDtm = processEndDtm;
	}

	@Override
	public String toString() {
		return "LeadInfoEntity [leadId=" + leadId + ", leadName=" + leadName + ", userId=" + userId + ", campaignId="
				+ campaignId + ", scheduleId=" + scheduleId + ", retryId=" + retryId + ", leadStatus=" + leadStatus
				+ ", leadCompletionStatus=" + leadCompletionStatus + ", leadAction=" + leadAction + ", countOfNumbers="
				+ countOfNumbers + ", countOfInvalidNumbers=" + countOfInvalidNumbers + ", countOfNonRcsNumbers="
				+ countOfNonRcsNumbers + ", countOfDuplicateNumbers=" + countOfDuplicateNumbers
				+ ", countOfBlackListNumbers=" + countOfBlackListNumbers + ", insertDtm=" + insertDtm + ", processDtm="
				+ processDtm + ", leadPriorityType=" + leadPriorityType + ", leadPriority=" + leadPriority
				+ ", createdBy=" + createdBy + ", createdDate=" + createdDate + ", lastModifiedBy=" + lastModifiedBy
				+ ", lastModifiedDate=" + lastModifiedDate + ", processEndDtm=" + processEndDtm + ", rcsMsgTypeId="
				+ rcsMsgTypeId + ", fileTotalRecord=" + fileTotalRecord + "]";
	}

}
