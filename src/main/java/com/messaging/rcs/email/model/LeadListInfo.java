package com.messaging.rcs.email.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Rahul Kumar 12-06-2023.
 */
public class LeadListInfo {

	private String userName;
	private Long leadId;
	private String leadName;
	private String leadCreateDtm;
	private Long campaignId;
	private String campaignName;
	private Integer totalNumbers;
	private Integer noOfRetry;
	private Integer validCount;
	private Integer invalidCount;
	private Integer duplicateCount;
	private Integer blacklistCount;
	private Integer totalDialed;
	private Integer totalDnd;
	private int discardedCount;
	private int gvSupportedCount;
	private String leadStatus;
	private String leadScheduleStartDtm;
	private String leadScheduleEndDtm;

	public String getHeader() {
		return "userName,leadId,leadName,leadCreateDtm,campaignId,campaignName,totalNumbers,noOfRetry,validCount,invalidCount,"
				+ "duplicateCount,blacklistCount,totalDialed,totalDnd,discardedCount,gvSupportedCount,leadStatus,leadScheduleStartDtm,leadScheduleEndDtm";
	}

	public String getData() {
		StringBuilder sb = new StringBuilder();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String executionDate = null;
		try {
			executionDate = df.format(df.parse(leadCreateDtm));
		} catch (Exception e) {
			executionDate = leadCreateDtm;
		}

		return userName + "," + leadId + "," + leadName + "," + executionDate + "," + campaignId + "," + campaignName
				+ "," + totalNumbers + "," + noOfRetry + "," + validCount + "," + invalidCount + "," + duplicateCount
				+ "," + blacklistCount + "," + totalDialed + "," + totalDnd + "," + discardedCount + ","
				+ gvSupportedCount + "," + leadStatus + "," + leadScheduleStartDtm + "," + leadScheduleEndDtm;

	}

	public int getGvSupportedCount() {
		return gvSupportedCount;
	}

	public void setGvSupportedCount(int gvSupportedCount) {
		this.gvSupportedCount = gvSupportedCount;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public Integer getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(Integer totalNumbers) {
		this.totalNumbers = totalNumbers;
	}

	public Integer getNoOfRetry() {
		return noOfRetry;
	}

	public void setNoOfRetry(Integer noOfRetry) {
		this.noOfRetry = noOfRetry;
	}

	public Integer getValidCount() {
		return validCount;
	}

	public void setValidCount(Integer validCount) {
		this.validCount = validCount;
	}

	public Integer getInvalidCount() {
		return invalidCount;
	}

	public void setInvalidCount(Integer invalidCount) {
		this.invalidCount = invalidCount;
	}

	public Integer getDuplicateCount() {
		return duplicateCount;
	}

	public void setDuplicateCount(Integer duplicateCount) {
		this.duplicateCount = duplicateCount;
	}

	public Integer getTotalDialed() {
		return totalDialed;
	}

	public void setTotalDialed(Integer totalDialed) {
		this.totalDialed = totalDialed;
	}

	public Integer getTotalDnd() {
		return totalDnd;
	}

	public void setTotalDnd(Integer totalDnd) {
		this.totalDnd = totalDnd;
	}

	public String getLeadStatus() {
		return leadStatus;
	}

	public void setLeadStatus(String leadStatus) {
		this.leadStatus = leadStatus;
	}

	public Integer getBlacklistCount() {
		return blacklistCount;
	}

	public void setBlacklistCount(Integer blacklistCount) {
		this.blacklistCount = blacklistCount;
	}

	public String getLeadCreateDtm() {
		return leadCreateDtm;
	}

	public void setLeadCreateDtm(String leadCreateDtm) {
		this.leadCreateDtm = leadCreateDtm;
	}

	public String getLeadScheduleStartDtm() {
		return leadScheduleStartDtm;
	}

	public void setLeadScheduleStartDtm(String leadScheduleStartDtm) {
		this.leadScheduleStartDtm = leadScheduleStartDtm;
	}

	public String getLeadScheduleEndDtm() {
		return leadScheduleEndDtm;
	}

	public void setLeadScheduleEndDtm(String leadScheduleEndDtm) {
		this.leadScheduleEndDtm = leadScheduleEndDtm;
	}

	public int getDiscardedCount() {
		return discardedCount;
	}

	public void setDiscardedCount(int discardedCount) {
		this.discardedCount = discardedCount;
	}

}
