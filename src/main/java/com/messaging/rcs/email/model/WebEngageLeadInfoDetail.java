package com.messaging.rcs.email.model;

import java.io.Serializable;

public class WebEngageLeadInfoDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String phoneNumber;
	private String additonalDataInfoText2;
	private String additonalDataInfoText;
	private String textFields;

	public String getTextFields() {
		return textFields;
	}

	public void setTextFields(String textFields) {
		this.textFields = textFields;
	}

	public String getAdditonalDataInfoText2() {
		return additonalDataInfoText2;
	}

	public void setAdditonalDataInfoText2(String additonalDataInfoText2) {
		this.additonalDataInfoText2 = additonalDataInfoText2;
	}

	public String getAdditonalDataInfoText() {
		return additonalDataInfoText;
	}

	public void setAdditonalDataInfoText(String additonalDataInfoText) {
		this.additonalDataInfoText = additonalDataInfoText;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String toString() {
		return "WebEngageLeadInfoDetail [phoneNumber=" + phoneNumber + ", additonalDataInfoText2="
				+ additonalDataInfoText2 + ", additonalDataInfoText=" + additonalDataInfoText + ", textFields="
				+ textFields + "]";
	}

	

}
