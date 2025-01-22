package com.messaging.rcs.service;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.logging.Logger;


/**
 * Created by sbsingh on 4/13/19.
 */
public abstract class NotificationBase {
    private static final Logger log = Logger.getLogger(NotificationBase.class.getName());

    private Comparator<Integer> mListenerComparator = new Comparator<Integer>()
    {
        public int compare(Integer i1, Integer i2)
        {
            return i2 - i1; // reverse comparator, so higher priorities sort first
        }
    };

    protected Comparator<Integer> getListenerComparator()
    {
        return mListenerComparator;
    }

    protected <L> void registerListener(L pListener, int pPriority, final SortedMap<Integer, L> pMap, String pListenerType)
    {
        assert pListener != null;
        synchronized (pMap)
        {
            if (pMap.values().contains(pListener))
            {
                pMap.values().remove(pListener);
            }

            if (pMap.containsKey(pPriority))
            {
                Object currentListener = pMap.get(pPriority);
                if (currentListener.getClass().equals(pListener.getClass()))
                {
                    log.info("Replacing listener " + pListener.getClass().getSimpleName() + " of priority " + pPriority);
                    pMap.remove(pPriority);
                }
            }

            assert !pMap.containsKey(pPriority) :
                    "Error registering listener " + pListener.getClass().getSimpleName() +
                            ". Already registered listener with priority " + pPriority + ": " +
                            pMap.get(pPriority).getClass().getSimpleName();

            pMap.put(pPriority, pListener);
            log.info("Registered " + pListenerType + " listener " + pListener.getClass().getSimpleName() + " with priority " + pPriority);
        }
    }

    protected <L> void removeListener(L pListener, final SortedMap<Integer, L> pMap)
    {
        synchronized (pMap)
        {
            pMap.values().remove(pListener);
        }
    }
}
