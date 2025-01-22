package com.messaging.rcs.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.messaging.rcs.util.CustomDateDeserializer;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author Rahul
 *
 */
@Entity
@Table(name = "schedule_info")
public class ScheduleEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SCHEDULE_ID")
    private Long scheduleId;

    @Column(name = "SCHEDULE_DAY")
    private String scheduleDay;//1,2,3,4,5,6,7

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SCHEDULE_START_DTM")
    private Date scheduleStartDtm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SCHEDULE_END_DTM")
    private Date scheduleEndDtm;

    @Column(name = "WINDOW_START_TIME", length = 10)
    private String windowStartTime;
    @Column(name = "WINDOW_END_TIME", length = 10)
    private String windowEndTime;
    @Column(name = "WINDOW_REQUIRED", columnDefinition = "varchar(1) default 'N'")
    private String windowRequired;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED_DATE")
    private Date lastModifiedDate;

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

    @JsonDeserialize(using = CustomDateDeserializer.class)
    public void setScheduleStartDtm(Date scheduleStartDtm) {
        this.scheduleStartDtm = scheduleStartDtm;
    }

    public Date getScheduleEndDtm() {
        return scheduleEndDtm;
    }

    @JsonDeserialize(using = CustomDateDeserializer.class)
    public void setScheduleEndDtm(Date scheduleEndDtm) {
        this.scheduleEndDtm = scheduleEndDtm;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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

    @Override
    public String toString() {
        return "ScheduleEntity{" +
                "scheduleId=" + scheduleId +
                ", scheduleDay='" + scheduleDay + '\'' +
                ", scheduleStartDtm=" + scheduleStartDtm +
                ", scheduleEndDtm=" + scheduleEndDtm +
                '}';
    }
}
