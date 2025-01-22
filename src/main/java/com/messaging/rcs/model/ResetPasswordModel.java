package com.messaging.rcs.model;

public class ResetPasswordModel {
	private Long userId;
	
	/*
	 * @Pattern( regexp =
	 * "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,16}$",
	 * message =
	 * "Password must be 12-16 characters long, include at least one uppercase letter, one lowercase letter, one number, and one special character."
	 * )
	 */
	private String newPassword;
	 
	private String oldPassword;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	@Override
	public String toString() {
		return "ResetPasswordModel [userId=" + userId + ", newPassword=" + newPassword + ", oldPassword=" + oldPassword
				+ "]";
	}

}
