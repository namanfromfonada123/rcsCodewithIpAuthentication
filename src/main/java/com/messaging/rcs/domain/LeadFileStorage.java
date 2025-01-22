package com.messaging.rcs.domain;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "lead_file_storage")
public class LeadFileStorage {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "lead_file_id")
	private Long lead_file_id;
	private Long leadId;
	private boolean isDnd;
	private boolean isDuplicate;
	private String createdDate;
	private String leadFileName;
	private String leadFileType;
	private Integer isSchedule;

	public Integer getIsSchedule() {
		return isSchedule;
	}

	public void setIsSchedule(Integer isSchedule) {
		this.isSchedule = isSchedule;
	}

	@Lob
	private byte[] data;

	public LeadFileStorage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LeadFileStorage(Long leadId, Integer isSchedule, boolean isDnd, boolean isDuplicate, String createdDate,
			String leadFileName, String leadFileType, byte[] data) {
		this.leadId = leadId;
		this.isSchedule = isSchedule;
		this.isDnd = isDnd;
		this.isDuplicate = isDuplicate;
		this.createdDate = createdDate;
		this.leadFileName = leadFileName;
		this.leadFileType = leadFileType;
		this.data = data;
	}

	public Long getLead_file_id() {
		return lead_file_id;
	}

	public void setLead_file_id(Long lead_file_id) {
		this.lead_file_id = lead_file_id;
	}

	public Long getLeadId() {
		return leadId;
	}

	public void setLeadId(Long leadId) {
		this.leadId = leadId;
	}

	public boolean isDnd() {
		return isDnd;
	}

	public void setDnd(boolean isDnd) {
		this.isDnd = isDnd;
	}

	public boolean isDuplicate() {
		return isDuplicate;
	}

	public void setDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getLeadFileName() {
		return leadFileName;
	}

	public void setLeadFileName(String leadFileName) {
		this.leadFileName = leadFileName;
	}

	public String getLeadFileType() {
		return leadFileType;
	}

	public void setLeadFileType(String leadFileType) {
		this.leadFileType = leadFileType;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "LeadFileStorage [lead_file_id=" + lead_file_id + ", leadId=" + leadId + ", isDnd=" + isDnd
				+ ", isDuplicate=" + isDuplicate + ", createdDate=" + createdDate + ", leadFileName=" + leadFileName
				+ ", leadFileType=" + leadFileType + ", data=" + Arrays.toString(data) + "]";
	}

}
