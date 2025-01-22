package com.messaging.rcs.vi.bot.response;

public class Reason {
	public String text;
	public int code;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return "Reason [text=" + text + ", code=" + code + "]";
	}
	
	
}
