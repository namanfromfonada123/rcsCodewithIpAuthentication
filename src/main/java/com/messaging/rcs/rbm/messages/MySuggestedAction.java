package com.messaging.rcs.rbm.messages;

/**
 * Created by sbsingh on Dec/26/2021.
 */
public class MySuggestedAction {
    private MyCreateCalendarEventAction createCalendarEventAction;
    private MyOpenUrlAction openUrlAction;
    private MyDialAction dialAction;
    private String fallbackUrl;
    private boolean shareLocationAction = false;
    private MyViewLocationAction viewLocationAction;
    private String text;
    private String postbackData;

    public MyCreateCalendarEventAction getCreateCalendarEventAction() {
        return createCalendarEventAction;
    }

    public void setCreateCalendarEventAction(MyCreateCalendarEventAction createCalendarEventAction) {
        this.createCalendarEventAction = createCalendarEventAction;
    }

    public MyDialAction getDialAction() {
        return dialAction;
    }

    public void setDialAction(MyDialAction dialAction) {
        this.dialAction = dialAction;
    }

    public String getFallbackUrl() {
        return fallbackUrl;
    }

    public void setFallbackUrl(String fallbackUrl) {
        this.fallbackUrl = fallbackUrl;
    }

    public boolean isShareLocationAction() {
        return shareLocationAction;
    }

    public void setShareLocationAction(boolean shareLocationAction) {
        this.shareLocationAction = shareLocationAction;
    }

    public MyViewLocationAction getViewLocationAction() {
        return viewLocationAction;
    }

    public void setViewLocationAction(MyViewLocationAction viewLocationAction) {
        this.viewLocationAction = viewLocationAction;
    }

    public MyOpenUrlAction getOpenUrlAction() {
        return openUrlAction;
    }

    public void setOpenUrlAction(MyOpenUrlAction openUrlAction) {
        this.openUrlAction = openUrlAction;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostbackData() {
        return postbackData;
    }

    public void setPostbackData(String postbackData) {
        this.postbackData = postbackData;
    }
}
