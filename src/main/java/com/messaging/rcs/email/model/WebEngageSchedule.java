package com.messaging.rcs.email.model;

import java.io.Serializable;
import java.util.Date;

public class WebEngageSchedule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String scheduleDay;// 1,2,3,4,5,6,7

	private Date scheduleStartDtm;

	private Date scheduleEndDtm;

	public String getScheduleDay() {
		return scheduleDay;
	}

	public void setScheduleDay(String scheduleDay) {
		this.scheduleDay = scheduleDay;
	}

	public Date getScheduleStartDtm() {
		return scheduleStartDtm;
	}

	public void setScheduleStartDtm(Date scheduleStartDtm) {
		this.scheduleStartDtm = scheduleStartDtm;
	}

	public Date getScheduleEndDtm() {
		return scheduleEndDtm;
	}

	public void setScheduleEndDtm(Date scheduleEndDtm) {
		this.scheduleEndDtm = scheduleEndDtm;
	}

	@Override
	public String toString() {
		return "WebEngageSchedule [scheduleDay=" + scheduleDay + ", scheduleStartDtm=" + scheduleStartDtm
				+ ", scheduleEndDtm=" + scheduleEndDtm + "]";
	}

}
