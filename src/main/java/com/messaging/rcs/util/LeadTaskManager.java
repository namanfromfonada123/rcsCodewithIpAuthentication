package com.messaging.rcs.util;


import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.service.MessageProcessingEngine;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sbsingh on 6/7/19.
 */
public class LeadTaskManager implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(LeadTaskManager.class.getName());

    volatile int totalNumberOfPhoneNumbersThatCanBePublished;
    LeadInfoEntity leadInfoEntity;
    MessageProcessingEngine messageProcessingEngine;
    CopyOnWriteArrayList<Long> threadStartLeadList;

    public LeadTaskManager(int totalNumberOfPhoneNumbersThatCanBePublished, LeadInfoEntity leadInfoEntity, MessageProcessingEngine messageProcessingEngine, CopyOnWriteArrayList<Long> threadStartLeadList){
        this.totalNumberOfPhoneNumbersThatCanBePublished = totalNumberOfPhoneNumbersThatCanBePublished;
        this.leadInfoEntity = leadInfoEntity;
        this.messageProcessingEngine = messageProcessingEngine;
        this.threadStartLeadList = threadStartLeadList;
    }
    @Override
    public void run() {
        LOGGER.info("Thread Started for "+ leadInfoEntity.getLeadName()+" in Auto/W-Auto mode");
        List<LeadInfoEntity> tempLeadInfoEntityList = new ArrayList<>();
        tempLeadInfoEntityList.add(leadInfoEntity);
        if(messageProcessingEngine != null) {
            try {
                messageProcessingEngine.processLeads(tempLeadInfoEntityList, totalNumberOfPhoneNumbersThatCanBePublished , true);
            }catch (Exception e){
                LOGGER.info("Thread exiting for "+ leadInfoEntity.getLeadName()+" in Auto/W-Auto mode");
                threadStartLeadList.remove(leadInfoEntity.getLeadId());
                LOGGER.error("Error in processLeads:",e);
            }

            LOGGER.info("Thread exiting for "+ leadInfoEntity.getLeadName()+" in Auto/W-Auto mode");
            threadStartLeadList.remove(leadInfoEntity.getLeadId());
        }else{
            LOGGER.error("astrixDispatcherService is null in TaskManager");
        }
    }
}
