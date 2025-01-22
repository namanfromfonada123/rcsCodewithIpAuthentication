package com.messaging.rcs.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.MessageTypeEntity;
import com.messaging.rcs.email.model.CampaignListing;
import com.messaging.rcs.model.Campaign;
import com.messaging.rcs.model.Template;
import com.messaging.rcs.repository.CampaignRepository;
import com.messaging.rcs.repository.RcsMsgTypeRepository;
import com.messaging.rcs.repository.TemplateRepository;
import com.messaging.rcs.repository.UserBotMappingRepository;
import com.messaging.rcs.service.CampaignService;

@RestController
@CrossOrigin({ "*" })
@RequestMapping(value = { "/api/v1/rcsmessaging/campaign" }, produces = { "application/json" })
public class CampaignController {
	private static final Logger LOGGER = Logger
			.getLogger(com.messaging.rcs.controller.CampaignController.class.getName());

	private static final String STATUS = "status";

	private static final String CAMPAIGN = "Campaign";

	private static final String MESSAGE = "message";

	@Autowired
	CampaignService campaignService;

	@Autowired
	CampaignRepository campaignRepository;

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private RcsMsgTypeRepository rcsMsgTypeRepository;
	@Autowired
	private UserBotMappingRepository botMappingRepository;

	@PostMapping({ "/createCampaign" })
	ResponseEntity createCampaign(@RequestBody Campaign campaign) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		LOGGER.info("CONTROLLER Create Campaign:" + campaign);
		CampaignEntity existCampaigns = null;
		Campaign returnedCampaign = null;
		try {
			existCampaigns = this.campaignRepository.getCampaignByNameAndUserId(campaign.getCampaignName(),
					campaign.getUserId());
			if (Objects.nonNull(existCampaigns)) {
				LOGGER.error("Campaign Already Exist Against UserId and Campaign.");
				result.put("status", Integer.valueOf(201));
				result.put("message", "Campaign Already Exist Against UserId and Campaign.");
				result.put("Campaign", existCampaigns);
				return new ResponseEntity(result, HttpStatus.CREATED);
			}
			if (campaign.getCampaignName().isEmpty()) {
				LOGGER.error("Campaign Name Required.");
				result.put("status", Integer.valueOf(404));
				result.put("message", "Campaign Name Required.");
				result.put("Campaign", existCampaigns);
				return new ResponseEntity(result, HttpStatus.CREATED);
			}
			returnedCampaign = this.campaignService.createCampaign(campaign);
		} catch (Exception e) {
			LOGGER.error("Got Exception while creating/updating campaign:", e);
			result.put("status", Integer.valueOf(500));
			result.put("message", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("Campaign", e);
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (returnedCampaign == null) {
			result.put("status",
					"User Id " + campaign.getUserId() + " not found for Campaign " + campaign.getCampaignName());
			result.put("message",
					"User Id " + campaign.getUserId() + " not found for Campaign " + campaign.getCampaignName());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		result.put("status", HttpStatus.OK);
		result.put("Campaign", returnedCampaign);
		return new ResponseEntity(result, HttpStatus.CREATED);
	}

	@RequestMapping(value = { "/findCampaignById" }, produces = { "application/json" })
	public ResponseEntity findCampaignById(@RequestParam("Id") Long Id) throws Exception {
		LOGGER.info("getCampaigns->" + Id);
		Campaign campaign = this.campaignService.getCampaignById(Id);
		return new ResponseEntity(campaign, HttpStatus.OK);
	}

	@RequestMapping(value = { "/deleteCampaignById" }, produces = { "application/json" })
	public ResponseEntity deleteCampaignById(@RequestParam("Id") Long Id, @RequestParam("status") String status)
			throws Exception {
		HashMap<String, String> result = new HashMap<>();
		try {
			this.campaignService.deleteCampaign(Id, status);
			result.put("status", "deleted campaign " + Id);
			result.put("Campaign", "Campign Id " + Id);
			return new ResponseEntity(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put("status", "Error in deletion of campaign. Please contact administrator or try again. ");
			result.put("message", "Error in deletion of campaign. Please contact administrator or try again. ");
			return new ResponseEntity(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@RequestMapping(value = { "/findCampaignByCampaignNameAndUserId" }, produces = { "application/json" })
	public ResponseEntity findCampaignByCampaignNameAndUserId(@RequestParam("campaignName") String campaignName,
			@RequestParam("userId") Long userId) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		Campaign existCampaigns = null;
		try {
			existCampaigns = this.campaignService.getCampaignByCampaignNameAndUserId(campaignName, userId.longValue());
			if (Objects.nonNull(existCampaigns)) {
				LOGGER.error("Campaign Founded  Against Campaign and UserId.  " + campaignName + " AND " + userId);
				result.put("status", HttpStatus.OK.toString());
				result.put("message", "Record Already Exist.");
				result.put("Campaign", existCampaigns);
				return new ResponseEntity(result, HttpStatus.OK);
			}
			LOGGER.error("Campaign Not Found  Against UserId and Campaign. " + campaignName + " AND " + userId);
			result.put("message", "Campaign Doesn't Exist.");
			result.put("status", Integer.valueOf(404));
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			result.put("status", Integer.valueOf(404));
			result.put("message",
					"Error for Campaign Finding  Against Campaign and UserId.  \" + campaignName + \" AND \" + userId");
			return new ResponseEntity(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@GetMapping(value = { "/messageTypes" }, produces = { "application/json" })
	public ResponseEntity<?> getAllMessageTypes() {
		HashMap<String, Object> result = new HashMap<>();
		List<MessageTypeEntity> messageTypes = this.campaignService.getAllMessageTypes();
		if (!messageTypes.isEmpty()) {
			result.put("status", "Success");
			result.put("message", messageTypes);
			return new ResponseEntity(result, HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = { "/findAllCapmaingList" }, method = { RequestMethod.GET })
	public ResponseEntity findAllCapmaingList(@RequestParam("from") String from, @RequestParam("to") String to,
			@RequestParam("userId") Long userId, @RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "templateId", required = false) String templateId,
			@RequestParam(value = "campaignId", required = false) String campaignId,@RequestParam(value = "botMappingId", required = false)String botMappingId) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		List<Campaign> existCampaigns = null;
		try {
			String startDate = from + " 00:00:00";
			String endDate = to + " 23:59:59";
			long totalCampaign = 0L;
			if (Objects.nonNull(templateId) && Objects.nonNull(campaignId) && Objects.nonNull(botMappingId)) {
				
				List<Long> tempalteListId=	this.templateRepository.findAllByTemplateId(botMappingId);
				totalCampaign = this.campaignRepository
						.countByTemplateIdAndCampaingIdAndBotId(startDate, endDate, userId, campaignId, tempalteListId).longValue();
			} else if (Objects.nonNull(templateId) && Objects.isNull(campaignId) && Objects.isNull(botMappingId)) {
				totalCampaign = this.campaignRepository.countByTemplateId(startDate, endDate, userId, templateId)
						.longValue();
			} else if (Objects.isNull(templateId) && Objects.nonNull(campaignId) && Objects.isNull(botMappingId)) {
				totalCampaign = this.campaignRepository.countByCampaignId(startDate, endDate, userId, campaignId)
						.longValue();
			}else if (Objects.isNull(templateId) && Objects.isNull(campaignId) && Objects.nonNull(botMappingId)) {
				
				List<Long> tempalteListId=	templateRepository.findAllByTemplateId(botMappingId);
				
				totalCampaign = this.campaignRepository.countByBotMappingId(startDate, endDate, userId, tempalteListId)
						.longValue();
			} else {
				totalCampaign = this.campaignRepository.countByUserId(startDate, endDate, userId).longValue();
			}
			LOGGER.info("Total Campaing Count::=>" + totalCampaign);
			existCampaigns = this.campaignService.getAllByCampaignByUserId(from, to, userId.longValue(), start, limit,
					templateId, campaignId,botMappingId);
			if (existCampaigns.size() > 0) {
				for (Campaign camType : existCampaigns) {
					camType.setMsgCampaignType(
							rcsMsgTypeRepository.findById(camType.getRcsMsgTypeId()).get().getMessageType());
				}
				LOGGER.info("Campaign Founded  Against Campaign and UserId :: " + userId);
				result.put("Campaign", existCampaigns);
				result.put("totalCount", Long.valueOf(totalCampaign));
				return new ResponseEntity(result, HttpStatus.OK);
			}
			LOGGER.info("Campaign Not Found  Against UserId :: " + userId);
			result.put("message", "Record Not Found.");
			result.put("status", Integer.valueOf(404));
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", Integer.valueOf(404));
			result.put("message", "Error for Campaign Finding  Against UserId. " + userId);
			return new ResponseEntity(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@GetMapping(value = { "/campaignListing" }, produces = { "application/json" })
	public ResponseEntity getCampaignListing(@RequestParam(value = "userId", required = true) Long userId)
			throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		List<CampaignListing> campaignListings = new ArrayList<>();
		List<CampaignEntity> campaignList = this.campaignRepository.getCampaignByUserId(userId);
		if (campaignList.size() > 0) {
			for (CampaignEntity camEntity : campaignList) {
				CampaignListing nameList = new CampaignListing();
				nameList.setCampaignId(camEntity.getCampaignId().longValue());
				nameList.setCampaignName(camEntity.getCampaignName());
				nameList.setRcsMsgTypeId(camEntity.getRcsMsgTypeId());
				campaignListings.add(nameList);
			}
			return new ResponseEntity(campaignListings, HttpStatus.OK);
		}
		result.put("message", "No Content");
		return new ResponseEntity(result, HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = { "/updateCampaign" }, produces = { "application/json", "application/xml" }, consumes = {
			"application/json", "application/xml" }, method = { RequestMethod.POST })
	public ResponseEntity updateCampaign(@RequestBody Campaign campaign) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		LOGGER.info("CONTROLLER Update Campaign:" + campaign);
		Campaign updateCampaign = null;
		try {
			updateCampaign = this.campaignService.updateCampaign(campaign);
		} catch (RuntimeException e) {
			result.put("status", HttpStatus.BAD_REQUEST);
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error("Got Exception while Updating campaign:", e);
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("message", e);
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (updateCampaign == null) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			result.put("message", "Internal Server Error");
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		result.put("status", HttpStatus.OK);
		result.put("message", updateCampaign);
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping(value = { "/campaignNameAndIdListByDateRange" }, produces = { "application/json" })
	public ResponseEntity campaignNameAndIdListByDateRange(@RequestParam("from") String from,
			@RequestParam("to") String to, @RequestParam(value = "userId", required = true) Long userId)
			throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		List<CampaignListing> campaignListings = new ArrayList<>();
		List<CampaignEntity> campaignList = this.campaignRepository.getAllCampaignByStartAndDate(from + " 00:00:00",
				to + " 23:59:59", userId);
		if (campaignList.size() > 0) {
			for (CampaignEntity camEntity : campaignList) {
				CampaignListing nameList = new CampaignListing();
				nameList.setCampaignId(camEntity.getCampaignId().longValue());
				nameList.setCampaignName(camEntity.getCampaignName());
				campaignListings.add(nameList);
			}
			return new ResponseEntity(campaignListings, HttpStatus.OK);
		}
		result.put("message", "No Content");
		return new ResponseEntity(result, HttpStatus.NO_CONTENT);
	}

	@GetMapping(value = { "/campaignListingByTemplateId" }, produces = { "application/json" })
	public ResponseEntity campaignListingByTemplateId(@RequestParam(value = "userId", required = true) Long userId,
			@RequestParam(value = "templateId", required = true) Long templateId) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		List<CampaignListing> campaignListings = new ArrayList<>();
		List<CampaignEntity> campaignList = this.campaignRepository.getCampaignByUserIdAndTemplateId(userId,
				templateId);
		if (campaignList.size() > 0) {
			for (CampaignEntity camEntity : campaignList) {
				CampaignListing nameList = new CampaignListing();
				nameList.setCampaignId(camEntity.getCampaignId().longValue());
				nameList.setCampaignName(camEntity.getCampaignName());
				campaignListings.add(nameList);
			}
			return new ResponseEntity(campaignListings, HttpStatus.OK);
		}
		result.put("message", "No Content");
		return new ResponseEntity(result, HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("unchecked")
	@GetMapping(value = { "/templateByCamapignId" }, produces = { "application/json" })
	public ResponseEntity templateObjectByCamapignId(
			@RequestParam(value = "campaignId", required = true) Long campaignId) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		List<Template> listTemplate = new ArrayList<>();
		CampaignEntity campaignEntity = null;
		campaignEntity = campaignRepository.findById(campaignId).get();
		Optional<Template> rcsTempalte = null;
		if (Objects.nonNull(campaignEntity.getTemplateId())) {
			rcsTempalte = this.templateRepository.findById(campaignEntity.getTemplateId());
			listTemplate.add(rcsTempalte.get());
		}
		if (Objects.nonNull(campaignEntity.getSmsTemplateId())) {
			Optional<Template> smsTempalte = null;
			smsTempalte = this.templateRepository.findById(campaignEntity.getSmsTemplateId());
			listTemplate.add(smsTempalte.get());
		}
		if (listTemplate.size() > 0) {
			return new ResponseEntity(listTemplate, HttpStatus.OK);
		} else {
			result.put("message", "No Content");

			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		}
	}
}
