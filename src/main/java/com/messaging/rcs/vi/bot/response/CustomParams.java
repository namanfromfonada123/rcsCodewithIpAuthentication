package com.messaging.rcs.vi.bot.response;

public class CustomParams {
	private String date;
	private String amount;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "CustomParams [date=" + date + ", amount=" + amount + "]";
	}

}
