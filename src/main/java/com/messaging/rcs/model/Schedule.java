package com.messaging.rcs.model;

import java.util.Date;

/**
 * 
 * @author Rahul
 *
 */
public class Schedule extends BaseModel{
    private Long scheduleId;

    private String scheduleDay;//1,2,3,4,5,6,7

    private Date scheduleStartDtm;

    private Date scheduleEndDtm;
    private String windowStartTime;
    private String windowEndTime;
    private String windowRequired;

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

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

    public String getWindowStartTime() {
        return windowStartTime;
    }

    public void setWindowStartTime(String windowStartTime) {
        this.windowStartTime = windowStartTime;
    }

    public String getWindowEndTime() {
        return windowEndTime;
    }

    public void setWindowEndTime(String windowEndTime) {
        this.windowEndTime = windowEndTime;
    }

    public String getWindowRequired() {
        return windowRequired;
    }

    public void setWindowRequired(String windowRequired) {
        this.windowRequired = windowRequired;
    }
}
