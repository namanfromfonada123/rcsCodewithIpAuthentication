package com.messaging.rcs.service;

/**
 * Created by sbsingh on 4/13/19.
 */
public interface EventNotificationI {

    void notifyEventCaptured(String eventType, Object... pArgs);
}
