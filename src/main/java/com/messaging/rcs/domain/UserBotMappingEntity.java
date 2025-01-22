package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "user_bot_mapping")
public class UserBotMappingEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String botName;
	private String botId;
	private String botType;
	private String brand;
	private String basicToken;
	private String secretKey;
    private String operator;
    
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getBasicToken() {
		return basicToken;
	}

	public void setBasicToken(String basicToken) {
		this.basicToken = basicToken;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getBotType() {
		return botType;
	}

	public void setBotType(String botType) {
		this.botType = botType;
	}

	@Column(name = "user_id")
	private Long user_id;
	
	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable =
	 * false, insertable = false, updatable = false) private UserEntity role;
	 * 
	 * public UserEntity getRole() { return role; }
	 * 
	 * public void setRole(UserEntity role) { this.role = role; }
	 */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBotName() {
		return botName;
	}

	public void setBotName(String botName) {
		this.botName = botName;
	}

	public String getBotId() {
		return botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	@Override
	public String toString() {
		return "UserBotMappingEntity [id=" + id + ", botName=" + botName + ", botId=" + botId + ", botType=" + botType
				+ ", brand=" + brand + ", basicToken=" + basicToken + ", secretKey=" + secretKey + ", operator="
				+ operator + ", user_id=" + user_id + "]";
	}

}
