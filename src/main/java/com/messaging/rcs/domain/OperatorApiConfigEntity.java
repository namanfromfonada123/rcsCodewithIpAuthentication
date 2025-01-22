package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "operator_config_api")
public class OperatorApiConfigEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "api_config_id")
	private Long apiConfigId;
	private Long operatorId;
	private String createdDate;
	private String updatedDate;
	private String apiUrl;
	private String apiType;
	private String basicToken;
	private String botId;
	private String secretKey;

	public String getBotId() {
		return botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getBasicToken() {
		return basicToken;
	}

	public void setBasicToken(String basicToken) {
		this.basicToken = basicToken;
	}

	public Long getApiConfigId() {
		return apiConfigId;
	}

	public void setApiConfigId(Long apiConfigId) {
		this.apiConfigId = apiConfigId;
	}

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

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	@Override
	public String toString() {
		return "OperatorApiConfigEntity [apiConfigId=" + apiConfigId + ", operatorId=" + operatorId + ", createdDate="
				+ createdDate + ", updatedDate=" + updatedDate + ", apiUrl=" + apiUrl + ", apiType=" + apiType
				+ ", basicToken=" + basicToken + "]";
	}

}
