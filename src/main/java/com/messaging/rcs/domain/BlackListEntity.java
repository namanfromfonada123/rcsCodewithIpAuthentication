package com.messaging.rcs.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "black_list")
public class BlackListEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	private Integer prefix;
	private String phoneNumber;
	private Long userId;
	private Date createDtm;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getPrefix() {
		return prefix;
	}

	public void setPrefix(Integer prefix) {
		this.prefix = prefix;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(Date createDtm) {
		this.createDtm = createDtm;
	}
	
	@Override
	public String toString() {
		return "BlackListEntity [id=" + id + ", prefix=" + prefix + ", phoneNumber=" + phoneNumber + ", userId="
				+ userId + ", createDtm=" + createDtm + "]";
	}
}
