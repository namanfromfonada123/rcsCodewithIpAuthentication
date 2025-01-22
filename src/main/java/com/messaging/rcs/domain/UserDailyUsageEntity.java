package com.messaging.rcs.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by sbsingh on Nov/03/2020.
 */

@Entity
@Table(name = "user_daily_usage")
public class UserDailyUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Date uploadDate;
    private int uploadCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(int uploadCount) {
        this.uploadCount = uploadCount;
    }
}
