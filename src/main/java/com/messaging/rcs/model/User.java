package com.messaging.rcs.model;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

/**
 * 
 * @author RahulRajput 18-08-2023
 *
 */
public class User {// extends BaseModel {

	private long userId;
	@NotNull(message = "User Name Cannot be NULL")
	private String userName;
	@NotNull(message = "First Name Cannot be NULL")
	private String firstName;
	@NotNull(message = "Last Name Cannot be NULL")
	private String lastName;
	@NotNull(message = "Password Cannot be NULL")
	private String userPassword;
	private String userType;
	@NotNull(message = "Email Cannot be NULL")
	private String email;
	@NotNull(message = "Phone Number Cannot be NULL")
	private String phone;
	private String active;
	private String multipleLoginAllowed;
	private String channelPriorityScheme;
	private String channelPrioritySchemeValue;
	private long parentUserId;
	private String notificationRequired;
	private int dailyUsageLimit;
	private int isDeleted;
	private String botId;
	private String botToken;
	private String acctType;

	public String getAcctType() {
		return acctType;
	}

	public void setAcctType(String acctType) {
		this.acctType = acctType;
	}

	private byte[] companyBanner; // image
	private String companyBannerFileName;
	private String companyBannerFileType;

	private byte[] companyLogo; // image
	private String companyLogoFileName;
	private String companyLogoFileType;
	private String companyName;
	private String apiKey;
	private Integer role_id;
	private String copyRight;
	private Integer menuPermission;
	private String aiVideo;
	private Integer totalRcsCredit;
	private Integer totalSMSCredit;
	private Integer totalWhatsAppCredit;

	public Integer getTotalRcsCredit() {
		return totalRcsCredit;
	}

	public void setTotalRcsCredit(Integer totalRcsCredit) {
		this.totalRcsCredit = totalRcsCredit;
	}

	public Integer getTotalSMSCredit() {
		return totalSMSCredit;
	}

	public void setTotalSMSCredit(Integer totalSMSCredit) {
		this.totalSMSCredit = totalSMSCredit;
	}

	public Integer getTotalWhatsAppCredit() {
		return totalWhatsAppCredit;
	}

	public void setTotalWhatsAppCredit(Integer totalWhatsAppCredit) {
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

	public Integer getRole_id() {
		return role_id;
	}

	public void setRole_id(Integer role_id) {
		this.role_id = role_id;
	}

	public String getCopyRight() {
		return copyRight;
	}

	public void setCopyRight(String copyRight) {
		this.copyRight = copyRight;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public byte[] getCompanyBanner() {
		return companyBanner;
	}

	public void setCompanyBanner(byte[] companyBanner) {
		this.companyBanner = companyBanner;
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

	public byte[] getCompanyLogo() {
		return companyLogo;
	}

	public void setCompanyLogo(byte[] companyLogo) {
		this.companyLogo = companyLogo;
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

	public int getDailyUsageLimit() {
		return dailyUsageLimit;
	}

	public void setDailyUsageLimit(int dailyUsageLimit) {
		this.dailyUsageLimit = dailyUsageLimit;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public long getUserId() {
		return userId;
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

	public String getMultipleLoginAllowed() {
		return multipleLoginAllowed;
	}

	public void setMultipleLoginAllowed(String multipleLoginAllowed) {
		this.multipleLoginAllowed = multipleLoginAllowed;
	}

	public long getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(long parentUserId) {
		this.parentUserId = parentUserId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getChannelPriorityScheme() {
		return channelPriorityScheme;
	}

	public void setChannelPriorityScheme(String channelPriorityScheme) {
		this.channelPriorityScheme = channelPriorityScheme;
	}

	public String getChannelPrioritySchemeValue() {
		return channelPrioritySchemeValue;
	}

	public void setChannelPrioritySchemeValue(String channelPrioritySchemeValue) {
		this.channelPrioritySchemeValue = channelPrioritySchemeValue;
	}

	public String getNotificationRequired() {
		return notificationRequired;
	}

	public void setNotificationRequired(String notificationRequired) {
		this.notificationRequired = notificationRequired;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", userName=" + userName + ", firstName=" + firstName + ", lastName="
				+ lastName + ", userPassword=" + userPassword + ", userType=" + userType + ", email=" + email
				+ ", phone=" + phone + ", active=" + active + ", multipleLoginAllowed=" + multipleLoginAllowed
				+ ", channelPriorityScheme=" + channelPriorityScheme + ", channelPrioritySchemeValue="
				+ channelPrioritySchemeValue + ", parentUserId=" + parentUserId + ", notificationRequired="
				+ notificationRequired + ", dailyUsageLimit=" + dailyUsageLimit + ", isDeleted=" + isDeleted
				+ ", botId=" + botId + ", botToken=" + botToken + ", companyBanner=" + Arrays.toString(companyBanner)
				+ ", companyBannerFileName=" + companyBannerFileName + ", companyBannerFileType="
				+ companyBannerFileType + ", companyLogo=" + Arrays.toString(companyLogo) + ", companyLogoFileName="
				+ companyLogoFileName + ", companyLogoFileType=" + companyLogoFileType + ", companyName=" + companyName
				+ ", apiKey=" + apiKey + ", role_id=" + role_id + ", copyRight=" + copyRight + ", menuPermission="
				+ menuPermission + "]";
	}

}
