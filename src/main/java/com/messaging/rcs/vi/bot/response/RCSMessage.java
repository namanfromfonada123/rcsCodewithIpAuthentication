package com.messaging.rcs.vi.bot.response;

import java.util.Date;

public class RCSMessage {
	public String msgId;
	public String status;
	public Date timestamp;

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "RCSMessage [msgId=" + msgId + ", status=" + status + ", timestamp=" + timestamp + "]";
	}

}
