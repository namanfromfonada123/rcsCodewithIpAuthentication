package com.messaging.rcs.service;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by sbsingh on 4/13/19.
 */
public class Notifications extends NotificationBase {

    private static final Notifications sInstance = new Notifications();

    private Notifications() {}

    public static Notifications getsInstance() { return sInstance;}

    // Scan notification listeners
    private final SortedMap<Integer, EventNotificationI> mEventNotificationListeners =
            new TreeMap<Integer, EventNotificationI>(getListenerComparator());

    public void registerEventNotificationListener(EventNotificationI pListener, int pPriority)
    {
        registerListener(pListener, pPriority, mEventNotificationListeners, "Event Notification");
    }

    public void notifyEventCaptured(String eventName, Object... pArgs)
    {
        synchronized (mEventNotificationListeners)
        {
            for (Map.Entry<Integer, EventNotificationI> entry : mEventNotificationListeners.entrySet())
            {
                entry.getValue().notifyEventCaptured(eventName, pArgs);
            }
        }
    }
}
