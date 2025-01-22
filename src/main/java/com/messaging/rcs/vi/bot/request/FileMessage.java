package com.messaging.rcs.vi.bot.request;

/**
 * createdOn:: 2023-04-01
 * 
 * @author Rahul
 *
 */
public class FileMessage {
	public String fileUrl;

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	@Override
	public String toString() {
		return "FileMessage [fileUrl=" + fileUrl + "]";
	}

}
