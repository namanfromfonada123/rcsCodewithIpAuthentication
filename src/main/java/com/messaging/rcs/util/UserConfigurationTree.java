package com.messaging.rcs.util;



import com.messaging.rcs.domain.UserEntity;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by sbsingh on 4/14/19.
 */
public class UserConfigurationTree {

    private UserEntity userEntity;
    private CopyOnWriteArrayList<CampaignConfigurationTree> userWiseCampaignList;
    private CopyOnWriteArrayList<CampaignConfigurationTree> userWiseAllCampaignList = new CopyOnWriteArrayList<>();
    private volatile boolean isThreadStarted;
    private volatile boolean isDynamicInStaticUserThreadStarted;
    private CopyOnWriteArraySet<Long> processedCampaignList = new CopyOnWriteArraySet<>();
    private CopyOnWriteArraySet<Long> activeCampaignList = new CopyOnWriteArraySet<>();

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }


    public CopyOnWriteArrayList<CampaignConfigurationTree> getUserWiseCampaignList() {
        return userWiseCampaignList;
    }

    public void setUserWiseCampaignList(CopyOnWriteArrayList<CampaignConfigurationTree> userWiseCampaignList) {
        this.userWiseCampaignList = userWiseCampaignList;
    }

    public CopyOnWriteArrayList<CampaignConfigurationTree> getUserWiseAllCampaignList() {
        return userWiseAllCampaignList;
    }

    public void setUserWiseAllCampaignList(CopyOnWriteArrayList<CampaignConfigurationTree> userWiseAllCampaignList) {
        this.userWiseAllCampaignList = userWiseAllCampaignList;
    }

    public boolean isThreadStarted() {
        return isThreadStarted;
    }

    public void setThreadStarted(boolean threadStarted) {
        isThreadStarted = threadStarted;
    }

    public boolean isDynamicInStaticUserThreadStarted() {
        return isDynamicInStaticUserThreadStarted;
    }

    public void setDynamicInStaticUserThreadStarted(boolean dynamicInStaticUserThreadStarted) {
        isDynamicInStaticUserThreadStarted = dynamicInStaticUserThreadStarted;
    }

    public CopyOnWriteArraySet<Long> getProcessedCampaignList() {
        return processedCampaignList;
    }

    public void setProcessedCampaignList(CopyOnWriteArraySet<Long> processedCampaignList) {
        this.processedCampaignList = processedCampaignList;
    }

    public CopyOnWriteArraySet<Long> getActiveCampaignList() {
        return activeCampaignList;
    }

    public void setActiveCampaignList(CopyOnWriteArraySet<Long> activeCampaignList) {
        this.activeCampaignList = activeCampaignList;
    }
}
