package com.messaging.rcs.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.MessageTypeEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.model.Campaign;
import com.messaging.rcs.repository.CampaignRepository;
import com.messaging.rcs.repository.LeadInfoRepository;
import com.messaging.rcs.repository.MessageTypeRepository;
import com.messaging.rcs.repository.TemplateRepository;
import com.messaging.rcs.repository.UserRepository;

/**
 * 
 * @author Rahul Kumar 18/05/2023
 *
 */
@Service
public class CampaignServiceImpl implements CampaignService {
	private static final Logger LOGGER = Logger.getLogger(CampaignServiceImpl.class.getName());
	private static final String CAMPAIGN_CREATED = "CAMPAIGN_CREATED";
	private static final String CAMPAIGN_UPDATED = "CAMPAIGN_UPDATED";
	private static final String CAMPAIGN_DELETED = "CAMPAIGN_DELETED";

	@Resource
	private CampaignRepository campaignRepository;
	@Autowired
	private TemplateRepository templateRepository;
	@Resource
	private UserRepository userRepository;
	// @Autowired
	// private UserService userService;
	@Autowired
	private MessageTypeRepository messageTypeRepository;

	private BeanUtilsBean beanUtils = new BeanUtilsBean();

	@Autowired
	private LeadService leadService;
	@Autowired
	private LeadInfoRepository leadInfoRepository;

	@Override
	@Transactional
	public Campaign createCampaign(Campaign campaign) throws Exception {
		LOGGER.info("createCampaign ->" + campaign + "->" + campaign.getCampaignName());
		UserEntity userEntity = userRepository.findById(campaign.getUserId()).orElse(null);
		if (userEntity == null) {
			LOGGER.error(
					"No User found with ID " + campaign.getUserId() + " For Campaign " + campaign.getCampaignName());
			return null;
		}
		CampaignEntity campaignEntity = new CampaignEntity();
		try {
			beanUtils.copyProperties(campaignEntity, campaign);
			campaignEntity.setCreatedDate(new Date());
			campaignEntity.setLastModifiedDate(new Date());
			campaignEntity.setCreatedBy(userEntity.getFirstName());// (userService.getLoggedInUserName());
			campaignEntity.setLastModifiedBy(userEntity.getFirstName());// (userService.getLoggedInUserName());
			campaignEntity.setIsSendToRmq(0);
			campaignEntity = campaignRepository.save(campaignEntity);
			campaign.setCampaignId(campaignEntity.getCampaignId());
			if (campaign.getRcsMsgTypeId() == 1) {
				campaign.setTemplateName(templateRepository.findById(campaign.getTemplateId()).get().getTemplateCode());
			} else {
				campaign.setTemplateName(
						templateRepository.findById(campaign.getSmsTemplateId()).get().getTemplateCode());

			}
			campaign.setCreatedDate(campaignEntity.getCreatedDate());
			campaign.setLastModifiedBy(userEntity.getFirstName());
			campaign.setCreatedBy(userEntity.getFirstName());// (userService.getLoggedInUserName());
			campaign.setLastModifiedDate(campaignEntity.getLastModifiedDate());// (userService.getLoggedInUserName());

		} catch (Exception e) {
			LOGGER.error("Got Exception ", e);
			throw e;
		}
		LOGGER.info(CAMPAIGN_CREATED);
		Notifications.getsInstance().notifyEventCaptured(CAMPAIGN_CREATED, campaignEntity);
		return campaign;
	}

	@Override
	public List<Campaign> getAllCampaigns() throws Exception {
		LOGGER.info("getAllCampaigns ->");
		List<CampaignEntity> campaignEntities = campaignRepository.findAll();
		List<Campaign> campaigns = new ArrayList<>();
		if (!CollectionUtils.isEmpty(campaignEntities)) {
			for (CampaignEntity campaignEntity : campaignEntities) {
				Campaign campaign = new Campaign();
				beanUtils.copyProperties(campaign, campaignEntity);
				campaigns.add(campaign);
			}
		}
		return campaigns;
	}

	@Override
	public void deleteCampaign(Long id, String status) throws Exception {
		Optional<CampaignEntity> entityOptional = campaignRepository.findById(id);
		CampaignEntity campaignEntity = entityOptional.get();
		if (campaignEntity != null) {
			campaignEntity.setIsDeleted(Integer.valueOf(status));
			campaignEntity.setCampaignStatus(Integer.valueOf(status) == 0 ? "Active" : "InActive");
			campaignRepository.save(campaignEntity);
			LOGGER.info("Campaign deleted successfully : " + id);
		} else {
			LOGGER.info("Campaign not found !! : " + id);
		}

		LOGGER.info(CAMPAIGN_DELETED);
	}

	@Override
	public Campaign getCampaignById(Long id) throws InvocationTargetException, IllegalAccessException {
		Optional<CampaignEntity> entityOptional = campaignRepository.findById(id);
		if (entityOptional.isPresent()) {
			Campaign campaign = new Campaign();
			beanUtils.copyProperties(campaign, entityOptional.get());
			return campaign;
		}
		return null;
	}

	@Override
	public List<MessageTypeEntity> getAllMessageTypes() {
		return messageTypeRepository.findAll();
	}

