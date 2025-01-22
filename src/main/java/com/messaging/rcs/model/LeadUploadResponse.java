package com.messaging.rcs.model;

public class LeadUploadResponse {

	private String leadName;
	private Integer totalRecords;
	private String uploadDtm;
	private String Resid;

	public String getLeadName() {
		return leadName;
	}

	public void setLeadName(String leadName) {
		this.leadName = leadName;
	}

	public Integer getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public String getUploadDtm() {
		return uploadDtm;
	}

	public void setUploadDtm(String uploadDtm) {
		this.uploadDtm = uploadDtm;
	}

	public String getResid() {
		return Resid;
	}

	public void setResid(String resid) {
		Resid = resid;
	}

	@Override
	public String toString() {
		return "LeadUploadResponse [leadName=" + leadName + ", totalRecords=" + totalRecords + ", uploadDtm="
				+ uploadDtm + ", Resid=" + Resid + "]";
	}

}
