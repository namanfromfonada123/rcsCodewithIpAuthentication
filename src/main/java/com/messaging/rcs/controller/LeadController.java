package com.messaging.rcs.controller;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.messaging.rcs.configuration.HashMaps;
import com.messaging.rcs.consumer.RabbitMQSenderTelco;
import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.domain.ScheduleEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.model.WebEngageLeadInfoDetail;
import com.messaging.rcs.email.model.WebEngageSchedule;
import com.messaging.rcs.model.ApiLeadCreatedPojo;
import com.messaging.rcs.model.ApiResponse;
import com.messaging.rcs.model.ClientLeadCreatedPojo;
import com.messaging.rcs.model.Credentials;
import com.messaging.rcs.model.DemoRcsModel;
import com.messaging.rcs.model.InvalidIPException;
import com.messaging.rcs.model.LeadAndCampaingListing;
import com.messaging.rcs.model.LeadInfo;
import com.messaging.rcs.model.LeadInfoDetail;
import com.messaging.rcs.model.LeadListing;
import com.messaging.rcs.model.LeadUploadResponse;
import com.messaging.rcs.model.Schedule;
import com.messaging.rcs.model.SmsApiWebRequest;
import com.messaging.rcs.repository.CampaignRepository;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.repository.LeadInfoRepository;
import com.messaging.rcs.repository.ScheduleRepository;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.service.LeadService;
import com.messaging.rcs.util.UserNotFoundException;

@RestController
@CrossOrigin
@RequestMapping(value = { "/api/v1/rcsmessaging/lead" }, produces = { "application/json" })
public class LeadController {
	private static final Logger LOGGER = Logger.getLogger(com.messaging.rcs.controller.LeadController.class.getName());

	private static final String STATUS = "Status";

	private static final String LEAD = "Lead";

	private static final String MESSAGE = "message";

	private static final List<String> VALID_ACTION = Arrays.asList(new String[] { "Start", "Stop", "Pause", "Cancel" });

	@Value("${api.key}")
	public String uKey;

	private BeanUtilsBean beanUtils = new BeanUtilsBean();

	@Autowired
	private LeadInfoRepository leadInfoRepository;

	@Autowired
	private CampaignRepository campaignRepository;
	@Autowired
	private RabbitMQSenderTelco rabbitMQSender;

	@Autowired
	private LeadInfoDetailRepository leadInfoDetailRepository;

	@Autowired
	private LeadService leadService;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	RedisTemplate<String, CampaignEntity> redisCampaignEntity;

