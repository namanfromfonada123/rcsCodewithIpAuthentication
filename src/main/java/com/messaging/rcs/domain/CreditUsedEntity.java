package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author rahul
 * @Created Date 11-12-2023 Monday
 */
@Entity
@Table(name = "credit_used")
public class CreditUsedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "crdId")
	private Long crdId;
	private String acctType;
	private long acctCredit;
	private long acctDebit;
	private long acctBalance;
	private String createdDate;
	private String updateDate;
	private String userId;
	private String remarks;
	private String msgType;
    private long parentUserId;
    
	public long getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(long parentUserId) {
		this.parentUserId = parentUserId;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Long getCrdId() {
		return crdId;
	}

	public void setCrdId(Long crdId) {
		this.crdId = crdId;
	}

	public String getAcctType() {
		return acctType;
	}

	public void setAcctType(String acctType) {
		this.acctType = acctType;
	}

	public long getAcctCredit() {
		return acctCredit;
	}

	public void setAcctCredit(long acctCredit) {
		this.acctCredit = acctCredit;
	}

	public long getAcctDebit() {
		return acctDebit;
	}

	public void setAcctDebit(long acctDebit) {
		this.acctDebit = acctDebit;
	}

	public long getAcctBalance() {
		return acctBalance;
	}

	public void setAcctBalance(long acctBalance) {
		this.acctBalance = acctBalance;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public String toString() {
		return "CreditUsedEntity [crdId=" + crdId + ", acctType=" + acctType + ", acctCredit=" + acctCredit
				+ ", acctDebit=" + acctDebit + ", acctBalance=" + acctBalance + ", createdDate=" + createdDate
				+ ", updateDate=" + updateDate + ", userId=" + userId + ", remarks=" + remarks + ", msgType=" + msgType
				+ "]";
	}

}