	@Override
	public Campaign getCampaignByCampaignNameAndUserId(String campaignName, long userId)
			throws InvocationTargetException, IllegalAccessException {
		CampaignEntity existCam = null;

		existCam = campaignRepository.getCampaignByNameAndUserId(campaignName, userId);
		if (Objects.nonNull(existCam)) {
			Campaign campaign = new Campaign();
			beanUtils.copyProperties(campaign, existCam);
			return campaign;
		}
		return null;

	}

	@Override
	public List<Campaign> getAllByCampaignByUserId(String from, String to, long UserId, Integer start, Integer limit,
			String templateId, String campaignId,String botMappingId) throws InvocationTargetException, IllegalAccessException {
		List<Campaign> campaignList = new ArrayList<>();

		// String userName=userRepository.findByUserId(UserId).getUserName();
		String startDate = from + " 00:00:00";
		String endDate = to + " 23:59:59";
		List<CampaignEntity> campaingList = null;
		List<MessageTypeEntity> msgList = messageTypeRepository.findAll();
		if (limit == 0) {
			campaingList = campaignRepository.getAllCampaignByStartAndDate(startDate, endDate, UserId);

		} else {
			start = start - 1;
			int pageSize = limit;
			int pageNum = start != 0 ? start / pageSize : 0;
			Pageable pageable = PageRequest.of(pageNum, pageSize);

			if (Objects.nonNull(templateId) && Objects.nonNull(campaignId) && Objects.nonNull(botMappingId)) {
				List<Long> tempalteListId = templateRepository.findAllByTemplateId(botMappingId);

				campaingList = campaignRepository.getAllByCampaignIdAndTemplateIdAndBotMappingId(startDate, endDate,
						UserId, campaignId, tempalteListId, pageable);
			} else if (Objects.nonNull(templateId) && Objects.isNull(campaignId) && Objects.isNull(botMappingId)) {
				campaingList = campaignRepository.getAllByTemplateIdAndBetweenStartDate(startDate, endDate, UserId,
						templateId, pageable);
			} else if (Objects.isNull(templateId) && Objects.nonNull(campaignId) && Objects.isNull(botMappingId)) {
				campaingList = campaignRepository.getAllByCampaignIdAndBetweenStartDate(startDate, endDate, UserId,
						campaignId, pageable);
			} else if (Objects.isNull(templateId) && Objects.isNull(campaignId) && Objects.nonNull(botMappingId)) {
				List<Long> tempalteListId = templateRepository.findAllByTemplateId(botMappingId);

				campaingList = campaignRepository.getAllByCampaignIdByBotMappingId(startDate, endDate, UserId,
						tempalteListId, pageable);
			} else {
				campaingList = campaignRepository.getAllByUserIdAndStartDate(startDate, endDate, UserId, pageable);
			}
		}
		if (campaingList.size() > 0) {
			for (CampaignEntity cmpg : campaingList) {

				Campaign campaignDto = new Campaign();
				List<MessageTypeEntity> mSG = msgList.stream().filter(msg -> msg.getMessageId() == cmpg.getMessageId())
						.collect(Collectors.toList());
				beanUtils.copyProperties(campaignDto, cmpg);
				// campaignDto.setUsageType(mSG.get(0).getMessageType());
				if (cmpg.getRcsMsgTypeId() == 1) {
					campaignDto
							.setTemplateName(templateRepository.findById(cmpg.getTemplateId()).get().getTemplateCode());
				} else {
					campaignDto.setTemplateName(
							templateRepository.findById(cmpg.getSmsTemplateId()).get().getTemplateCode());

				}
				campaignDto.setLeadCount(leadInfoRepository.getLeadCountByCampaignId(cmpg.getCampaignId()));
				campaignList.add(campaignDto);
			}
		}
		return campaignList;
	}

	@Override
	public List<Campaign> getAllByCampaignByUserId(long UserId)
			throws InvocationTargetException, IllegalAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Campaign updateCampaign(Campaign campaign) throws Exception {
		LOGGER.info("***** Campaign udupdateCampaign *****");

		CampaignEntity existCampaign = null;
		Campaign cam = new Campaign();
		CampaignEntity updateCampaign = null;
		existCampaign = campaignRepository.getByCampaignId(campaign.getCampaignId());
		try {

			if (existCampaign == null) {
				throw new Exception("Campaign Doesn't Exist.");
			}
			if (!campaign.getTemplateId().equals(existCampaign.getTemplateId())) {
				existCampaign.setTemplateId(templateRepository.findById(campaign.getTemplateId()).get().getId());
			}
			if (Objects.nonNull(campaign.getCampaignStartTime())) {
				existCampaign.setCampaignStartTime(campaign.getCampaignStartTime());
			}
			if (Objects.nonNull(campaign.getCampaignEndTime())) {
				existCampaign.setCampaignEndTime(campaign.getCampaignEndTime());
			}
			if (Objects.nonNull(campaign.getDescription())) {
				existCampaign.setDescription(campaign.getDescription());

			}

			if (Objects.nonNull(campaign.getMessageId())) {
				existCampaign.setMessageId(campaign.getMessageId());
			}

			existCampaign.setLastModifiedDate(new Date());

			updateCampaign = campaignRepository.save(existCampaign);

			beanUtils.copyProperties(cam, updateCampaign);
		} catch (Exception e) {
			LOGGER.info("***** Campaign udupdateCampaign Error *****" + e.getMessage());

			e.printStackTrace();
		}
		return cam;
	}

}
