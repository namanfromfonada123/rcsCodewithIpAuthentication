package com.messaging.rcs.summary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rcs_summary")
public class RcsSummaryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "created_date")
	private String created_date;

	@Column(name = "lead_name")
	private String lead_name;

	@Column(name = "last_modified_date")
	private String last_modified_date;

	@Column(name = "status")
	private String status;

	@Column(name = "total")
	private String TOTAL;

	@Column(name = "campaign_name")
	private String campaing_name;

	@Column(name = "nonrcs_failed")
	private String NonRCS_FAILED;

	@Column(name = "submitted")
	private String submitted;

	@Column(name = "sent")
	private String sent;

	@Column(name = "delivered")
	private String Delivered;

	@Column(name = "read_no")
	private String Read;

	@Column(name = "failed")
	private String failed;

	@Column(name = "invalid")
	private String Invalid;

	@Column(name = "user_name")
	private String user_name;
	@Column(name = "lead_id")
	private String leadId;
	private String rcs_msg_type_id;
	private String nonrcs_fallback_sms_submit;
	private String nonrcs_sms_submit;
	private String delliveryFailed;
	private String submitFailed;
	private String deliverySuccess;

	public String getDelliveryFailed() {
		return delliveryFailed;
	}

	public void setDelliveryFailed(String delliveryFailed) {
		this.delliveryFailed = delliveryFailed;
	}

	public String getSubmitFailed() {
		return submitFailed;
	}

	public void setSubmitFailed(String submitFailed) {
		this.submitFailed = submitFailed;
	}

	public String getDeliverySuccess() {
		return deliverySuccess;
	}

	public void setDeliverySuccess(String deliverySuccess) {
		this.deliverySuccess = deliverySuccess;
	}

	public String getNonrcs_fallback_sms_submit() {
		return nonrcs_fallback_sms_submit;
	}

	public void setNonrcs_fallback_sms_submit(String nonrcs_fallback_sms_submit) {
		this.nonrcs_fallback_sms_submit = nonrcs_fallback_sms_submit;
	}

	public String getNonrcs_sms_submit() {
		return nonrcs_sms_submit;
	}

	public void setNonrcs_sms_submit(String nonrcs_sms_submit) {
		this.nonrcs_sms_submit = nonrcs_sms_submit;
	}

	public String getRcs_msg_type_id() {
		return rcs_msg_type_id;
	}

	public void setRcs_msg_type_id(String rcs_msg_type_id) {
		this.rcs_msg_type_id = rcs_msg_type_id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCreated_date() {
		return created_date;
	}

	public void setCreated_date(String created_date) {
		this.created_date = created_date;
	}

	public String getLead_name() {
		return lead_name;
	}

	public void setLead_name(String lead_name) {
		this.lead_name = lead_name;
	}

	public String getLast_modified_date() {
		return last_modified_date;
	}

	public void setLast_modified_date(String last_modified_date) {
		this.last_modified_date = last_modified_date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTOTAL() {
		return TOTAL;
	}

	public void setTOTAL(String tOTAL) {
		TOTAL = tOTAL;
	}

	public String getCampaing_name() {
		return campaing_name;
	}

	public void setCampaing_name(String campaing_name) {
		this.campaing_name = campaing_name;
	}

	public String getNonRCS_FAILED() {
		return NonRCS_FAILED;
	}

	public void setNonRCS_FAILED(String nonRCS_FAILED) {
		NonRCS_FAILED = nonRCS_FAILED;
	}

	public String getSubmitted() {
		return submitted;
	}

	public void setSubmitted(String submitted) {
		this.submitted = submitted;
	}

	public String getSent() {
		return sent;
	}

	public void setSent(String sent) {
		this.sent = sent;
	}

	public String getDelivered() {
		return Delivered;
	}

	public void setDelivered(String delivered) {
		Delivered = delivered;
	}

	public String getRead() {
		return Read;
	}

	public void setRead(String read) {
		Read = read;
	}

	public String getFailed() {
		return failed;
	}

	public void setFailed(String failed) {
		this.failed = failed;
	}

	public String getInvalid() {
		return Invalid;
	}

	public void setInvalid(String invalid) {
		Invalid = invalid;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getLeadId() {
		return leadId;
	}

	public void setLeadId(String leadId) {
		this.leadId = leadId;
	}

	@Override
	public String toString() {
		return "RcsSummaryEntity [id=" + id + ", created_date=" + created_date + ", lead_name=" + lead_name
				+ ", last_modified_date=" + last_modified_date + ", status=" + status + ", TOTAL=" + TOTAL
				+ ", campaing_name=" + campaing_name + ", NonRCS_FAILED=" + NonRCS_FAILED + ", submitted=" + submitted
				+ ", sent=" + sent + ", Delivered=" + Delivered + ", Read=" + Read + ", failed=" + failed + ", Invalid="
				+ Invalid + ", user_name=" + user_name + ", leadId=" + leadId + ", rcs_msg_type_id=" + rcs_msg_type_id
				+ ", nonrcs_fallback_sms_submit=" + nonrcs_fallback_sms_submit + ", nonrcs_sms_submit="
				+ nonrcs_sms_submit + "]";
	}

}
