package com.messaging.rcs.domain;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.messaging.rcs.jwt.Constants;

/**
 * 
 * @author RahulRajput 2023-06-13
 *
 */
@Entity
@Table(name = "users")
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private Long userId;
	private String userName;
	private String firstName;
	private String lastName;
	private String userPassword;
	private String userType;
	private String email;
	private String phone;
	@Column(name = "ACTIVE") // , columnDefinition = "varchar(1) default 'Y'")
	private String active;
	@Column(name = "MULTIPLE_LOGIN_ALLOWED", columnDefinition = "varchar(1) default 'N'")
	private String multipleLoginAllowed;
	private long parentUserId;
	private String channelPriorityScheme;
	private String channelPrioritySchemeValue;
	@Column(columnDefinition = "integer default 0")
	private int isDeleted;
	@Column(columnDefinition = "integer default 5000000")
	private int dailyUsageLimit;
	@Column(name = "NOTIFICATION_REQUIRED", columnDefinition = "varchar(10) default 'N'")
	private String notificationRequired;
	@Column(name = "CREATED_BY")
	private String createdBy;
	@Column(name = "CREATED_DATE")
	private Date createdDate;
	@Column(name = "LAST_MODIFIED_BY")
	private String lastModifiedBy;
	@Column(name = "LAST_MODIFIED_DATE")
	private Date lastModifiedDate;
	private String botId;
	private String botToken;
	private String credentials;
	private String apiKey;
	private long smsCreditBalance;
	private long whatsAppCreditBalance;
	@Lob
	private byte[] companyBanner; // image
	private String companyBannerFileName;
	private String companyBannerFileType;
	@Lob
	private byte[] companyLogo; // image
	private String companyLogoFileName;
	private String companyLogoFileType;
	private String companyName;
	private long creditBalance;
	@Column(name = "acct_type")
	private String acctType;
	private String defaultMsg;
	private String smsDltPrincipleId;
	private Integer role_id;
	private String copyRight;
	private Integer menuPermission;
	private String aiVideo;
	private long totalRcsCredit;
	private long totalSMSCredit;
	private long totalWhatsAppCredit;

	private String otp;
	private String ipLists;

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getIpLists() {
		return ipLists;
	}

	public void setIpLists(String ipLists) {
		this.ipLists = ipLists;
	}
	
	public long getTotalRcsCredit() {
		return totalRcsCredit;
	}

	public void setTotalRcsCredit(long totalRcsCredit) {
		this.totalRcsCredit = totalRcsCredit;
	}

	public long getTotalSMSCredit() {
		return totalSMSCredit;
	}

	public void setTotalSMSCredit(long totalSMSCredit) {
		this.totalSMSCredit = totalSMSCredit;
	}

	public long getTotalWhatsAppCredit() {
		return totalWhatsAppCredit;
	}

	public void setTotalWhatsAppCredit(long totalWhatsAppCredit) {
		this.totalWhatsAppCredit = totalWhatsAppCredit;
	}

	public String getAiVideo() {
		return aiVideo;
	}

	public void setAiVideo(String aiVideo) {
		this.aiVideo = aiVideo;
	}

	public Integer getMenuPermission() {
		return menuPermission;
	}

	public void setMenuPermission(Integer menuPermission) {
		this.menuPermission = menuPermission;
	}

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false,
	 * insertable = false, updatable = false) private UsersRole role;
	 */
	public String getCopyRight() {
		return copyRight;
	}

	public void setCopyRight(String copyRight) {
		this.copyRight = copyRight;
	}

	/*
	 * public UsersRole getRole() { return role; }
	 * 
	 * public void setRole(UsersRole role) { this.role = role; }
	 */

	public Integer getRole_id() {
		return role_id;
	}

	public void setRole_id(Integer role_id) {
		this.role_id = role_id;
	}

	public long getWhatsAppCreditBalance() {
		return whatsAppCreditBalance;
	}

	public void setWhatsAppCreditBalance(long whatsAppCreditBalance) {
		this.whatsAppCreditBalance = whatsAppCreditBalance;
	}

	public long getSmsCreditBalance() {
		return smsCreditBalance;
	}

	public void setSmsCreditBalance(long smsCreditBalance) {
		this.smsCreditBalance = smsCreditBalance;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getCredentials() {
		return credentials;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public String getDefaultMsg() {
		return defaultMsg;
	}

	public void setDefaultMsg(String defaultMsg) {
		this.defaultMsg = defaultMsg;
	}

	public String getSmsDltPrincipleId() {
		return smsDltPrincipleId;
	}

	public void setSmsDltPrincipleId(String smsDltPrincipleId) {
		this.smsDltPrincipleId = smsDltPrincipleId;
	}

	public long getCreditBalance() {
		return creditBalance;
	}

	public void setCreditBalance(long creditBalance) {
		this.creditBalance = creditBalance;
	}

	public String getAcctType() {
		return acctType;
	}

	public void setAcctType(String acctType) {
		this.acctType = acctType;
	}

	public String getCompanyBannerFileName() {
		return companyBannerFileName;
	}

	public void setCompanyBannerFileName(String companyBannerFileName) {
		this.companyBannerFileName = companyBannerFileName;
	}

	public String getCompanyBannerFileType() {
		return companyBannerFileType;
	}

	public void setCompanyBannerFileType(String companyBannerFileType) {
		this.companyBannerFileType = companyBannerFileType;
	}

	public String getCompanyLogoFileName() {
		return companyLogoFileName;
	}

	public void setCompanyLogoFileName(String companyLogoFileName) {
		this.companyLogoFileName = companyLogoFileName;
	}

	public String getCompanyLogoFileType() {
		return companyLogoFileType;
	}

	public void setCompanyLogoFileType(String companyLogoFileType) {
		this.companyLogoFileType = companyLogoFileType;
	}

	public byte[] getCompanyBanner() {
		return companyBanner;
	}

	public void setCompanyBanner(byte[] companyBanner) {
		this.companyBanner = companyBanner;
	}

	public byte[] getCompanyLogo() {
		return companyLogo;
	}

	public void setCompanyLogo(byte[] companyLogo) {
		this.companyLogo = companyLogo;
	}

	@JsonFormat(pattern = Constants.defaultDateTimeFormat, timezone = Constants.defaultTimezone)
	private Date pwdResetDate;

	public Date getPwdResetDate() {
		return pwdResetDate;
	}

	public void setPwdResetDate(Date pwdResetDate) {
		this.pwdResetDate = pwdResetDate;
	}

	public String getBotId() {
		return botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public String getBotToken() {
		return botToken;
	}

	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public long getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(long parentUserId) {
		this.parentUserId = parentUserId;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getMultipleLoginAllowed() {
		return multipleLoginAllowed;
	}

	public void setMultipleLoginAllowed(String multipleLoginAllowed) {
		this.multipleLoginAllowed = multipleLoginAllowed;
	}

	public String getChannelPrioritySchemeValue() {
		return channelPrioritySchemeValue;
	}

	public void setChannelPrioritySchemeValue(String channelPrioritySchemeValue) {
		this.channelPrioritySchemeValue = channelPrioritySchemeValue;
	}

	public String getChannelPriorityScheme() {
		return channelPriorityScheme;
	}

	public void setChannelPriorityScheme(String channelPriorityScheme) {
		this.channelPriorityScheme = channelPriorityScheme;
	}

	public String getNotificationRequired() {
		return notificationRequired;
	}

	public void setNotificationRequired(String notificationRequired) {
		this.notificationRequired = notificationRequired;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public int getDailyUsageLimit() {
		return dailyUsageLimit;
	}

	public void setDailyUsageLimit(int dailyUsageLimit) {
		this.dailyUsageLimit = dailyUsageLimit;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "UserEntity [userId=" + userId + ", userName=" + userName + ", firstName=" + firstName + ", lastName="
				+ lastName + ", userPassword=" + userPassword + ", userType=" + userType + ", email=" + email
				+ ", phone=" + phone + ", active=" + active + ", multipleLoginAllowed=" + multipleLoginAllowed
				+ ", parentUserId=" + parentUserId + ", channelPriorityScheme=" + channelPriorityScheme
				+ ", channelPrioritySchemeValue=" + channelPrioritySchemeValue + ", isDeleted=" + isDeleted
				+ ", dailyUsageLimit=" + dailyUsageLimit + ", notificationRequired=" + notificationRequired
				+ ", createdBy=" + createdBy + ", createdDate=" + createdDate + ", lastModifiedBy=" + lastModifiedBy
				+ ", lastModifiedDate=" + lastModifiedDate + ", botId=" + botId + ", botToken=" + botToken
				+ ", credentials=" + credentials + ", apiKey=" + apiKey + ", smsCreditBalance=" + smsCreditBalance
				+ ", whatsAppCreditBalance=" + whatsAppCreditBalance + ", companyBanner="
				+ Arrays.toString(companyBanner) + ", companyBannerFileName=" + companyBannerFileName
				+ ", companyBannerFileType=" + companyBannerFileType + ", companyLogo=" + Arrays.toString(companyLogo)
				+ ", companyLogoFileName=" + companyLogoFileName + ", companyLogoFileType=" + companyLogoFileType
				+ ", companyName=" + companyName + ", creditBalance=" + creditBalance + ", acctType=" + acctType
				+ ", defaultMsg=" + defaultMsg + ", smsDltPrincipleId=" + smsDltPrincipleId + ", role_id=" + role_id
				+ ", copyRight=" + copyRight + ", menuPermission=" + menuPermission + ", aiVideo=" + aiVideo
				+ ", totalRcsCredit=" + totalRcsCredit + ", totalSMSCredit=" + totalSMSCredit + ", totalWhatsAppCredit="
				+ totalWhatsAppCredit + ", pwdResetDate=" + pwdResetDate + "]";
	}

}
