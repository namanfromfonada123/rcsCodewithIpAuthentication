package com.messaging.rcs.model;

public class RcsSummarySmsData {

	private String created_date;
	private String LEAD_ID;
	private String last_modified_date;
	private String STATUS;
	private String TOTAL;
	private String NonRCS_FAILED;
	private String SUBMITTED;
	private String Delivered;
    private String count;
    
	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public void setNonRCS_FAILED(String nonRCS_FAILED) {
		NonRCS_FAILED = nonRCS_FAILED;
	}

	public String getNonRCS_FAILED() {
		return NonRCS_FAILED;
	}

	public void setNonRCS_FAILE(String nonRCS_FAILED) {
		NonRCS_FAILED = nonRCS_FAILED;
	}

	public String getSUBMITTED() {
		return SUBMITTED;
	}

	public void setSUBMITTED(String sUBMITTED) {
		SUBMITTED = sUBMITTED;
	}

	public String getDelivered() {
		return Delivered;
	}

	public void setDelivered(String delivered) {
		Delivered = delivered;
	}

	public String getCreated_date() {
		return created_date;
	}

	public void setCreated_date(String created_date) {
		this.created_date = created_date;
	}

	public String getLEAD_ID() {
		return LEAD_ID;
	}

	public void setLEAD_ID(String lEAD_ID) {
		LEAD_ID = lEAD_ID;
	}

	public String getLast_modified_date() {
		return last_modified_date;
	}

	public void setLast_modified_date(String last_modified_date) {
		this.last_modified_date = last_modified_date;
	}

	public String getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(String sTATUS) {
		STATUS = sTATUS;
	}

	public String getTOTAL() {
		return TOTAL;
	}

	public void setTOTAL(String tOTAL) {
		TOTAL = tOTAL;
	}

	@Override
	public String toString() {
		return "RcsSummarySmsData [created_date=" + created_date + ", LEAD_ID=" + LEAD_ID + ", last_modified_date="
				+ last_modified_date + ", STATUS=" + STATUS + ", TOTAL=" + TOTAL + ", NonRCS_FAILED=" + NonRCS_FAILED
				+ ", SUBMITTED=" + SUBMITTED + ", Delivered=" + Delivered + ", count=" + count + "]";
	}

}