	@SuppressWarnings("unchecked")
	@PostMapping({ "/generateLead" })
	public ResponseEntity<?> generateLead(@RequestBody ApiLeadCreatedPojo apiRequest,
			@RequestParam(value = "isDND", required = false, defaultValue = "true") boolean isDND) throws Exception {

		ClientLeadCreatedPojo webEnagage = new ClientLeadCreatedPojo();
		LeadInfoEntity returnedLead = null;
		List<WebEngageLeadInfoDetail> leadInfoDetails = new ArrayList<WebEngageLeadInfoDetail>();
		LOGGER.info("Request From ApiLeadCreatedPojo /generateLead JSON Format::=>" + apiRequest.toString());

		HashMap<String, Object> result = new HashMap<>();
		try {
			webEnagage.setDnd(isDND);
			webEnagage.setCampaignId(apiRequest.getCampaignId());
			webEnagage.setLeadName(apiRequest.getLeadName());
			webEnagage.setCheckRcs(apiRequest.getCheckRcs());
			webEnagage.setuKey(apiRequest.getuKey());

			if (Objects.nonNull(apiRequest.getPhoneNumber())) {
				List<String> noList = Arrays.asList(apiRequest.getPhoneNumber().split(","));
				for (String no : noList) {
					WebEngageLeadInfoDetail webEngageDetail = new WebEngageLeadInfoDetail();
					webEngageDetail.setPhoneNumber(no);
					webEngageDetail.setAdditonalDataInfoText(apiRequest.getTemplateKey());
					webEngageDetail.setAdditonalDataInfoText2(apiRequest.getTemplateValue());
					leadInfoDetails.add(webEngageDetail);
				}
				webEnagage.setLeadInfoDetails(leadInfoDetails);
			}
			/*
			 * if (Objects.nonNull(apiRequest.getPhoneNumber())) { WebEngageLeadInfoDetail
			 * webEngageDetail = new WebEngageLeadInfoDetail();
			 * webEngageDetail.setPhoneNumber(apiRequest.getPhoneNumber());
			 * webEngageDetail.setAdditonalDataInfoText(apiRequest.getTemplateKey());
			 * webEngageDetail.setAdditonalDataInfoText2(apiRequest.getTemplateValue());
			 * leadInfoDetails.add(webEngageDetail);
			 * webEnagage.setLeadInfoDetails(leadInfoDetails); }
			 */
			
			
			UserEntity hashMapKey = null;
			LOGGER.info("Request From ClientLeadCreatedPojo /generateLead API::=>" + webEnagage.toString());
			if (Objects.nonNull(webEnagage.getuKey())) {
				hashMapKey = HashMaps.userEntityHashmap.get(webEnagage.getuKey());
				/*
				 * userRepository
				 * .findById(campaignRepository.findById(webEnagage.getCampaignId()).get().
				 * getUserId()).get() .getApiKey();
				 */

				if (Objects.isNull(hashMapKey) || !webEnagage.getuKey().equalsIgnoreCase(webEnagage.getuKey())) {
					LOGGER.info(
							"API Key From DB::  " + hashMapKey + " And From Client Request ::" + webEnagage.getuKey());

					result.put("Status", HttpStatus.NOT_FOUND.toString());
					result.put("message", "API Key Not Valid.");
					LOGGER.info("Response From /generateLead API::=>" + result.toString());
					return new ResponseEntity(result, HttpStatus.NOT_FOUND);
				}
			}
			if (Objects.isNull(webEnagage.getCampaignId())) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "Campaign Id Required.");
				LOGGER.info("Response From /generateLead API::=>" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			if (Objects.isNull(webEnagage.getLeadName())) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "Lead Name Required.");
				LOGGER.info("Response From /generateLead API::=>" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			if (webEnagage.getLeadInfoDetails().size() == 0) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "Phone Number Required.");
				LOGGER.info("Response From /generateLead API::=>" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}

			if (Objects.nonNull(webEnagage.getCampaignId())) {
				CampaignEntity campaignEntity = null;

				campaignEntity = redisCampaignEntity.opsForValue()
						.get(String.valueOf(webEnagage.getCampaignId()) + "_" + hashMapKey.getUserId());
				if (Objects.isNull(campaignEntity)) {
					campaignEntity = campaignRepository
							.getActiveCampaignsByUserIdAndCampaignId(webEnagage.getCampaignId(), hashMapKey.getUserId())
							.orElse(null);
					if (Objects.nonNull(campaignEntity)) {
						LOGGER.info("SET Campaign From DB In Redis :: " + String.valueOf(webEnagage.getCampaignId())
								+ "_" + hashMapKey.getUserId() + " And Object ::/n" + campaignEntity.toString());

						redisCampaignEntity.opsForValue().set(
								String.valueOf(webEnagage.getCampaignId()) + "_" + hashMapKey.getUserId(),
								campaignEntity, 1380L, TimeUnit.MINUTES);

					} else {
						result.put("message", "No Campaign found with ID:: " + webEnagage.getCampaignId()
								+ " for This User :: " + hashMapKey.getUserName());
						result.put("Status", HttpStatus.NOT_FOUND.toString());

						LOGGER.info("Response From /generateLead API::=>" + result.toString());
						return new ResponseEntity(result, HttpStatus.NOT_FOUND);

					}

				}

			}
			String uuid = UUID.randomUUID().toString();
			// leadInfo.setMsgId(uuid);
			if (webEnagage.getLeadInfoDetails().size() > 0)
				for (WebEngageLeadInfoDetail phone : webEnagage.getLeadInfoDetails()) {
					if (phone.getPhoneNumber().startsWith("91")) {
						String prefix = phone.getPhoneNumber().substring(phone.getPhoneNumber().length() - 10);
						phone.setPhoneNumber(prefix);
						System.out.println(prefix);
					}
				}
			if (webEnagage.getCheckRcs().equalsIgnoreCase("N")) {
				if (webEnagage.getLeadSchedule() != null) {
					WebEngageSchedule schedule = new WebEngageSchedule();
					schedule.setScheduleStartDtm(webEnagage.getLeadSchedule().getScheduleStartDtm());
					schedule.setScheduleEndDtm(webEnagage.getLeadSchedule().getScheduleEndDtm());
					schedule.setScheduleDay(webEnagage.getLeadSchedule().getScheduleDay());
					webEnagage.setLeadSchedule(schedule);
				} else {
					WebEngageSchedule schedule = new WebEngageSchedule();
					schedule.setScheduleStartDtm(new Date());
					schedule.setScheduleEndDtm(new Date());
					schedule.setScheduleDay("1");
					webEnagage.setLeadSchedule(schedule);

				}
				this.rabbitMQSender.sendToWebEngageQueue(webEnagage);
			} else {
				LeadInfo leadInfo = new LeadInfo();
				if (Objects.nonNull(webEnagage.getLeadName()) || !webEnagage.getLeadName().isEmpty()) {
					LeadInfoEntity exitsleadId = null;
					exitsleadId = leadInfoRepository.findFirstByLeadName(webEnagage.getLeadName());

					if (Objects.nonNull(exitsleadId)) {
						LOGGER.info("Working On Existing LeadId ::=>" + exitsleadId.getLeadId());
						leadInfo.setLeadId(exitsleadId.getLeadId());
					}
				}

				leadInfo.setRcsMsgTypeId(1L);
				leadInfo.setCampaignId(webEnagage.getCampaignId());
				leadInfo.setLeadName(webEnagage.getLeadName());
				// leadInfo.setUserId(clientRequest.getUserId());

				if (webEnagage.getLeadSchedule() != null) {
					Schedule schedule = new Schedule();
					schedule.setScheduleStartDtm(webEnagage.getLeadSchedule().getScheduleStartDtm());
					schedule.setScheduleEndDtm(webEnagage.getLeadSchedule().getScheduleEndDtm());
					schedule.setScheduleDay(webEnagage.getLeadSchedule().getScheduleDay());
					leadInfo.setLeadSchedule(schedule);
				} else {
					Schedule schedule = new Schedule();
					schedule.setScheduleStartDtm(new Date());
					schedule.setScheduleEndDtm(new Date());
					schedule.setScheduleDay("1");
					leadInfo.setLeadSchedule(schedule);

				}
				/**
				 * Set leadInfoDetails
				 */
				if (webEnagage.getLeadInfoDetails().size() > 0) {
					for (WebEngageLeadInfoDetail welid : webEnagage.getLeadInfoDetails()) {
						LeadInfoDetail lide = new LeadInfoDetail();
						if (welid.getAdditonalDataInfoText().isEmpty() || welid.getAdditonalDataInfoText().equals(null)
								|| welid.getAdditonalDataInfoText().equals("")) {
							lide.setAdditonalDataInfoText(null);
							lide.setAdditonalDataInfoText2(null);
						} else {
							lide.setAdditonalDataInfoText(welid.getAdditonalDataInfoText());
							lide.setAdditonalDataInfoText2(welid.getAdditonalDataInfoText2());
						}

						lide.setPhoneNumber(welid.getPhoneNumber());
						lide.setTextFields(welid.getTextFields());
						leadInfo.getLeadInfoDetails().add(lide);
					}
				}
				LOGGER.info("webEnagage.getCheckRcs().equalsIgnoreCase(Y):: " + webEnagage.toString());
				returnedLead = leadService.rcsAPIsaveLeadWithDnDCheck3(leadInfo, isDND, webEnagage.getCheckRcs());
			}
			LeadUploadResponse response = new LeadUploadResponse();
			response.setLeadName(webEnagage.getLeadName());
			response.setTotalRecords(Integer.valueOf(webEnagage.getLeadInfoDetails().size()));
			// response.setResid(uuid);
			Date now = new Date();
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			response.setUploadDtm(df.format(now));
			result.put("Status", HttpStatus.OK.toString());
			result.put("message", response);
			LOGGER.info("Success Response From /generateLead API::=>" + result.toString());
			return new ResponseEntity(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put("Status", Integer.valueOf(500));
			result.put("message", e.getMessage());
			LOGGER.info("Error Response From /generateLead API::=>" + result.toString());
			e.printStackTrace();
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = { "/leadInfo" }, produces = { "application/json", "application/xml" }, consumes = {
			"application/json", "application/xml" })
	public ResponseEntity<?> insertLeadInfo(@RequestBody LeadInfo leadInfo,
			@RequestParam(value = "isDND", required = false, defaultValue = "true") boolean isDND) throws Exception {
		LOGGER.info("Controller saveLead =>" + leadInfo);
		LeadInfoEntity returnedLead = null;
		HashMap<String, Object> result = new HashMap<>();
		if (leadInfo.getLeadSchedule() == null) {
			result.put("Status", "Failure");
			result.put("message", "Schedule not found for Lead");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		if (Objects.isNull(leadInfo.getLeadId())) {
			LeadInfoEntity exitsleadId = null;
			exitsleadId = this.leadInfoRepository.findFirstByLeadName(leadInfo.getLeadName());
			if (Objects.nonNull(exitsleadId)) {
				LOGGER.info("Working On Existing LeadId ::=>" + exitsleadId.getLeadId());
				leadInfo.setLeadId(exitsleadId.getLeadId());
			}
		}
		if (!this.leadService.uploadAllowed(leadInfo)) {
			result.put("Status", "Failure");
			result.put("message", "Daily Limit Breached, Upload not allowed !!");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		try {
			returnedLead = this.leadService.saveLeadWithDnDCheck2(leadInfo, isDND);
		} catch (Exception e) {
			LOGGER.error("Exception ", e);
			result.put("Status", "Failure");
			result.put("message", e.getLocalizedMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		if (returnedLead == null) {
			result.put("Status", "Failure");
			result.put("message", "No Object");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		this.beanUtils.copyProperties(leadInfo, returnedLead);
		leadInfo.getLeadSchedule().setScheduleId(returnedLead.getScheduleId());
		result.put("Status", HttpStatus.OK.toString());
		result.put("Lead", leadInfo);
		return new ResponseEntity(result, HttpStatus.CREATED);
	}

	@PostMapping({ "/leadInfo/upload" })
	public ResponseEntity uploadMediaForNewLead(@RequestPart(value = "file", required = true) MultipartFile file,
			@RequestParam("leadInfoJson") String leadInfoJson,
			@RequestParam(value = "isDND", required = false, defaultValue = "true") boolean isDND,
			@RequestParam(value = "isDuplicate", required = false, defaultValue = "true") boolean isDuplicate)
			throws Exception {
		LOGGER.info("Controller Upload New Lead =>" + file.getOriginalFilename());
		LeadInfo leadInfo = (LeadInfo) (new ObjectMapper()).readValue(leadInfoJson, LeadInfo.class);
		leadInfo.getLeadInfoDetails().clear();
		HashMap<String, Object> result = new HashMap<>();
		String contentType = file.getContentType();
		if (!"text/csv".equals(contentType) && !"application/octet-stream".equals(contentType)
				&& !"application/vnd.ms-excel".equals(contentType)
				&& !"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
			result.put("Status", "Failure");
			result.put("message", "Invalid File Type: " + contentType);
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		boolean isUpdate = false;
		if (leadInfo.getLeadSchedule() == null) {
			result.put("Status", "Schedule not found for Lead " + leadInfo.getLeadName());
			result.put("message", "Schedule not found for Lead");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		if (leadInfo.getRetryInfo() == null) {
			result.put("Status", "Retry Info not found for Lead " + leadInfo.getLeadName());
			result.put("message", "Retry Info not found for Lead");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		try {
			if (leadInfo.getLeadId() != null)
				isUpdate = true;
			if (Objects.isNull(leadInfo.getLeadId())) {
				LeadInfoEntity exitsleadId = null;
				exitsleadId = this.leadInfoRepository.findFirstByLeadName(leadInfo.getLeadName());
				if (Objects.nonNull(exitsleadId)) {
					LOGGER.info("Working On Existing LeadId ::=>" + exitsleadId.getLeadId());
					leadInfo.setLeadId(exitsleadId.getLeadId());
				}
			}
			if (!this.leadService.uploadAllowed(leadInfo, file)) {
				result.put("Status", "Failure");
				result.put("message", "Daily Limit Breached, Upload not allowed !!");
				return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
			}
			// leadInfo.setRcsMsgTypeId(1L);
			LeadInfo returnedLead = this.leadService.save(leadInfo, file, isDND, isDuplicate);
			if (returnedLead == null) {
				result.put("Status",
						"Campaign Id " + leadInfo.getCampaignId() + " not found for Lead " + leadInfo.getLeadName());
				result.put("message", leadInfo.toString());
				return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
			}
			if (isUpdate)
				this.leadService.updateAttemptInfo(returnedLead);
			this.beanUtils.copyProperties(leadInfo, returnedLead);
			result.put("Status", HttpStatus.OK.toString());
			result.put("Lead", leadInfo);
			return new ResponseEntity(result, HttpStatus.CREATED);
		} catch (Exception e) {
			LOGGER.error("Exception:", e);
			result.put("Status", "Error in uploading lead. Plead contact administrator " + e.getLocalizedMessage());
			result.put("message", "Error in uploading lead. Plead contact administrator " + e.getLocalizedMessage());
			return new ResponseEntity(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@GetMapping({ "/rcs" })
	public ResponseEntity<?> processTestNumber(@RequestParam("phoneNumber") String phoneNumber) {
		HashMap<String, Object> result = new HashMap<>();
		try {
			this.leadService.processTestNumber(phoneNumber);
			result.put("Status", HttpStatus.OK.toString());
			result.put("message", "Success");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("Status", HttpStatus.BAD_REQUEST.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping({ "/findByLeadInfoId" })
	public ResponseEntity<?> findByLeadInfoId(@RequestParam("leadId") Long leadId) {
		HashMap<String, Object> result = new HashMap<>();
		try {
			List<LeadInfoEntity> leadInfo = this.leadInfoRepository.findByLeadId(leadId);
			LeadInfo modelLeadInfo = new LeadInfo();
			this.beanUtils.copyProperties(modelLeadInfo, leadInfo);
			for (LeadInfoEntity lead : leadInfo) {
				ScheduleEntity scheduleEntity = this.scheduleRepository.findById(lead.getScheduleId()).orElse(null);
				if (scheduleEntity != null) {
					Schedule schedule = new Schedule();
					this.beanUtils.copyProperties(schedule, scheduleEntity);
					modelLeadInfo.setLeadSchedule(schedule);
				}
			}
			result.put("Status", HttpStatus.OK.toString());
			result.put("message", "Success");
			result.put("Lead Info", modelLeadInfo);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("Status", HttpStatus.BAD_REQUEST.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping({ "/findLeadByCampaignId" })
	public ResponseEntity<?> findLeadByCampaignId(@RequestParam("leadCampaignId") Long findLeadByCampaignId) {
		HashMap<String, Object> result = new HashMap<>();
		try {
			List<LeadInfoEntity> leadInfo = this.leadInfoRepository.findByCampaignId(findLeadByCampaignId);
			LeadInfo modelLeadInfo = new LeadInfo();
			this.beanUtils.copyProperties(modelLeadInfo, leadInfo);
			for (LeadInfoEntity lead : leadInfo) {
				ScheduleEntity scheduleEntity = this.scheduleRepository.findById(lead.getScheduleId()).orElse(null);
				if (scheduleEntity != null) {
					Schedule schedule = new Schedule();
					this.beanUtils.copyProperties(schedule, scheduleEntity);
					modelLeadInfo.setLeadSchedule(schedule);
				}
			}
			result.put("Status", HttpStatus.OK.toString());
			result.put("message", "Success");
			result.put("Lead Info", modelLeadInfo);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("Status", HttpStatus.BAD_REQUEST.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping({ "/leadInfoList" })
	public ResponseEntity<?> findAllLeadInfo(@RequestParam("from") String from, @RequestParam("to") String to,
			@RequestParam("userId") Long userId, @RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "campaignId", required = false) String campaignId,
			@RequestParam(value = "leadId", required = false) String leadId) {
		HashMap<String, Object> result = new HashMap<>();
		List<LeadInfo> leadInfo = null;
		try {
			long leadCount = 0L;
			if (Objects.nonNull(campaignId) && Objects.isNull(leadId)) {
				leadCount = this.leadInfoRepository.countByCampaignIdBetweenDate(from + " 00:00:00", to + " 23:59:59",
						userId, campaignId);
			} else if (Objects.nonNull(campaignId) && Objects.nonNull(leadId)) {
				leadCount = this.leadInfoRepository.countByCampaignIdAndLeadIdBetweenDate(from + " 00:00:00",
						to + " 23:59:59", userId, campaignId, leadId);
			} else {
				leadCount = this.leadInfoRepository.countByUserIdAndCreatedDateBetween(from + " 00:00:00",
						to + " 23:59:59", userId);
			}
			LOGGER.info("Lead Count ::=> " + leadCount);
			leadInfo = this.leadService.getAllLeadInfoByUserIdAndDateBetween(from, to, userId, start, limit, campaignId,
					leadId);
			if (leadInfo.size() > 0) {
				result.put("Status", HttpStatus.OK.toString());
				result.put("message", "Success");
				result.put("totalCount", Long.valueOf(leadCount));
				result.put("Lead Info", leadInfo);
			} else {
				result.put("Status", Integer.valueOf(404));
				result.put("message", "Record Doesn't Exist.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("Status", HttpStatus.BAD_REQUEST.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping({ "/checkIfLeadNameExists" })
	public ResponseEntity<?> checkIfNameExists(@RequestParam("leadName") String leadName) {
		HashMap<String, String> result = new HashMap<>();
		try {
			if (this.leadService.checkIfNameExists(leadName)) {
				result.put("Status", HttpStatus.OK.toString());
				result.put("message", "Y");
			} else {
				result.put("Status", HttpStatus.OK.toString());
				result.put("message", "N");
			}
			return new ResponseEntity(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put("Status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping({ "/findAllLeadNameByUserId" })
	public ResponseEntity<?> findAllLeadInfoByUserId(@RequestParam("userId") Long userId) {
		HashMap<String, Object> result = new HashMap<>();
		List<LeadListing> leadListings = new ArrayList<>();
		try {
			List<LeadInfoEntity> leadList = this.leadInfoRepository.getAllByUserId(userId);
			if (leadList.size() > 0) {
				for (LeadInfoEntity lead : leadList) {
					LeadListing nameList = new LeadListing();
					nameList.setLeadId(lead.getLeadId().longValue());
					nameList.setLeadName(lead.getLeadName());
					leadListings.add(nameList);
				}
				return new ResponseEntity(leadListings, HttpStatus.OK);
			}
			result.put("message", "No Content");
			return new ResponseEntity(result, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@GetMapping(value = { "/action" }, produces = { "application/json" })
	public ResponseEntity actionOnUpdateLeadStatus(@RequestParam(value = "leadId", required = false) Integer leadId,
			@RequestParam(value = "action", required = true) String action) throws Exception {
		LOGGER.info("actionOnUpdateLeadStatus-> leadId" + leadId + " action->" + action);
		HashMap<String, String> result = new HashMap<>();
		if (leadId == null) {
			result.put("Status", HttpStatus.BAD_REQUEST.toString());
			result.put("message", "LeadId and Campaign id both are null");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		try {
			if (!VALID_ACTION.contains(action)) {
				result.put("Status", HttpStatus.EXPECTATION_FAILED.toString());
				result.put("message", "Invalid Action Given: " + action);
				return new ResponseEntity(result, HttpStatus.EXPECTATION_FAILED);
			}
			if (Objects.nonNull(leadId)) {
				this.leadInfoRepository.updateLeadCompletionStatusAndStatus(leadId.intValue(), action);
				this.rabbitMQSender.sendLeadId(leadId,
						leadInfoRepository.findByLeadId(Long.valueOf(leadId)).get(0).getRcsMsgTypeId());
				result.put("Status", HttpStatus.OK.toString());
				result.put("Lead", "Action [ " + action + " ] Submitted Successfully For Lead Id [ " + leadId + " ]");
			}
		} catch (Exception e) {
			result.put("Status", HttpStatus.EXPECTATION_FAILED.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping({ "/leadNameAndIdList" })
	public ResponseEntity<?> leadNameAndIdList(@RequestParam("campaignId") Long campignId,
			@RequestParam("userId") Long userId) {
		HashMap<String, Object> result = new HashMap<>();
		List<LeadListing> leadListings = new ArrayList<>();
		try {
			List<LeadInfoEntity> leadList = this.leadInfoRepository.getLeadIdAndNameByCampaignId(campignId, userId);
			if (leadList.size() > 0) {
				for (LeadInfoEntity lead : leadList) {
					LeadListing nameList = new LeadListing();
					nameList.setLeadId(lead.getLeadId().longValue());
					nameList.setLeadName(lead.getLeadName());
					leadListings.add(nameList);
				}
				return new ResponseEntity(leadListings, HttpStatus.OK);
			}
			result.put("message", "No Content");
			return new ResponseEntity(result, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@GetMapping({ "/getLeadAndCampaignNameWithIds" })
	public ResponseEntity<?> getLeadAndCampaignNameWithIds(@RequestParam("from") String from,
			@RequestParam("to") String to, @RequestParam("userId") Long userId) {
		LeadAndCampaingListing leadAndCampaign = null;
		HashMap<String, Object> result = new HashMap<>();
		List<LeadAndCampaingListing> leadListings = new ArrayList<>();
		try {
			List<Object[]> leadList = this.leadInfoRepository.findAllLeadAndCampaignNameWithIds(from + " 00:00:00",
					to + " 23:59:59", userId);
			if (leadList.size() > 0) {
				for (Object[] lead : leadList) {
					leadAndCampaign = new LeadAndCampaingListing();
					leadAndCampaign.setLeadName(String.valueOf(lead[0]));
					leadAndCampaign.setLeadId(String.valueOf(lead[1]));
					leadAndCampaign.setCampaignName(String.valueOf(lead[2]));
					leadAndCampaign.setCampId(String.valueOf(lead[3]));
					leadListings.add(leadAndCampaign);
				}
				return new ResponseEntity(leadListings, HttpStatus.OK);
			}
			result.put("message", "No Content");
			return new ResponseEntity(result, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	@GetMapping({ "/getUserAndBotIdByMnoForDemoRcsService" })
	public String getUserAndBotIdByMnoForDemoRcsService(@RequestParam("phone") String phone) {
		DemoRcsModel demoRcsModel = null;
		try {
			List<Object[]> leadLists = this.leadInfoRepository.getUserAndBotIdByMnoForDemoRcsService(phone);
			if (leadLists.size() > 0) {
				for (Object[] leadList : leadLists) {
					demoRcsModel = new DemoRcsModel();
					demoRcsModel.setUserName(String.valueOf(leadList[0]));
					demoRcsModel.setBotId(String.valueOf(leadList[1]));
					demoRcsModel.setTemplateCode(String.valueOf(leadList[2]));
					demoRcsModel.setLeadName(String.valueOf(leadList[3]));
					demoRcsModel.setCampaignName(String.valueOf(leadList[4]));
					demoRcsModel.setCampaignType(String.valueOf(leadList[5]));
					demoRcsModel.setDataSourceName(String.valueOf(leadList[6]));
				}
				return (new Gson()).toJson(demoRcsModel).toString();
			}
			return (new Gson()).toJson(demoRcsModel).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return (new Gson()).toJson(demoRcsModel).toString();
		}
	}

	/**
	 * Date 18-012024
	 * 
	 * @param leadId
	 * @param phoneNumberStatus
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	@GetMapping({ "/getLeadInfoDetailsBasedOnPhoneNumberStatusAndLeadId" })
	public ResponseEntity<?> getLeadInfoDetailsBasedOnPhoneNumberStatusAndLeadId(@RequestParam("leadName") Long leadId,
			@RequestParam("phoneNumberStatus") String phoneNumberStatus)
			throws IllegalAccessException, InvocationTargetException {
		HashMap<String, Object> result = new HashMap<>();

		List<String> leadInfoDetailEntityList = null;
		try {
			leadInfoDetailEntityList = leadInfoDetailRepository.getPhoneNumberByLeadIdAndPhoneNumberStatus(leadId,
					phoneNumberStatus);
			if (leadInfoDetailEntityList.size() > 0) {
				List<LeadInfoDetail> leadInfoDetails = new ArrayList<>();
				for (String phoneNumber : leadInfoDetailEntityList) {
					LeadInfoDetail leadInfoDetail = new LeadInfoDetail();
					leadInfoDetail.setPhoneNumber(phoneNumber);
					// beanUtils.copyProperties(leadInfoDetail, leadInfoDetailEntity);
					leadInfoDetails.add(leadInfoDetail);
				}
				result.put("status", 200);
				result.put("msg", "Record Founed.");
				result.put("data", leadInfoDetails);
				return new ResponseEntity(result, HttpStatus.OK);
			} else {
				result.put("status", 400);
				result.put("msg", "Data Not Found.");
				return new ResponseEntity(result, HttpStatus.OK);
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", e.getMessage());
			e.printStackTrace();
			return new ResponseEntity(result, HttpStatus.OK);
		}
	}

	// @GetMapping(value = "/rcsSmsApi", produces =
	// MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse> sendMsg(@RequestParam(required = true) String username,
			@RequestParam(required = true) String password, @RequestParam(required = true) String from,
			@RequestParam(required = false) String corelationid, @RequestParam(required = true) String to,
			@RequestParam(required = true) String text, @RequestParam(required = true) String unicode,
			@RequestParam(required = false) String param) throws Exception {
		LOGGER.info("REQEUST FROM RCS SMS API :: " + username);
		// System.out.println(param);
		UserEntity userEntity;
		try {
			userEntity = userRepository.getByUserName(username);
			if (userEntity == null)
				throw new NoResultException();
			ValidateClient(userEntity, password);
		} catch (NoResultException nre) {
			throw new UserNotFoundException(username);
		}

		catch (Exception nre) {
			nre.printStackTrace();

			throw new InvalidIPException("");
		}
		SmsApiWebRequest webRequest = null;
		webRequest = new SmsApiWebRequest();

		webRequest.setMessage(text);
		webRequest.setFrom(from);
		webRequest.setMsisdn(to);
		webRequest.setPassword(password);
		webRequest.setUsername(username);
		webRequest.setParams(param);
		if (corelationid != null)
			webRequest.setCorelationid(corelationid);
		webRequest.setUnicode(unicode);
		ApiResponse api = new ApiResponse();

		if (Objects.nonNull(webRequest)) {
			rabbitMQSender.sendToSmsApiQueue(webRequest);
			api.setState("SUBMIT_ACCEPTED");
			api.setPdu("");
			api.setCorelationid(corelationid);
			api.setDescription("Message Accepted.");

		} else {
			api.setState("SUBMIT_FAILED");
			api.setPdu("");
			api.setCorelationid(corelationid);
		}
		return new ResponseEntity<ApiResponse>(api, HttpStatus.OK);

	}

	public boolean ValidateClient(UserEntity user, String password) throws NoResultException {

		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject object = (JsonObject) parser.parse(user.getCredentials());// response will be the json String
		Credentials objcredentials = gson.fromJson(object, Credentials.class);
		LOGGER.info(objcredentials.toString());
		if (objcredentials.getApi_pwd().equals(password))
			return true;
		else
			throw new NoResultException();
	}

	@PostMapping(value = { "/generateLeadByClient" }, produces = { "application/json", "application/xml" }, consumes = {
			"application/json", "application/xml" })
	public ResponseEntity<?> generateLeadByClient(@RequestBody LeadInfo leadInfo,
			@RequestParam(value = "isDND", required = false, defaultValue = "true") boolean isDND) throws Exception {
		LOGGER.info("Controller saveLead =>" + leadInfo);
		LeadInfoEntity returnedLead = null;
		HashMap<String, Object> result = new HashMap<>();
		if (leadInfo.getLeadSchedule() == null) {
			result.put("Status", "Failure");
			result.put("message", "Schedule not found for Lead");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		if (Objects.isNull(leadInfo.getLeadId())) {
			LeadInfoEntity exitsleadId = null;
			exitsleadId = this.leadInfoRepository.findFirstByLeadName(leadInfo.getLeadName());
			if (Objects.nonNull(exitsleadId)) {
				LOGGER.info("Working On Existing LeadId ::=>" + exitsleadId.getLeadId());
				leadInfo.setLeadId(exitsleadId.getLeadId());
			}
		}
		if (!this.leadService.uploadAllowed(leadInfo)) {
			result.put("Status", "Failure");
			result.put("message", "Daily Limit Breached, Upload not allowed !!");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		try {
			returnedLead = this.leadService.saveLeadWithDnDCheck2(leadInfo, isDND);
		} catch (Exception e) {
			LOGGER.error("Exception ", e);
			result.put("Status", "Failure");
			result.put("message", e.getLocalizedMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		if (returnedLead == null) {
			result.put("Status", "Failure");
			result.put("message", "No Object");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		LeadUploadResponse response = new LeadUploadResponse();
		response.setLeadName(returnedLead.getLeadName());
		response.setTotalRecords(Integer.valueOf(leadInfo.getLeadInfoDetails().size()));
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		response.setUploadDtm(df.format(returnedLead.getCreatedDate()));
		result.put("Status", HttpStatus.OK.toString());
		result.put("message", response);
		LOGGER.info("Success " + response);
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@PostMapping({ "/rcsLeadGeneratedApi" })
	public ResponseEntity<?> clientLeadGenerated(@RequestBody ClientLeadCreatedPojo clientRequest,
			@RequestParam("isDND") boolean isDND) throws Exception {
		LOGGER.info("WebEngage Request : : = >" + clientRequest.toString());

		LeadInfoEntity returnedLead = null;
		HashMap<String, Object> result = new HashMap<>();
		try {
			if (!this.uKey.equalsIgnoreCase(clientRequest.getuKey())) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "API Key Required.");
				LOGGER.info("Response From /generateLead API::=>" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			if (Objects.isNull(clientRequest.getCampaignId()) || clientRequest.getCampaignId() == 0) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "Campaign Id Required.");
				LOGGER.info("Response From /generateLead API::=>" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			if (Objects.isNull(clientRequest.getLeadName()) || clientRequest.getLeadName().isEmpty()) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "Lead Name Required.");
				LOGGER.info("Response From /generateLead API::=>" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			if (clientRequest.getLeadInfoDetails().size() == 0) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "Inside LeadInfoDetails Phone Number Required.");
				LOGGER.info("Response From /generateLead API::=>" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			LeadInfo leadInfo = new LeadInfo();
			if (Objects.nonNull(clientRequest.getLeadName()) || !clientRequest.getLeadName().isEmpty()) {
				LeadInfoEntity exitsleadId = null;
				exitsleadId = leadInfoRepository.findFirstByLeadName(clientRequest.getLeadName());

				if (Objects.nonNull(exitsleadId)) {
					LOGGER.info("Working On Existing LeadId ::=>" + exitsleadId.getLeadId());
					leadInfo.setLeadId(exitsleadId.getLeadId());
				}
			}

			leadInfo.setRcsMsgTypeId(1L);
			leadInfo.setCampaignId(clientRequest.getCampaignId());
			leadInfo.setLeadName(clientRequest.getLeadName());
			// leadInfo.setUserId(clientRequest.getUserId());

			if (clientRequest.getLeadSchedule() != null) {
				Schedule schedule = new Schedule();
				schedule.setScheduleStartDtm(clientRequest.getLeadSchedule().getScheduleStartDtm());
				schedule.setScheduleEndDtm(clientRequest.getLeadSchedule().getScheduleEndDtm());
				schedule.setScheduleDay(clientRequest.getLeadSchedule().getScheduleDay());
				leadInfo.setLeadSchedule(schedule);
			}
			/**
			 * Set leadInfoDetails
			 */
			if (clientRequest.getLeadInfoDetails().size() > 0) {
				for (WebEngageLeadInfoDetail welid : clientRequest.getLeadInfoDetails()) {
					LeadInfoDetail lide = new LeadInfoDetail();
					lide.setAdditonalDataInfoText(welid.getAdditonalDataInfoText());
					lide.setAdditonalDataInfoText2(welid.getAdditonalDataInfoText2());
					lide.setPhoneNumber(welid.getPhoneNumber());
					lide.setTextFields(welid.getTextFields());
					leadInfo.getLeadInfoDetails().add(lide);
				}
			}

			if (leadInfo.getLeadSchedule() == null) {
				result.put(STATUS, "Failure");
				result.put(MESSAGE, "Schedule not found for Lead");
				LOGGER.info("Schedule not found for Lead::=>" + leadInfo.getLeadName());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);

			}

			/*
			 * if (Objects.isNull(leadInfo.getLeadId())) { LeadInfoEntity exitsleadId =
			 * null; exitsleadId =
			 * leadInfoRepository.findFirstByLeadName(leadInfo.getLeadName());
			 * 
			 * if (Objects.nonNull(exitsleadId)) {
			 * LOGGER.info("Working On Existing LeadId ::=>" + exitsleadId.getLeadId());
			 * leadInfo.setLeadId(exitsleadId.getLeadId()); } }
			 */
			/*
			 * if (!leadService.uploadAllowed(leadInfo)) { result.put(STATUS, "Failure");
			 * result.put(MESSAGE, "Daily Limit Breached, Upload not allowed !!");
			 * LOGGER.info("Daily Limit Breached, Upload not allowed ::=>" +
			 * leadInfo.getLeadName()); return new ResponseEntity(result,
			 * HttpStatus.NOT_FOUND);
			 * 
			 * }
			 */

			returnedLead = leadService.rcsAPIsaveLeadWithDnDCheck3(leadInfo, isDND, clientRequest.getCheckRcs());
			LOGGER.info("After Saved Lead Details Response ::=>" + returnedLead.toString());
			LeadUploadResponse response = new LeadUploadResponse();
			response.setLeadName(leadInfo.getLeadName());
			response.setTotalRecords(Integer.valueOf(leadInfo.getLeadInfoDetails().size()));
			response.setResid(String.valueOf(returnedLead.getLeadId()));

			Date now = new Date();
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			response.setUploadDtm(df.format(now));
			result.put("Status", HttpStatus.OK.toString());
			result.put("message", response);
			LOGGER.info("Success Response From /generateLead API::=>" + result.toString());
			return new ResponseEntity(result, HttpStatus.OK);

		} catch (Exception e) {
			LOGGER.error("Exception ", e);
			result.put(STATUS, "Failure");
			result.put(MESSAGE, e.getLocalizedMessage());
			LOGGER.info("Exception ::=>" + clientRequest.getLeadName() + " And Response ::=>"
					+ new Gson().toJson(result).toString());
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);

		}
	}

}
