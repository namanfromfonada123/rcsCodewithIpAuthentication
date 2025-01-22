package com.messaging.rcs.rbm.messages;

import com.google.api.client.util.Key;

/**
 * Created by sbsingh on Dec/26/2021.
 */
public class MyCreateCalendarEventAction {
    private String description;
    private String endTime;
    private String startTime;
    private String title;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
