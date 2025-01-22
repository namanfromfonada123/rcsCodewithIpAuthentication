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
@Table(name = "BlackListUploadFile")
public class BlackListUploadFile {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	private String name;
	private String type;
	private Long userId;
	private String createdDate;
	private Integer isExecute;

	public Integer getIsExecute() {
		return isExecute;
	}

	public void setIsExecute(Integer isExecute) {
		this.isExecute = isExecute;
	}

	@Lob
	private byte[] data;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	public String toString() {
		return "BlackListUploadFile [id=" + id + ", name=" + name + ", type=" + type + ", userId=" + userId
				+ ", createdDate=" + createdDate + ", isExecute=" + isExecute + ", data=" + Arrays.toString(data) + "]";
	}

}
