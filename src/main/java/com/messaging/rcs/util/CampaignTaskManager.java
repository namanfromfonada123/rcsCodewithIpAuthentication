package com.messaging.rcs.util;


import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.service.MessageProcessingEngine;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sbsingh on 6/7/19.
 */
public class CampaignTaskManager implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(CampaignTaskManager.class.getName());
    int totalNumberOfPhoneNumbersThatCanBePublished;
    CampaignEntity campaignEntity;
    MessageProcessingEngine messageProcessingEngine;
    HashMap<Long,List<LeadInfoEntity>> leadInfoEntityListByCampaignId;
    CampaignConfigurationTree campaignConfigurationTree;

    public CampaignTaskManager(int totalNumberOfPhoneNumbersThatCanBePublished, CampaignEntity campaignEntity, MessageProcessingEngine messageProcessingEngine, HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId, CampaignConfigurationTree campaignConfigurationTree) {
        this.totalNumberOfPhoneNumbersThatCanBePublished = totalNumberOfPhoneNumbersThatCanBePublished;
        this.campaignEntity = campaignEntity;
        this.messageProcessingEngine = messageProcessingEngine;
        this.leadInfoEntityListByCampaignId = leadInfoEntityListByCampaignId;
        this.campaignConfigurationTree = campaignConfigurationTree;
    }

    @Override
    public void run() {
        LOGGER.info("Thread Started for "+ campaignEntity.getCampaignName()+" in Auto/W-Auto mode");
        List<CampaignEntity> tempCampaignList = new ArrayList<>();
        tempCampaignList.add(campaignEntity);
        if(messageProcessingEngine != null){
            try {
                messageProcessingEngine.processCampaignsForDynamicScheme(tempCampaignList, leadInfoEntityListByCampaignId, totalNumberOfPhoneNumbersThatCanBePublished);
            }catch (Exception e){
                campaignConfigurationTree.setThreadStarted(false);
                LOGGER.error("Error in processCampaignsForDynamicScheme ",e);
            }
        }else{
            LOGGER.error("astrixDispatcherService is null");
        }
        campaignConfigurationTree.setThreadStarted(false);
    }
}
