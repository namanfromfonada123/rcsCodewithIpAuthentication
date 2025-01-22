package com.messaging.rcs.rbm.service;

import com.messaging.rcs.domain.MessageTypeEntity;

import java.util.List;

/**
 * Created by sbsingh on Nov/21/2021.
 */
public interface RbmMessageService {

    List<MessageTypeEntity> getAllMessageTypes();

    void addMessageToCampaign(Long campaignId, Long messageId, Object message) throws Exception;
}
