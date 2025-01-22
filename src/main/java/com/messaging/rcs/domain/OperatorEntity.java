package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "operator")
public class OperatorEntity {

	@Id
	@Column(name = "operator_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long operatorId;
	private String createdDate;
	private String operatorName;

	public Long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Long operatorId) {
		this.operatorId = operatorId;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	@Override
	public String toString() {
		return "OperatorEntity [operatorId=" + operatorId + ", createdDate=" + createdDate + ", operatorName="
				+ operatorName + "]";
	}

}
