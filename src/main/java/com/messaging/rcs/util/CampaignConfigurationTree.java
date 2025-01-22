package com.messaging.rcs.util;



import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.LeadInfoEntity;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sbsingh on 4/14/19.
 */
public class CampaignConfigurationTree {

    private CampaignEntity campaignEntity;
    private CopyOnWriteArrayList<LeadInfoEntity> campaignWiseLeadList = new CopyOnWriteArrayList<>();
    private volatile boolean isThreadStarted;
    private CopyOnWriteArrayList<Long> processedLeadList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Long> threadStartLeadList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Long> fullyProcessedLeads = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<String> ivrInfo = new CopyOnWriteArrayList<>();
    private String campaignStatus;

    public CampaignEntity getCampaignEntity() {
        return campaignEntity;
    }

    public void setCampaignEntity(CampaignEntity campaignEntity) {
        this.campaignEntity = campaignEntity;
    }

    public CopyOnWriteArrayList<LeadInfoEntity> getCampaignWiseLeadList() {
        return campaignWiseLeadList;
    }

    public void setCampaignWiseLeadList(CopyOnWriteArrayList<LeadInfoEntity> campaignWiseLeadList) {
        this.campaignWiseLeadList = campaignWiseLeadList;
    }

    public boolean isThreadStarted() {
        return isThreadStarted;
    }

    public void setThreadStarted(boolean threadStarted) {
        isThreadStarted = threadStarted;
    }

    public CopyOnWriteArrayList<Long> getProcessedLeadList() {
        return processedLeadList;
    }

    public void setProcessedLeadList(CopyOnWriteArrayList<Long> processedLeadList) {
        this.processedLeadList = processedLeadList;
    }


    public CopyOnWriteArrayList<Long> getThreadStartLeadList() {
        return threadStartLeadList;
    }

    public void setThreadStartLeadList(CopyOnWriteArrayList<Long> threadStartLeadList) {
        this.threadStartLeadList = threadStartLeadList;
    }

    public CopyOnWriteArrayList<Long> getFullyProcessedLeads() {
        return fullyProcessedLeads;
    }

    public void setFullyProcessedLeads(CopyOnWriteArrayList<Long> fullyProcessedLeads) {
        this.fullyProcessedLeads = fullyProcessedLeads;
    }

    public CopyOnWriteArrayList<String> getIvrInfo() {
        return ivrInfo;
    }

    public void setIvrInfo(CopyOnWriteArrayList<String> ivrInfo) {
        this.ivrInfo = ivrInfo;
    }

    public String getCampaignStatus() {
        return campaignStatus;
    }

    public void setCampaignStatus(String campaignStatus) {
        this.campaignStatus = campaignStatus;
    }

}
