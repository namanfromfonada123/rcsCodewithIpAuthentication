package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "url_click_details")
public class urlClickDetails {

	@Id
	@Column(name = "id")
	private Integer id;
	@Column(name = "long_url")
	private String longUrl;
	@Column(name = "short_url")
	private String shortUrl;
	@Column(name = "clientid")
	private int clientid;
	@Column(name = "msisdn")
	private String msisdn;
	@Column(name = "ua")
	private String ua;
	@Column(name = "ip")
	private String ip;
	@Column(name = "device")
	private String device;
	@Column(name = "os")
	private String os;
	@Column(name = "clicks")
	private Integer clicks;
	@Column(name = "city")
	private String city;
	@Column(name = "country")
	private String country;
	@Column(name = "postal_code")
	private String postalCode;
	@Column(name = "state")
	private String state;
	@Column(name = "expiry_at")
	private String expiryAt;
	@Column(name = "tracking_id")
	private String trackingId;
	@Column(name = "created_date")
	private String createdDate;
	@Column(name = "clickid")
	private String clickid;
	@Column(name = "click_api_status")
	private Integer clickApiStatus;
	@Column(name="transaction_id")
	private String transactionId;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getLongUrl() {
		return longUrl;
	}
	public void setLongUrl(String longUrl) {
		this.longUrl = longUrl;
	}
	public String getShortUrl() {
		return shortUrl;
	}
	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}
	public int getClientid() {
		return clientid;
	}
	public void setClientid(int clientid) {
		this.clientid = clientid;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getUa() {
		return ua;
	}
	public void setUa(String ua) {
		this.ua = ua;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public Integer getClicks() {
		return clicks;
	}
	public void setClicks(Integer clicks) {
		this.clicks = clicks;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getExpiryAt() {
		return expiryAt;
	}
	public void setExpiryAt(String expiryAt) {
		this.expiryAt = expiryAt;
	}
	public String getTrackingId() {
		return trackingId;
	}
	public void setTrackingId(String trackingId) {
		this.trackingId = trackingId;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getClickid() {
		return clickid;
	}
	public void setClickid(String clickid) {
		this.clickid = clickid;
	}
	public Integer getClickApiStatus() {
		return clickApiStatus;
	}
	public void setClickApiStatus(Integer clickApiStatus) {
		this.clickApiStatus = clickApiStatus;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

}
