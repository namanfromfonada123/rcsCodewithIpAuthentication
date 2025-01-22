package com.messaging.rcs.service;

import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.MessageTypeEntity;
import com.messaging.rcs.model.Campaign;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;

/**
 * 
 * @author Rahul Kumar 26/05/2023
 *
 */
public interface CampaignService {

	Campaign createCampaign(Campaign campaign) throws Exception;

	List<Campaign> getAllCampaigns() throws Exception;

	void deleteCampaign(Long id, String status) throws Exception;

	Campaign updateCampaign(Campaign campaign) throws Exception;

	Campaign getCampaignById(Long id) throws InvocationTargetException, IllegalAccessException;

	Campaign getCampaignByCampaignNameAndUserId(String campaignName, long userId)
			throws InvocationTargetException, IllegalAccessException;

	List<MessageTypeEntity> getAllMessageTypes();

	List<Campaign> getAllByCampaignByUserId(long UserId) throws InvocationTargetException, IllegalAccessException;

	List<Campaign> getAllByCampaignByUserId(String from, String to, long UserId, Integer start, Integer limit,
			String templateId, String campaignId,String botId) throws InvocationTargetException, IllegalAccessException;

}
