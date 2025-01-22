package com.messaging.rcs.service;

import static com.messaging.rcs.util.SystemConstants.ACTIVE;
import static com.messaging.rcs.util.SystemConstants.BLACKLIST_NUMBER;
import static com.messaging.rcs.util.SystemConstants.COMPLETED;
import static com.messaging.rcs.util.SystemConstants.CREATED;
import static com.messaging.rcs.util.SystemConstants.DUPLICATE_NUMBER;
import static com.messaging.rcs.util.SystemConstants.INACTIVE;
import static com.messaging.rcs.util.SystemConstants.INVALID_NUMBER;
import static com.messaging.rcs.util.SystemConstants.LEAD_CREATED;
import static com.messaging.rcs.util.SystemConstants.NONRCS_NUMBER;
import static com.messaging.rcs.util.SystemConstants.OK;
import static com.messaging.rcs.util.SystemConstants.RUNNING;
import static com.messaging.rcs.util.SystemConstants.SCHEDULED_LATER;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.services.rcsbusinessmessaging.v1.RbmApiHelper;
import com.google.api.services.rcsbusinessmessaging.v1.model.BatchGetUsersResponse;
import com.google.gson.Gson;
import com.messaging.rcs.consumer.RabbitMQSenderTelco;
import com.messaging.rcs.consumer.WebRequest;
import com.messaging.rcs.domain.AttemptInfoEntity;
import com.messaging.rcs.domain.BlackListEntity;
import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.LeadFileStorage;
import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.domain.OperatorApiConfigEntity;
import com.messaging.rcs.domain.RetryInfoEntity;
import com.messaging.rcs.domain.ScheduleEntity;
import com.messaging.rcs.domain.UserBotMappingEntity;
import com.messaging.rcs.domain.UserDailyUsageEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.repository.DNDRepository;
import com.messaging.rcs.email.service.EmailService;
import com.messaging.rcs.model.LeadInfo;
import com.messaging.rcs.model.LeadInfoDetail;
import com.messaging.rcs.model.RetryInfo;
import com.messaging.rcs.model.Schedule;
import com.messaging.rcs.model.Template;
import com.messaging.rcs.repository.AttemptRepository;
import com.messaging.rcs.repository.BlackListRepository;
import com.messaging.rcs.repository.CampaignRepository;
import com.messaging.rcs.repository.LeadFileStorageRepository;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.repository.LeadInfoRepository;
import com.messaging.rcs.repository.OperatorApiConfigRepository;
import com.messaging.rcs.repository.OperatorRepository;
import com.messaging.rcs.repository.RetryRepository;
import com.messaging.rcs.repository.ScheduleRepository;
import com.messaging.rcs.repository.TemplateRepository;
import com.messaging.rcs.repository.UserBotMappingRepository;
import com.messaging.rcs.repository.UserDailyUsageRepository;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.schedular.BotTokenAPIService;
import com.messaging.rcs.schedular.TokenPojo;
import com.messaging.rcs.util.CSVHelper;
import com.messaging.rcs.util.CSVUtils;
import com.messaging.rcs.util.FileReadThread;
import com.messaging.rcs.util.JSONReadThread;
import com.messaging.rcs.util.RcsApiJsonReadThread;
import com.messaging.rcs.util.StringUtil;
import com.messaging.rcs.vi.bot.request.BulkTokenResponse;

/**
 * 
 * @author RahulRajput 29-05-2023
 *
 */
@Service
public class LeadServiceImpl implements LeadService {
	private static final Logger LOGGER = Logger.getLogger(LeadServiceImpl.class.getName());
	@Value("${rcsMsgCredentials.file.path}")
	private String rcsMsgCredentials;
	@Value("${rcsEnabled}")
	public String rcsEnabled;
	@Autowired
	private CampaignRepository campaignRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserDailyUsageRepository userDailyUsageRepository;
	@Autowired
	private LeadInfoDetailRepository leadInfoDetailRepository;
	@Autowired
	private LeadInfoRepository leadInfoRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private RetryRepository retryRepository;
	@Autowired
	private BlackListRepository blackListRepository;
	@Autowired
	private AttemptRepository attemptRepository;
	@Autowired
	@Qualifier("mySqlDataSourceTemplate")
	JdbcTemplate jdbcTemplate;
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserService userService;
	@Autowired
	private BotTokenAPIService botTokenAPIService;
	private BeanUtilsBean beanUtils = new BeanUtilsBean();
	@Autowired
	private DNDRepository dndRepository;
	@Autowired
	private RabbitMQSenderTelco rabbitMQSender;
	@Autowired
	private OperatorApiConfigRepository operatorApiConfigRepository;
	@Autowired
	private OperatorRepository operatorRepository;
	@Autowired
	private UserBotMappingRepository userBotMappingRepository;
	@Autowired
	RedisTemplate<String, BlackListEntity> blacklistTemplate;

	@Autowired
	private TemplateRepository tempRepository;
	@Autowired
	private LeadFileStorageRepository leadFileStorageRepository;

	/*
	 * @Autowired private FlieReadLeadInofDetails flieReadLeadInofDetails;
	 */
	@Override
	public LeadInfoEntity saveLeadWithDnDCheck2(LeadInfo leadInfo, boolean isDND) throws Exception {
		LeadInfoEntity leadInfoEntity = saveLeadWithDnDCheck(leadInfo, isDND);
		if (leadInfoEntity != null) {
			LOGGER.info("Filling Counters");
			leadInfo.setLeadId(leadInfoEntity.getLeadId());
			fillCounters(leadInfo);
			LOGGER.info("Filled Counters");
			leadInfoEntity.setFileTotalRecord(Long.valueOf(String.valueOf(leadInfo.getLeadInfoDetails().size())));
			leadInfoEntity.setCountOfNumbers(leadInfo.getCountOfNumbers());
			leadInfoEntity.setCountOfNonRcsNumbers(leadInfo.getCountOfNonRcsNumbers());
			leadInfoEntity.setCountOfDuplicateNumbers(leadInfo.getCountOfDuplicateNumbers());
			leadInfoEntity.setCountOfInvalidNumbers(leadInfo.getCountOfInvalidNumbers());
			leadInfoEntity.setCountOfBlackListNumbers(leadInfo.getCountOfBlackListNumbers());
			// leadInfoEntity.setLeadStatus("Completed");
			// leadInfoEntity.setLeadCompletionStatus("Completed");

			leadInfoEntity = leadInfoRepository.save(leadInfoEntity);
		}
		return leadInfoEntity;
	}

	public void rabbitMQSenderSetWebRequestInfoDetails(UserEntity campUser, LeadInfoDetailEntity leadInfoDetails,
			LeadInfoEntity leadInfo, String templateCode) throws Exception {
		WebRequest webRequest = null;
		try {
			/*
			 * leadInfoDetailRepository.
			 * updatePhoneNumberStatusByLIDIdAndLeadIdAfterCheckRcsOrNonRcsNo(
			 * leadInfoDetails.getLeadInfoDetailId(), OK, leadInfoDetails.getLeadId());
			 * 
			 */ webRequest = new WebRequest();
			webRequest.setBoatid(campUser.getBotId());
			webRequest.setCustomeparam(leadInfoDetails.getAdditonalDataInfoText());
			webRequest.setCustomeparamValue(leadInfoDetails.getAdditonalDataInfoText2());
			webRequest.setMsgid(String.valueOf(leadInfoDetails.getLeadInfoDetailId()));
			webRequest.setTemplatecode(templateCode);
			webRequest.setUsercontact(leadInfoDetails.getPhoneNumber());
			rabbitMQSender.sendInfoDetails(webRequest, String.valueOf(leadInfo.getLeadId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Created By Rahul for cliet API
	 * 
	 * @param leadInfo
	 * @param isDND
	 * @return
	 * @throws Exception
	 */
	@Override
	public LeadInfoEntity rcsAPIsaveLeadWithDnDCheck3(LeadInfo leadInfo, boolean isDND, String checkRcs)
			throws Exception {
		LeadInfoEntity leadInfoEntity = null;
		leadInfoEntity = rcsAPIsaveLeadWithDnDCheck2(leadInfo, isDND, checkRcs);
		/*
		 * if (leadInfoEntity != null) { LOGGER.info("Filling Counters");
		 * leadInfo.setLeadId(leadInfoEntity.getLeadId()); fillCounters(leadInfo);
		 * LOGGER.info("Filled Counters");
		 * leadInfoEntity.setCountOfNumbers(leadInfo.getCountOfNumbers());
		 * leadInfoEntity.setCountOfNonRcsNumbers(leadInfo.getCountOfNonRcsNumbers());
		 * leadInfoEntity.setCountOfDuplicateNumbers(leadInfo.getCountOfDuplicateNumbers
		 * ());
		 * leadInfoEntity.setCountOfInvalidNumbers(leadInfo.getCountOfInvalidNumbers());
		 * leadInfoEntity.setCountOfBlackListNumbers(leadInfo.getCountOfBlackListNumbers
		 * ()); leadInfoEntity = leadInfoRepository.save(leadInfoEntity); }
		 */
		return leadInfoEntity;
	}

	@Transactional(rollbackFor = Exception.class)
	public LeadInfoEntity rcsAPIsaveLeadWithDnDCheck2(LeadInfo leadInfo, boolean isDnd, String checkRcs)
			throws Exception {
		boolean isUpdate = false;
		boolean isFonada = false;
		boolean scheduleUpdated = false;
		boolean retryUpdated = false;
		UserEntity userEntity = null;
		Template templateCode = null;
		LOGGER.info("SavedLead: createLead ->" + leadInfo + "->" + leadInfo.getCreatedBy());
		CampaignEntity campaignEntity = campaignRepository.findById(leadInfo.getCampaignId()).orElse(null);

		if (campaignEntity == null) {
			LOGGER.error(
					"No Campaign found with ID " + leadInfo.getCampaignId() + " for Lead " + leadInfo.getLeadName());
			throw new Exception(
					"No Campaign found with ID " + leadInfo.getCampaignId() + " for Lead" + leadInfo.getLeadName());
			// return null;
		}
		leadInfo.setUserId(campaignEntity.getUserId());
		templateCode = tempRepository.findById(campaignEntity.getTemplateId()).get();
		LOGGER.info("TEMPLATE CODE :: " + templateCode);

		/*
		 * if (campaignEntity.getIsSendToRmq() == 1) {
		 * campaignRepository.updateCampaignIsSendToRmq(campaignEntity.getCampaignId(),
		 * 0); }
		 */
		if (Objects.nonNull(campaignEntity.getRcsMsgTypeId())) {
			leadInfo.setRcsMsgTypeId(campaignEntity.getRcsMsgTypeId());
		}
		userEntity = userRepository.getUserEntityByUserId(campaignEntity.getUserId());

		LeadInfoEntity leadInfoEntity = new LeadInfoEntity();
		if (leadInfo.getLeadId() != null) {
			// By Rahul Kumar
			partitionCheck(leadInfo.getLeadId());
			isUpdate = true;
			LeadInfoEntity tmpLeadInfo = leadInfoRepository.findById(leadInfo.getLeadId()).orElse(null);
			if (tmpLeadInfo.getLeadName().toLowerCase().contains("fonada")) {
				isFonada = true;
			}
			leadInfo.setCountOfNumbers(tmpLeadInfo.getCountOfNumbers());
			leadInfo.setCountOfBlackListNumbers(tmpLeadInfo.getCountOfBlackListNumbers());
			leadInfo.setCountOfDuplicateNumbers(tmpLeadInfo.getCountOfDuplicateNumbers());
			leadInfo.setCountOfInvalidNumbers(tmpLeadInfo.getCountOfInvalidNumbers());
			leadInfo.setCountOfNonRcsNumbers(tmpLeadInfo.getCountOfNonRcsNumbers());
			leadInfo.setCreatedBy(userEntity.getUserName());
			leadInfo.setCreatedDate(new Date());
			leadInfo.setLastModifiedBy(userEntity.getUserName());
			leadInfo.setLastModifiedDate(new Date());
			if (!StringUtils.hasLength(leadInfo.getLeadName())) {
				leadInfo.setLeadName(tmpLeadInfo.getLeadName());
			}
			leadInfo.setInsertDtm(tmpLeadInfo.getInsertDtm());
			if (!StringUtils.hasLength(leadInfo.getLeadAction())) {
				leadInfo.setLeadAction(tmpLeadInfo.getLeadAction());
			}

			if (!StringUtils.hasLength(leadInfo.getLeadPriorityType())) {
				leadInfo.setLeadPriorityType(tmpLeadInfo.getLeadPriorityType());
			}

			if (null == leadInfo.getLeadPriority()) {
				leadInfo.setLeadPriority(tmpLeadInfo.getLeadPriority());
			}

			leadInfo.setInsertDtm(tmpLeadInfo.getInsertDtm());
			leadInfo.setProcessDtm(tmpLeadInfo.getProcessDtm());
			LOGGER.info("Previous status ====> " + tmpLeadInfo.getLeadCompletionStatus());
			/*
			 * if (tmpLeadInfo.getLeadCompletionStatus().equalsIgnoreCase(COMPLETED) &&
			 * isLeadToBeRunToday(leadInfo)) { LOGGER.info("Marking lead as Running");
			 * leadInfo.setLeadCompletionStatus(RUNNING); } else {
			 * LOGGER.info("Marking lead as " + tmpLeadInfo.getLeadCompletionStatus());
			 */
			leadInfo.setLeadCompletionStatus("Active");
			// }
		} else {
			leadInfo.setCountOfNumbers(0);
			leadInfo.setCountOfBlackListNumbers(0);
			leadInfo.setCountOfDuplicateNumbers(0);
			leadInfo.setCountOfInvalidNumbers(0);
			leadInfo.setCountOfNonRcsNumbers(0);
		}
		beanUtils.copyProperties(leadInfoEntity, leadInfo);
		Schedule schedule = leadInfo.getLeadSchedule();
		ScheduleEntity scheduleEntity = new ScheduleEntity();
		if (!isFonada && schedule != null) {
			beanUtils.copyProperties(scheduleEntity, schedule);
			if (schedule.getScheduleId() == null || schedule.getScheduleId() <= 0) {
				scheduleEntity.setCreatedDate(new Date());
				scheduleEntity.setCreatedBy(leadInfo.getCreatedBy());
			} else {
				scheduleUpdated = true;
			}
			scheduleEntity.setLastModifiedDate(new Date());
			scheduleEntity.setLastModifiedBy(leadInfo.getLastModifiedBy());
			scheduleEntity.setCreatedBy(schedule.getCreatedBy());
			scheduleEntity = scheduleRepository.save(scheduleEntity);
			leadInfoEntity.setScheduleId(scheduleEntity.getScheduleId());
			if (isLeadToBeRunToday(leadInfoEntity)) {
				leadInfoEntity.setLeadCompletionStatus(RUNNING);
			} else {
				leadInfoEntity.setLeadCompletionStatus(SCHEDULED_LATER);
			}
		} else if (isFonada && schedule != null) {
			leadInfoEntity.setScheduleId(schedule.getScheduleId());
		}

		if (!isUpdate) {
			leadInfoEntity.setCreatedDate(new Date());
			leadInfoEntity.setCreatedBy(userEntity.getUserName());
			leadInfoEntity.setLeadCompletionStatus(CREATED);
		}
		leadInfoEntity.setLastModifiedDate(new Date());
		leadInfoEntity.setLastModifiedBy(leadInfo.getLastModifiedBy());
		leadInfoEntity.setLeadStatus(INACTIVE);
		leadInfoEntity = leadInfoRepository.save(leadInfoEntity);

		// GOING ON CREATING QUEUE BASED ON LEADID
		// rabbitMQSender.sendLeadId(String.valueOf(leadInfoEntity.getLeadId()));

		if (leadInfo != null) {
			leadInfo.setLeadId(leadInfoEntity.getLeadId());
			List<LeadInfoDetail> leadInfoDetails = leadInfo.getLeadInfoDetails();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (LeadInfoDetail leadInfoDetail : leadInfoDetails) {
				StringBuilder sb = new StringBuilder();
				sb.append(leadInfoDetail.toRecord()).append("\n");
				baos.write(sb.toString().getBytes());
			}
			byte[] bytes = baos.toByteArray();
			InputStream in = new ByteArrayInputStream(bytes);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

			int threadNo = 10;
			Thread t1;
			List<Connection> connectionList = new ArrayList<>();
			List<Thread> fileReadThreadList = new ArrayList<>();

			partitionCheck(leadInfo.getLeadId());
			try {
				for (int i = 0; i < threadNo; i++) {
					LOGGER.info("upload leads via json array starting thread->" + i);
					Connection con = null;

					t1 = new Thread(new RcsApiJsonReadThread(leadInfo, leadInfoDetailRepository, bufferedReader, false,
							blackListRepository, userEntity, jdbcTemplate, false, 0l, botTokenAPIService, dndRepository,
							checkRcs, rabbitMQSender,blacklistTemplate), "Thread " + i);
					fileReadThreadList.add(t1);
					t1.start();
				}
				for (Thread t : fileReadThreadList) {
					t.join();
				}
			} catch (Exception r) {
				throw r;
			}

		}

		/*
		 * leadInfoEntity.setLeadStatus(ACTIVE);
		 * leadInfoEntity.setLeadCompletionStatus(ACTIVE); leadInfoEntity =
		 * leadInfoRepository.save(leadInfoEntity);
		 * 
		 * 
		 * // Going On To Sending LeadInfoDetail To Queue telco.leadId.queue
		 * 
		 * List<LeadInfoDetailEntity> leadInfoDetailsList = null; leadInfoDetailsList =
		 * leadInfoDetailRepository.findByLeadIdAndUserId(leadInfo.getLeadId());
		 * LOGGER.info("Going On To Send LeadInfoDetail To Queue For This Lead Id::" +
		 * leadInfo.getLeadId()); for (LeadInfoDetailEntity leadInfoDetail :
		 * leadInfoDetailsList) { rabbitMQSenderSetWebRequestInfoDetails(userEntity,
		 * leadInfoDetail, leadInfoEntity, templateCode.getTemplateCode()); }
		 * LOGGER.info("Filling Counters"); fillCounters(leadInfo);
		 * LOGGER.info("Filled Counters"); leadInfoEntity.setLeadStatus("Completed");
		 * leadInfoEntity.setLeadCompletionStatus("Completed");
		 * leadInfoEntity.setCountOfNumbers(leadInfo.getCountOfNumbers());
		 * leadInfoEntity.setCountOfNonRcsNumbers(leadInfo.getCountOfNonRcsNumbers());
		 * leadInfoEntity.setCountOfDuplicateNumbers(leadInfo.getCountOfDuplicateNumbers
		 * ());
		 * leadInfoEntity.setCountOfInvalidNumbers(leadInfo.getCountOfInvalidNumbers());
		 * leadInfoEntity.setCountOfBlackListNumbers(leadInfo.getCountOfBlackListNumbers
		 * ()); leadInfoRepository.save(leadInfoEntity);
		 */

		return leadInfoEntity;
	}

	@SuppressWarnings("unused")
	@Override
	public LeadInfo save(LeadInfo leadInfo, MultipartFile file, boolean isDND, boolean isDuplicate) throws Exception {
		LOGGER.info("Service Upload New Lead => " + file.getOriginalFilename());

		BufferedReader bufferedReader = null;
		BufferedReader br = null;
		bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(convert(file))));

		bufferedReader.readLine();
		long countFileRecord = bufferedReader.lines().count();
		System.out.println("bufferedReader.lines().count():::" + countFileRecord);

		leadInfo.setFileTotalRecord(countFileRecord);
		LeadInfoEntity leadInfoEntity = saveLead(leadInfo);
		if (leadInfoEntity == null) {
			throw new Exception("Lead info could not be saved");
		}
		leadInfoRepository.updateLeadStatus(leadInfoEntity.getLeadId(), "InProcess");

		/**
		 * Save leadInfoFile
		 */
		LeadFileStorage ss = leadFileStorageRepository.save(new LeadFileStorage(leadInfoEntity.getLeadId(), 0, isDND,
				isDuplicate, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()), file.getOriginalFilename(),
				file.getContentType(), file.getBytes()));
		LOGGER.info("Marked Lead As Inactive " + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName());
		// Commented By Rahul Kumar
		// LeadInfo returnedLead = save(leadInfoEntity.getLeadId().intValue(), file,
		// isDND, isDuplicate);
		beanUtils.copyProperties(leadInfo, leadInfoEntity);

		return leadInfo;
	}

	@Override
	public void updateAttemptInfo(LeadInfo returnedLead) {

		AttemptInfoEntity attemptInfoEntity = attemptRepository
				.getRoundOneEntity(returnedLead.getRetryInfo().getRetryId());
		int lastQualifiedCount = 0;
		int qualifiedNumbers = 0;
		if (attemptInfoEntity != null) {
			lastQualifiedCount = attemptInfoEntity.getQualifiedCount();
			qualifiedNumbers = getTotalValidNumbers(returnedLead);
			if (qualifiedNumbers > 0) {
				List<AttemptInfoEntity> modifiedAttemptInfoEntities = new ArrayList<>();
				/**
				 * 1. Get retry count 2. Compare this count with no. of entries in attempt table
				 * 3. if New Retry Count > attempt size, create extra rows, add retry ID as same
				 * of earlier
				 */
				int retryCount = returnedLead.getRetryInfo().getNoOfRetry();
				List<AttemptInfoEntity> attemptInfoEntities = attemptRepository
						.findByRetryId(returnedLead.getRetryInfo().getRetryId());
				if (!CollectionUtils.isEmpty(attemptInfoEntities)) {

					int attemptCount = attemptInfoEntities.size();
					LOGGER.info("Attempt count:" + attemptCount);
					int deltaAttempts = retryCount - attemptCount + 1;
					LOGGER.info("deltaAttempts count:" + deltaAttempts);
					if (deltaAttempts > 0) {
						for (int i = 0; i < deltaAttempts; i++) {
							// create new rows
							LOGGER.info("Adding new Row in attempt table : " + (i + 1) + " round " + attemptCount
									+ (i + 1));
							AttemptInfoEntity attempt = new AttemptInfoEntity();
							attempt.setQualifiedCount(0);
							attempt.setProcessedNumCount(0);
							attempt.setStatus("Pending");
							attempt.setRound(attemptCount + (i + 1));
							attempt.setRetryId(returnedLead.getRetryInfo().getRetryId());
							modifiedAttemptInfoEntities.add(attempt);
						}
					}
					int i = 0;
					for (AttemptInfoEntity attemptInfoEntity1 : attemptInfoEntities) {
						if (i < retryCount + 1) {
							attemptInfoEntity1.setStatus("Pending");
						} else {
							attemptInfoEntity1.setStatus("Not Required");
						}
						if (i == 0) {
							attemptInfoEntity1.setQualifiedCount(qualifiedNumbers);
						}
						modifiedAttemptInfoEntities.add(attemptInfoEntity1);
						i++;
					}
					if (modifiedAttemptInfoEntities.size() > 0) {
						attemptRepository.saveAll(modifiedAttemptInfoEntities);
						LOGGER.info("Updated Attempts [" + modifiedAttemptInfoEntities.size() + "] for ["
								+ returnedLead.getLeadId() + "=>" + returnedLead.getLeadName() + "]");
						LeadInfoEntity leadInfoEntity = new LeadInfoEntity();
						try {
							beanUtils.copyProperties(leadInfoEntity, returnedLead);
						} catch (Exception e) {
							LOGGER.error(e);
						}
					} else {
						LOGGER.info("Nothing to Update in Attempts for [" + returnedLead.getLeadId() + "=>"
								+ returnedLead.getLeadName() + "]");
					}
				}
			} else {
				LOGGER.info("Nothing to Update in Attempts for [" + returnedLead.getLeadId() + "=>"
						+ returnedLead.getLeadName() + "] as qualifiedNumbers is 0");
			}
		}

	}

	/**
	 * 2023-06-23 By Rahul Kumar
	 */
	public LeadInfo save(int Id, MultipartFile file, boolean isDND, boolean isDuplicate) throws Exception {
		LOGGER.info("SavedLead: createLead ->");
		LOGGER.info("Service Update Existing Lead via file");
		boolean ikslCampaign = false;
		BufferedReader bufferedReader = null;
		BufferedReader br = null;
		BufferedReader bufR = null;
		LeadInfo leadInfo = null;
		UserEntity userEntity = null;
		boolean isTransactionalCampaign = false;
		LOGGER.info("upload lead file leadId->" + Id);
		String fileName = file.getOriginalFilename();
		try {

			long startTime = System.currentTimeMillis();
			bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			bufferedReader.readLine();
			int idx = 0;
			List<Integer> dateIndexes = new ArrayList<>();
			List<Integer> numIndexes = new ArrayList<>();
			List<Integer> textIndexes = new ArrayList<>();
			List<Integer> langIndexes = new ArrayList<>();
			List<Integer> playIndexes = new ArrayList<>();
			int phoneNumberIdx = -1;

			leadInfo = getLeadInfo(Id);
			userEntity = userRepository.getUserEntityByUserId(leadInfo.getUserId());
			CampaignEntity campaignEntity = campaignRepository.getCampaignEntityByCampaignId(leadInfo.getCampaignId());

			LOGGER.info("Upload lead file leadInfo->" + leadInfo);
			List<Thread> fileReadThreadList = new ArrayList<>();
			String line = null;
			int duplicateCount = 0;
			List<String> duplicateRecords = new ArrayList<>();
			Thread dupThread = null;
			String dynmaicParamFromFileHeader = getDynmaicParamFromFileHeader(convert(file));
			if (leadInfo != null) {

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Set<String> setLines = new HashSet<>();
				if (isDuplicate) {
					leadInfo.setLeadStatus(INACTIVE);
					while ((line = bufferedReader.readLine()) != null) {
						if (!setLines.contains(line)) {
							setLines.add(line);
						} else {
							duplicateCount++;
							duplicateRecords.add(line);
						}
					}
					bufferedReader.close();

					for (String str : setLines) {
						StringBuilder sb = new StringBuilder();
						sb.append(str).append("\n");
						baos.write(sb.toString().getBytes());
					}
					byte[] bytes = baos.toByteArray();
					InputStream in = new ByteArrayInputStream(bytes);
					bufferedReader = new BufferedReader(new InputStreamReader(in));
				}
				int threadNo = 10;
				Thread t1;
				TokenPojo pojo = null;
				/*
				 * pojo = new
				 * Gson().fromJson(botTokenAPIService.getTokenFromClientAPI(userEntity.
				 * getBotToken()), TokenPojo.class);
				 */

				for (int i = 0; i < threadNo; i++) {
					LOGGER.info("upload lead file starting thread->" + i);

					t1 = new Thread(new FileReadThread(fileName, leadInfo, leadInfoDetailRepository, bufferedReader,
							isDND, isDuplicate, dateIndexes, numIndexes, textIndexes, langIndexes, playIndexes,
							blackListRepository, userEntity, jdbcTemplate, false, botTokenAPIService, dndRepository,
							pojo, dynmaicParamFromFileHeader, rcsEnabled), "Thread " + i);
					fileReadThreadList.add(t1);
					t1.start();
				}
				if (isDuplicate && !duplicateRecords.isEmpty()) {
					baos = new ByteArrayOutputStream();
					for (String str : duplicateRecords) {
						StringBuilder sb = new StringBuilder();
						sb.append(str).append("\n");
						baos.write(sb.toString().getBytes());
					}
					byte[] bytes = baos.toByteArray();
					InputStream in = new ByteArrayInputStream(bytes);
					bufR = new BufferedReader(new InputStreamReader(in));
					dupThread = new Thread(new FileReadThread(fileName, leadInfo, leadInfoDetailRepository,
							bufferedReader, isDND, isDuplicate, dateIndexes, numIndexes, textIndexes, langIndexes,
							playIndexes, blackListRepository, userEntity, jdbcTemplate, false, botTokenAPIService,
							dndRepository, pojo, dynmaicParamFromFileHeader, rcsEnabled), "Thread Dup");
					fileReadThreadList.add(dupThread);
					dupThread.start();
				}
			}
			for (Thread t : fileReadThreadList) {
				t.join();
			}

			LOGGER.info("Filling Counters");
			fillCounters(leadInfo);
			int actualDups = leadInfo.getCountOfDuplicateNumbers();
			leadInfo.setCountOfDuplicateNumbers(duplicateCount);
			leadInfo.setCountOfNumbers(leadInfo.getCountOfNumbers() + duplicateCount - actualDups);
			long startT = System.currentTimeMillis();
			leadInfo.setLeadStatus(ACTIVE);
			LeadInfoEntity leadInfoEntity = leadInfoRepository.findById(leadInfo.getLeadId()).orElse(null);
			if (leadInfoEntity != null) {
				LOGGER.info("Lead already existed, seems tested before uploading file..");
				leadInfoEntity.setCountOfNumbers(leadInfo.getCountOfNumbers());
				leadInfoEntity.setCountOfNonRcsNumbers(leadInfo.getCountOfNonRcsNumbers());
				leadInfoEntity.setCountOfDuplicateNumbers(leadInfo.getCountOfDuplicateNumbers());
				leadInfoEntity.setCountOfInvalidNumbers(leadInfo.getCountOfInvalidNumbers());
				leadInfoEntity.setCountOfBlackListNumbers(leadInfo.getCountOfBlackListNumbers());
				leadInfoEntity.setLeadStatus("Active");
				LOGGER.info("Previous Lead status ====> " + leadInfoEntity.getLeadCompletionStatus());

				if (COMPLETED.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())
						&& isLeadToBeRunToday(leadInfoEntity)) {
					LOGGER.info("Marking Lead as Running");
					leadInfo.setLeadCompletionStatus("Running");
					leadInfoEntity.setLeadCompletionStatus("Running");
				} else {
					if (!isLeadToBeRunToday(leadInfo)) {
						Schedule schedule = leadInfo.getLeadSchedule();
						if ("0".equalsIgnoreCase(schedule.getScheduleDay())) {
							LOGGER.info("Marking Lead as " + "Run Manually");
							leadInfo.setLeadCompletionStatus("Run Manually");
							leadInfoEntity.setLeadCompletionStatus("Run Manually");
						} else {
							LOGGER.info("Marking Lead as " + "Scheduled Later");
							leadInfo.setLeadCompletionStatus("Scheduled Later");
							leadInfoEntity.setLeadCompletionStatus("Scheduled Later");
						}
					} else {
						LOGGER.info("Marking Lead as " + leadInfoEntity.getLeadCompletionStatus());
						leadInfo.setLeadCompletionStatus(leadInfoEntity.getLeadCompletionStatus());
					}
				}
				leadInfoEntity = leadInfoRepository.save(leadInfoEntity);
				long totalT = System.currentTimeMillis() - startT;
				LOGGER.info("CHECKIT:Total Time in saving duplicates Leads for File " + fileName + " time taken: "
						+ (totalT) + " Ms");
				LeadInfoEntity finalLead = leadInfoEntity;
				Runnable r = () -> Notifications.getsInstance().notifyEventCaptured(LEAD_CREATED, finalLead);
				new Thread(r).start();
				LOGGER.info("Saved Lead via fileupload:" + leadInfoEntity);
				long endTime = System.currentTimeMillis();
				LOGGER.info("CHECKIT: Saved Leads for File " + fileName + " time taken: " + (endTime - startTime));
				LOGGER.info("SaveLead leadInfo.getCountOfNumbers(): " + leadInfoEntity.getCountOfNumbers());
				LOGGER.info(
						"SaveLead leadInfo.getCountOfInvalidNumbers(): " + leadInfoEntity.getCountOfInvalidNumbers());
				LOGGER.info("SaveLead leadInfo.getCountOfNonRcsNumbers(): " + leadInfoEntity.getCountOfNonRcsNumbers());
				LOGGER.info("SaveLead leadInfo.getCountOfBlackListNumbers(): "
						+ leadInfoEntity.getCountOfBlackListNumbers());
			} else {
				LOGGER.info("Saving Lead after file reading without testing..");
				saveLead(leadInfo);// leadInfo with counters
			}
		} catch (Exception ex) {
			LOGGER.error("Error:", ex);
			ex.printStackTrace();
			throw ex;
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufR != null) {
				bufR.close();
				bufR = null;
			}
		}
		/*
		 * UserEntity fuserEntity =
		 * userRepository.getUserEntityByUserId(leadInfo.getUserId()); CampaignEntity
		 * campaignEntity =
		 * campaignRepository.getCampaignEntityByCampaignId(leadInfo.getCampaignId());
		 * LeadInfo fLeadInfo = leadInfo; if
		 * ("Y".equalsIgnoreCase(fuserEntity.getNotificationRequired())) { new Thread(()
		 * -> { EmailRequestModel emailRequestModel = new EmailRequestModel();
		 * emailRequestModel.setEventType(LEAD_UPLOAD);
		 * emailRequestModel.setEmailTo(fuserEntity.getEmail()); Map<String, String>
		 * valueToReplace = new HashMap<>(); valueToReplace.put("username",
		 * fuserEntity.getFirstName()); valueToReplace.put("campaignName",
		 * campaignEntity.getCampaignName()); valueToReplace.put("leadName",
		 * fLeadInfo.getLeadName()); valueToReplace.put("totalCount",
		 * fLeadInfo.getCountOfNumbers().toString());
		 * valueToReplace.put("blacklistCount",
		 * fLeadInfo.getCountOfBlackListNumbers().toString());
		 * valueToReplace.put("duplicateCount",
		 * fLeadInfo.getCountOfDuplicateNumbers().toString());
		 * valueToReplace.put("invalidCount",
		 * fLeadInfo.getCountOfInvalidNumbers().toString()); DateFormat dateFormat = new
		 * SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); valueToReplace.put("datetime",
		 * dateFormat.format(new Date())); emailService.sendEmail(valueToReplace,
		 * emailRequestModel); }).start(); }
		 * 
		 * new Thread(() -> { List<String> toPhoneNumbers = new ArrayList<>(); if
		 * ("Y".equalsIgnoreCase(fuserEntity.getNotificationRequired()) &&
		 * StringUtils.hasLength(fuserEntity.getPhone())) {
		 * toPhoneNumbers.add(fuserEntity.getPhone()); } try { UserEntity admin =
		 * userService.getAdminUser().get(0); if
		 * ("Y".equalsIgnoreCase(admin.getNotificationRequired()) &&
		 * StringUtils.hasLength(admin.getPhone())) {
		 * toPhoneNumbers.add(admin.getPhone()); } } catch (Exception e) {
		 * e.printStackTrace(); } if (!toPhoneNumbers.isEmpty()) { for (String
		 * phoneNumber : toPhoneNumbers) { Map<String, String> valueToReplace = new
		 * HashMap<>(); valueToReplace.put("leadname", fLeadInfo.getLeadName());
		 * valueToReplace.put("username", fuserEntity.getUserName());
		 * valueToReplace.put("campname", campaignEntity.getCampaignName());
		 * valueToReplace.put("vaildMSISDN",
		 * String.valueOf(getTotalValidNumbers(fLeadInfo))); try { //
		 * smsService.sendSms(LEAD_CREATION, valueToReplace, phoneNumber); TODO } catch
		 * (Exception e) { LOGGER.error("Error Sending OTP on Mobile"); } } } else {
		 * LOGGER.
		 * info("No Phone Number Found configured to Send SMS Notification for Lead:" +
		 * fLeadInfo.getLeadName()); }
		 * 
		 * }).start();
		 */

		return leadInfo;
	}

	private String savedLeadInfoDetailsFromFile(int leadId, List<List<String>> dataList, MultipartFile file) {
		LeadInfoDetailEntity leadInfoDetailEntity = null;

		String response = "";
		List<LeadInfoDetailEntity> leadInfoDetailEntityList = new ArrayList<LeadInfoDetailEntity>();
		int rowNumber = 0;
		String jsonArray = "";
		String jsonStart = "{";
		String jsonEnd = "{";
		try {
			for (List<String> row : dataList) {
				if (rowNumber == 0) {
					for (int i = 0; i < row.size(); i++) {
						jsonArray = jsonArray + row.get(i) + ":" + "" + ",";
					}
					jsonArray = jsonStart + jsonArray + jsonEnd;
					row.removeAll(Collections.singleton(""));
					rowNumber++;
				} else {
					leadInfoDetailEntity = new LeadInfoDetailEntity();
					leadInfoDetailEntity.setPhoneNumber(row.get(0).trim());
					leadInfoDetailEntity.setLeadId(leadId);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response = "Got Exception::" + e.getMessage();

		}
		return response;

	}

	@Transactional(rollbackFor = Exception.class)
	public LeadInfoEntity saveLead(LeadInfo leadInfo) throws Exception {

		LOGGER.info("SavedLead: createLead ->" + leadInfo + "->" + leadInfo.getCreatedBy());
		CampaignEntity campaignEntity = campaignRepository.findById(leadInfo.getCampaignId()).orElse(null);

		if (campaignEntity == null) {
			LOGGER.error(
					"No Campaign found with ID " + leadInfo.getCampaignId() + " for Lead " + leadInfo.getLeadName());
			return null;
		}
		if (campaignEntity.getIsSendToRmq() == 1) {
			campaignRepository.updateCampaignIsSendToRmq(campaignEntity.getCampaignId(), 0);
		}
		Optional<Template> tem = tempRepository.findById(campaignEntity.getTemplateId());

		leadInfo.setRcsMsgTypeId(campaignEntity.getRcsMsgTypeId());
		if (tem.isPresent()) {
			leadInfo.setTemplateVideoId(tem.get().getVideoId());
		}
		LeadInfoEntity leadInfoEntity = new LeadInfoEntity();
		leadInfoEntity.setFileTotalRecord(leadInfo.getFileTotalRecord());
		LeadInfoEntity tmpLeadInfo = null;
		UserEntity userEntity = null;
		userEntity = userRepository.getUserEntityByUserId(campaignEntity.getUserId());
		if (leadInfo.getLeadId() != null) {
			partitionCheck(leadInfo.getLeadId());
			tmpLeadInfo = leadInfoRepository.findById(leadInfo.getLeadId()).orElse(null);
			LOGGER.info("Previous Lead status ====> " + tmpLeadInfo.getLeadCompletionStatus());
			if (COMPLETED.equalsIgnoreCase(tmpLeadInfo.getLeadCompletionStatus()) && isLeadToBeRunToday(leadInfo)) {
				LOGGER.info("Marking Lead as Running");
				leadInfo.setLeadCompletionStatus(RUNNING);
			} else {
				if (!isLeadToBeRunToday(leadInfo)) {

					Schedule schedule = leadInfo.getLeadSchedule();
					if ("0".equalsIgnoreCase(schedule.getScheduleDay())) {
						LOGGER.info("Marking Lead as " + "Run Manually");
						leadInfo.setLeadCompletionStatus("Run Manually");
					} else {
						LOGGER.info("Marking Lead as " + "Scheduled Later");
						leadInfo.setLeadCompletionStatus("Scheduled Later");
					}
				} else {
					LOGGER.info("Marking Lead as " + tmpLeadInfo.getLeadCompletionStatus());
					leadInfo.setLeadCompletionStatus(tmpLeadInfo.getLeadCompletionStatus());
				}
			}
			leadInfo.setCountOfNumbers(tmpLeadInfo.getCountOfNumbers() != null ? tmpLeadInfo.getCountOfNumbers() : 0);
			leadInfo.setCountOfDuplicateNumbers(
					tmpLeadInfo.getCountOfDuplicateNumbers() != null ? tmpLeadInfo.getCountOfDuplicateNumbers() : 0);
			leadInfo.setCountOfInvalidNumbers(
					tmpLeadInfo.getCountOfInvalidNumbers() != null ? tmpLeadInfo.getCountOfInvalidNumbers() : 0);
			leadInfo.setCountOfNonRcsNumbers(
					tmpLeadInfo.getCountOfNonRcsNumbers() != null ? tmpLeadInfo.getCountOfNonRcsNumbers() : 0);
			leadInfo.setCountOfBlackListNumbers(
					tmpLeadInfo.getCountOfBlackListNumbers() != null ? tmpLeadInfo.getCountOfBlackListNumbers() : 0);
		} else {
			leadInfo.setCountOfNumbers(0);
			leadInfo.setCountOfDuplicateNumbers(0);
			leadInfo.setCountOfInvalidNumbers(0);
			leadInfo.setCountOfNonRcsNumbers(0);
			leadInfo.setCountOfBlackListNumbers(0);
		}
		leadInfo.setCreatedDate(new Date());
		leadInfo.setCreatedBy(userEntity.getUserName());
		beanUtils.copyProperties(leadInfoEntity, leadInfo);
		Schedule schedule = leadInfo.getLeadSchedule();
		boolean scheduleUpdated = false;
		ScheduleEntity scheduleEntity = new ScheduleEntity();
		if (schedule != null) {

			beanUtils.copyProperties(scheduleEntity, schedule);
			if (schedule.getScheduleId() == null || schedule.getScheduleId() <= 0) {
				scheduleEntity.setCreatedDate(new Date());
			} else {
				scheduleUpdated = true;
			}
			scheduleEntity.setLastModifiedDate(new Date());
			scheduleEntity.setLastModifiedBy(schedule.getLastModifiedBy());
			scheduleEntity.setCreatedBy(schedule.getCreatedBy());
			scheduleEntity = scheduleRepository.save(scheduleEntity);
			leadInfoEntity.setScheduleId(scheduleEntity.getScheduleId());
		}
		/*
		 * RetryInfo retryInfo = leadInfo.getRetryInfo(); if (retryInfo != null) {
		 * RetryInfoEntity retryInfoEntity = new RetryInfoEntity();
		 * beanUtils.copyProperties(retryInfoEntity, retryInfo); retryInfoEntity =
		 * retryRepository.save(retryInfoEntity); retryCount =
		 * retryInfoEntity.getNoOfRetry();
		 * leadInfoEntity.setRetryId(retryInfoEntity.getRetryId()); }
		 */
		leadInfoEntity.setCreatedDate(new Date());
		leadInfoEntity.setLastModifiedDate(new Date());
		leadInfoEntity.setLastModifiedBy(leadInfo.getLastModifiedBy());
		leadInfoEntity.setCreatedBy(userEntity.getUserName());
		if (leadInfo.getLeadId() == null) {
			leadInfoEntity.setLeadCompletionStatus(CREATED);
		}

		leadInfoEntity = leadInfoRepository.save(leadInfoEntity);

		List<LeadInfoDetail> leadInfoDetails = leadInfo.getLeadInfoDetails();
		List<LeadInfoDetailEntity> leadInfoDetailEntityList = new ArrayList<>();
		Date now = new Date();
		if (leadInfoDetails != null && !leadInfoDetails.isEmpty()) {
			for (LeadInfoDetail leadInfoDetail : leadInfoDetails) {
				LeadInfoDetailEntity leadInfoDetailEntity = new LeadInfoDetailEntity();
				leadInfoDetail.setLeadId(leadInfoEntity.getLeadId());
				beanUtils.copyProperties(leadInfoDetailEntity, leadInfoDetail);
				leadInfoDetailEntity.setAttemptNum(0);
				leadInfoDetailEntity.setStatus(CREATED);
				leadInfoDetailEntity.setNextCallDtm(now);
				if (!validPhoneNumber(leadInfoDetail.getPhoneNumber())) {
					leadInfoDetailEntity.setPhoneNumberStatus(INVALID_NUMBER);
					int countOfInvalidNumbers = leadInfo.getCountOfInvalidNumbers() + 1;
					leadInfoEntity.setCountOfInvalidNumbers(countOfInvalidNumbers);
					int countOfNumbers = leadInfo.getCountOfNumbers() + 1;
					leadInfoEntity.setCountOfNumbers(countOfNumbers);
				} else if (isBlackListed(leadInfoDetail.getPhoneNumber(), leadInfo.getUserId(),
						userEntity.getParentUserId())) {
					leadInfoDetailEntity.setPhoneNumberStatus(BLACKLIST_NUMBER);
					int countOfBlackListNumbers = leadInfo.getCountOfBlackListNumbers() + 1;
					leadInfoEntity.setCountOfBlackListNumbers(countOfBlackListNumbers);
					int countOfNumbers = leadInfo.getCountOfNumbers() + 1;
					leadInfoEntity.setCountOfNumbers(countOfNumbers);
				} else {
					leadInfoDetailEntity.setPhoneNumberStatus(OK);
				}

				leadInfoDetailEntityList.add(leadInfoDetailEntity);

			}
		}
		leadInfoEntity = leadInfoRepository.save(leadInfoEntity);
		LOGGER.info("SaveLead: leadInfoEntity" + leadInfoEntity);
		if (leadInfoDetailEntityList != null && !leadInfoDetailEntityList.isEmpty()) {
			leadInfoDetailRepository.saveAll(leadInfoDetailEntityList);
		}

		/*
		 * List<AttemptInfoEntity> attemptInfoEntities =
		 * attemptRepository.findByRetryId(leadInfoEntity.getRetryId()); if
		 * (CollectionUtils.isEmpty(attemptInfoEntities)) { for (int i =
		 * attemptInfoEntities.size() + 1; i <= retryCount + 1; i++) { AttemptInfoEntity
		 * attemptInfoEntity = new AttemptInfoEntity();
		 * attemptInfoEntity.setStatus("Pending"); if (i == 1) {
		 * attemptInfoEntity.setQualifiedCount(getTotalValidNumbers(leadInfoEntity)); }
		 * else { attemptInfoEntity.setQualifiedCount(0); }
		 * attemptInfoEntity.setProcessedNumCount(0); attemptInfoEntity.setRound(i);
		 * attemptInfoEntity.setRetryId(leadInfoEntity.getRetryId());
		 * LOGGER.info("Added Attempt #" + i + " in lead:" + leadInfo.getLeadId());
		 * attemptInfoEntities.add(attemptInfoEntity); }
		 * attemptRepository.saveAll(attemptInfoEntities); }
		 */
		LOGGER.info("Lead [ " + leadInfo.getLeadName() + " ] Created Successfully [" + leadInfoEntity.toString() + "]");
		if (tmpLeadInfo == null) {
			Notifications.getsInstance().notifyEventCaptured(LEAD_CREATED, leadInfoEntity);
		}

		return leadInfoEntity;
	}

	private boolean isBlackListed(String phoneNumber, long userId, long parentUserId) {
		boolean isBlackList = false;
		LOGGER.info("Searching Number [" + phoneNumber + "] in BlackList DB for User ID: " + userId);
		// String prefix = phoneNumber.substring(0, 3);
		String phNumber = blackListRepository.isBlackList(phoneNumber, userId);// ,
																				// parentUserId);
		if (StringUtils.hasLength(phNumber)) {
			LOGGER.info("Phone Number[" + phoneNumber + "] is BlackListed");
			isBlackList = true;
		} else {
			LOGGER.info("phoneNumber:" + phoneNumber + " is not black List");
		}
		return isBlackList;
	}

	@Transactional(rollbackFor = Exception.class)
	public LeadInfoEntity saveLeadWithDnDCheck(LeadInfo leadInfo, boolean isDnd) throws Exception {
		boolean attemptUpdated = false;
		boolean isUpdate = false;
		boolean isFonada = false;
		boolean scheduleUpdated = false;
		boolean retryUpdated = false;
		UserEntity userEntity = null;
		RetryInfoEntity retryEntityFromDB = null;
		RetryInfoEntity retryEntityClone = new RetryInfoEntity();
		RetryInfoEntity retryInfoEntity = null;
		boolean isFailedRetryUpdated = false;
		boolean isBusyRetryUpdated = false;
		boolean isNoAnsRetryUpdated = false;
		LOGGER.info("SavedLead: createLead ->" + leadInfo + "->" + leadInfo.getCreatedBy());
		CampaignEntity campaignEntity = campaignRepository.findById(leadInfo.getCampaignId()).orElse(null);
		if (campaignEntity == null) {
			LOGGER.error(
					"No Campaign found with ID " + leadInfo.getCampaignId() + " for Lead " + leadInfo.getLeadName());
			return null;
		}
		if (campaignEntity.getIsSendToRmq() == 1) {
			campaignRepository.updateCampaignIsSendToRmq(campaignEntity.getCampaignId(), 0);
		}
		Optional<Template> tem = tempRepository.findById(campaignEntity.getTemplateId());
		if (tem.isPresent()) {
			leadInfo.setTemplateVideoId(tem.get().getVideoId());
		}
		if (Objects.nonNull(campaignEntity.getRcsMsgTypeId())) {
			leadInfo.setRcsMsgTypeId(campaignEntity.getRcsMsgTypeId());
		}
		userEntity = userRepository.getUserEntityByUserId(campaignEntity.getUserId());

		LeadInfoEntity leadInfoEntity = new LeadInfoEntity();
		if (leadInfo.getLeadId() != null) {
			// By Rahul Kumar
			partitionCheck(leadInfo.getLeadId());
			isUpdate = true;
			LeadInfoEntity tmpLeadInfo = leadInfoRepository.findById(leadInfo.getLeadId()).orElse(null);
			if (tmpLeadInfo.getLeadName().toLowerCase().contains("fonada")) {
				isFonada = true;
			}
			leadInfo.setCountOfNumbers(tmpLeadInfo.getCountOfNumbers());
			leadInfo.setCountOfBlackListNumbers(tmpLeadInfo.getCountOfBlackListNumbers());
			leadInfo.setCountOfDuplicateNumbers(tmpLeadInfo.getCountOfDuplicateNumbers());
			leadInfo.setCountOfInvalidNumbers(tmpLeadInfo.getCountOfInvalidNumbers());
			leadInfo.setCountOfNonRcsNumbers(tmpLeadInfo.getCountOfNonRcsNumbers());
			leadInfo.setCreatedBy(userEntity.getUserName());
			leadInfo.setCreatedDate(new Date());
			leadInfo.setLastModifiedBy(userEntity.getUserName());
			leadInfo.setLastModifiedDate(new Date());
			if (!StringUtils.hasLength(leadInfo.getLeadName())) {
				leadInfo.setLeadName(tmpLeadInfo.getLeadName());
			}
			leadInfo.setInsertDtm(tmpLeadInfo.getInsertDtm());
			if (!StringUtils.hasLength(leadInfo.getLeadAction())) {
				leadInfo.setLeadAction(tmpLeadInfo.getLeadAction());
			}

			if (!StringUtils.hasLength(leadInfo.getLeadPriorityType())) {
				leadInfo.setLeadPriorityType(tmpLeadInfo.getLeadPriorityType());
			}

			if (null == leadInfo.getLeadPriority()) {
				leadInfo.setLeadPriority(tmpLeadInfo.getLeadPriority());
			}

			leadInfo.setInsertDtm(tmpLeadInfo.getInsertDtm());
			leadInfo.setProcessDtm(tmpLeadInfo.getProcessDtm());
			LOGGER.info("Previous status ====> " + tmpLeadInfo.getLeadCompletionStatus());
			if (tmpLeadInfo.getLeadCompletionStatus().equalsIgnoreCase(COMPLETED) && isLeadToBeRunToday(leadInfo)) {
				LOGGER.info("Marking lead as Running");
				leadInfo.setLeadCompletionStatus(RUNNING);
			} else {
				LOGGER.info("Marking lead as " + tmpLeadInfo.getLeadCompletionStatus());
				leadInfo.setLeadCompletionStatus(tmpLeadInfo.getLeadCompletionStatus());
			}
		} else {
			leadInfo.setCountOfNumbers(0);
			leadInfo.setCountOfBlackListNumbers(0);
			leadInfo.setCountOfDuplicateNumbers(0);
			leadInfo.setCountOfInvalidNumbers(0);
			leadInfo.setCountOfNonRcsNumbers(0);
		}
		beanUtils.copyProperties(leadInfoEntity, leadInfo);
		Schedule schedule = leadInfo.getLeadSchedule();
		ScheduleEntity scheduleEntity = new ScheduleEntity();
		if (!isFonada && schedule != null) {
			beanUtils.copyProperties(scheduleEntity, schedule);
			if (schedule.getScheduleId() == null || schedule.getScheduleId() <= 0) {
				scheduleEntity.setCreatedDate(new Date());
				scheduleEntity.setCreatedBy(leadInfo.getCreatedBy());
			} else {
				scheduleUpdated = true;
			}
			scheduleEntity.setLastModifiedDate(new Date());
			scheduleEntity.setLastModifiedBy(leadInfo.getLastModifiedBy());
			scheduleEntity.setCreatedBy(schedule.getCreatedBy());
			scheduleEntity = scheduleRepository.save(scheduleEntity);
			leadInfoEntity.setScheduleId(scheduleEntity.getScheduleId());
			if (isLeadToBeRunToday(leadInfoEntity)) {
				leadInfoEntity.setLeadCompletionStatus(RUNNING);
			} else {
				leadInfoEntity.setLeadCompletionStatus(SCHEDULED_LATER);
			}
		} else if (isFonada && schedule != null) {
			leadInfoEntity.setScheduleId(schedule.getScheduleId());
		}
		/*
		 * RetryInfo retryInfo = leadInfo.getRetryInfo(); if (!isFonada && retryInfo !=
		 * null) { retryInfoEntity = new RetryInfoEntity();
		 * beanUtils.copyProperties(retryInfoEntity, retryInfo); if
		 * (retryInfo.getRetryId() == null || retryInfo.getRetryId() <= 0) { // no -op }
		 * else { retryEntityFromDB =
		 * retryRepository.getRetryInfoEntityByRetryId(retryInfo.getRetryId());
		 * beanUtils.copyProperties(retryEntityClone, retryEntityFromDB); if
		 * (retryEntityFromDB.getNoOfRetry().intValue() <
		 * retryInfo.getNoOfRetry().intValue()) { retryUpdated = true; } }
		 * retryInfoEntity = retryRepository.save(retryInfoEntity);
		 * LOGGER.info("Updated Retry is :" + retryInfoEntity);
		 * leadInfoEntity.setRetryId(retryInfoEntity.getRetryId()); } else if (isFonada
		 * && retryInfo != null) { retryInfoEntity =
		 * retryRepository.getRetryInfoEntityByRetryId(retryInfo.getRetryId());
		 * leadInfoEntity.setRetryId(retryInfo.getRetryId()); }
		 */
		if (!isUpdate) {
			leadInfoEntity.setCreatedDate(new Date());
			leadInfoEntity.setCreatedBy(userEntity.getUserName());
			leadInfoEntity.setLastModifiedBy(userEntity.getUserName());
			leadInfoEntity.setLeadCompletionStatus(CREATED);
		}
		leadInfoEntity.setLastModifiedDate(new Date());
		leadInfoEntity.setLastModifiedBy(userEntity.getUserName());
		leadInfoEntity.setCreatedBy(userEntity.getUserName());
		leadInfoEntity.setLeadStatus(INACTIVE);
		leadInfoEntity = leadInfoRepository.save(leadInfoEntity);

		if (leadInfo != null) {
			leadInfo.setLeadId(leadInfoEntity.getLeadId());
			List<LeadInfoDetail> leadInfoDetails = leadInfo.getLeadInfoDetails();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (LeadInfoDetail leadInfoDetail : leadInfoDetails) {
				StringBuilder sb = new StringBuilder();
				sb.append(leadInfoDetail.toRecord()).append("\n");
				baos.write(sb.toString().getBytes());
			}
			byte[] bytes = baos.toByteArray();
			InputStream in = new ByteArrayInputStream(bytes);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

			int threadNo = 10;
			Thread t1;
			List<Connection> connectionList = new ArrayList<>();
			List<Thread> fileReadThreadList = new ArrayList<>();
			TokenPojo pojo = null;

			/**
			 * 
			 */
			OperatorApiConfigEntity operatorApiConfigEntity = null;
			UserBotMappingEntity userBotMappingEntity = null;
			if (Objects.nonNull(tem.get().getUserBotMappingId() != null)) {
				userBotMappingEntity = userBotMappingRepository.findById(tem.get().getUserBotMappingId());

				operatorApiConfigEntity = operatorApiConfigRepository.findByApiTypeAndTemplateTypeAndOperatorId(
						"TOKEN",
						operatorRepository.findByOperatorName(userBotMappingEntity.getOperator()).getOperatorId());

				if (Objects.nonNull(operatorApiConfigEntity)) {
					pojo = new Gson()
							.fromJson(botTokenAPIService.getTokenFromClientAPI(operatorApiConfigEntity.getApiUrl(),
									userBotMappingEntity.getBasicToken()), TokenPojo.class);
				}
			}
			partitionCheck(leadInfo.getLeadId());
			try {
				for (int i = 0; i < threadNo; i++) {
					LOGGER.info("upload leads via json array starting thread->" + i);
					Connection con = null;

					t1 = new Thread(new JSONReadThread(leadInfo, leadInfoDetailRepository, bufferedReader, false,
							blackListRepository, userEntity, jdbcTemplate, false, 0l, botTokenAPIService, dndRepository,
							pojo, rcsEnabled, rabbitMQSender), "Thread " + i);
					fileReadThreadList.add(t1);
					t1.start();
				}
				for (Thread t : fileReadThreadList) {
					t.join();
				}
			} catch (Exception r) {
				throw r;
			}

		}

		leadInfoEntity.setLeadStatus(ACTIVE);
		leadInfoEntity = leadInfoRepository.save(leadInfoEntity);
		LOGGER.info("Filling Counters");
		fillCounters(leadInfo);
		LOGGER.info("Filled Counters");
		leadInfoEntity.setCountOfNumbers(leadInfo.getCountOfNumbers());
		leadInfoEntity.setCountOfNonRcsNumbers(leadInfo.getCountOfNonRcsNumbers());
		leadInfoEntity.setCountOfDuplicateNumbers(leadInfo.getCountOfDuplicateNumbers());
		leadInfoEntity.setCountOfInvalidNumbers(leadInfo.getCountOfInvalidNumbers());
		leadInfoEntity.setCountOfBlackListNumbers(leadInfo.getCountOfBlackListNumbers());
		leadInfoRepository.save(leadInfoEntity);

		/*
		 * int incInRetryNoForFail = 0; int incInRetryNoForBusy = 0; int
		 * incInRetryNoForNoAns = 0; int retryCount = 0; List<AttemptInfoEntity>
		 * savedAttemptEntities = null; if (retryUpdated ||
		 * (!"C".equalsIgnoreCase(retryInfoEntity.getRetryType()) &&
		 * retryEntityClone.getNoOfRetry() != null)) { // If Retry is updated then mark
		 * Undialed CDRs if any to TBRedialed.
		 * 
		 * LOGGER.info("Retry Updated.."); if (retryEntityClone.getRetryOnFail() ==
		 * null) { retryEntityClone.setRetryOnFail(0); } if
		 * (retryEntityClone.getRetryOnBusy() == null) {
		 * retryEntityClone.setRetryOnBusy(0); } if (retryEntityClone.getRetryOnNoAns()
		 * == null) { retryEntityClone.setRetryOnNoAns(0); } if
		 * (retryInfoEntity.getRetryOnFail() != null &&
		 * retryInfoEntity.getRetryOnFail().intValue() > 0 &&
		 * retryEntityClone.getRetryOnFail().intValue() <
		 * retryInfoEntity.getRetryOnFail().intValue()) {
		 * LOGGER.info("Marking Failed Cdrs As TBRedialed for LeadId:" +
		 * leadInfoEntity.getLeadId()); //
		 * updateCdrsAsTBRedialedWithStatus(leadInfoEntity.getLeadId(),"FAILED");
		 * isFailedRetryUpdated = true; incInRetryNoForFail =
		 * retryInfoEntity.getRetryOnFail().intValue() -
		 * retryEntityClone.getRetryOnFail().intValue(); } else {
		 * LOGGER.info("Skip markFailedCdrsAsTBRedialed for LeadId:" +
		 * leadInfoEntity.getLeadId() + " retryInfoEntity" + retryInfoEntity +
		 * "retryEntityFromDB" + retryEntityClone); }
		 * 
		 * if (retryInfoEntity.getRetryOnBusy() != null &&
		 * retryInfoEntity.getRetryOnBusy().intValue() > 0 &&
		 * retryEntityClone.getRetryOnBusy().intValue() <
		 * retryInfoEntity.getRetryOnBusy().intValue()) {
		 * LOGGER.info("Marking Busy Cdrs As TBRedialed for LeadId:" +
		 * leadInfoEntity.getLeadId()); //
		 * updateCdrsAsTBRedialedWithStatus(leadInfoEntity.getLeadId(),"BUSY");
		 * isBusyRetryUpdated = true; incInRetryNoForBusy =
		 * retryInfoEntity.getRetryOnBusy().intValue() -
		 * retryEntityClone.getRetryOnBusy().intValue();
		 * 
		 * } else { LOGGER.info("Skip markBusyCdrsAsTBRedialed for LeadId:" +
		 * leadInfoEntity.getLeadId() + " retryInfoEntity" + retryInfoEntity +
		 * "retryEntityFromDB" + retryEntityClone);
		 * 
		 * } if (retryInfoEntity.getRetryOnNoAns() != null &&
		 * retryInfoEntity.getRetryOnNoAns().intValue() > 0 &&
		 * retryEntityClone.getRetryOnNoAns().intValue() <
		 * retryInfoEntity.getRetryOnNoAns().intValue()) {
		 * LOGGER.info("Marking NoAnswered CdrsAs TBRedialed for LeadId:" +
		 * leadInfoEntity.getLeadId()); //
		 * updateCdrsAsTBRedialedWithStatus(leadInfoEntity.getLeadId(),"NO ANSWER");
		 * isNoAnsRetryUpdated = true; incInRetryNoForNoAns =
		 * retryInfoEntity.getRetryOnNoAns().intValue() -
		 * retryEntityClone.getRetryOnNoAns().intValue();
		 * 
		 * } else { LOGGER.info("Skip markNoAnsweredCdrsAsTBRedialed for LeadId:" +
		 * leadInfoEntity.getLeadId() + " retryInfoEntity" + retryInfoEntity +
		 * "retryEntityFromDB" + retryEntityClone); }
		 */
		/**
		 * Case #1 Initial values: FAILED = 4, NO ANS = 0, BUSY = 0 Updated values:
		 * FAILED = 4, NO ANS = 3, BUSY = 2
		 *
		 * Modified Retry Count = should be Old Retry Count (4) + attemptCounts-1(for
		 * first attempt as it was not retry but normal) - Old Retry Count + max(all
		 * three = ) 3 = 8 Updated Attempt Of Particular Type # = Change > 0 ? Mod Retry
		 * Count - Inc in Self Retry Count +1: Retry Count (8) FAILED = 8 NO ANS = 8 -
		 * new count + 1 = 8 - 3 +1 = 6 BUSY = 8 - 2 + 1 = 7
		 */
		/*
		 * savedAttemptEntities =
		 * attemptRepository.findByRetryId(leadInfoEntity.getRetryId());
		 * 
		 * if (retryInfoEntity != null) { retryCount = retryEntityClone.getNoOfRetry() +
		 * savedAttemptEntities.size() - retryEntityClone.getNoOfRetry().intValue() - 1
		 * + maxOf(incInRetryNoForBusy, incInRetryNoForFail, incInRetryNoForNoAns);
		 * 
		 * int updatedCount = 0; if (incInRetryNoForBusy > 0) { incInRetryNoForBusy =
		 * retryCount - incInRetryNoForBusy + 1; } else { incInRetryNoForBusy =
		 * retryCount; } if (incInRetryNoForFail > 0) { incInRetryNoForFail = retryCount
		 * - incInRetryNoForFail + 1; } else { incInRetryNoForFail = retryCount; }
		 * 
		 * if (incInRetryNoForNoAns > 0) { incInRetryNoForNoAns = retryCount -
		 * incInRetryNoForNoAns + 1; } else { incInRetryNoForNoAns = retryCount; }
		 * LOGGER.info("Updated Retry Count is :" + retryCount);
		 * 
		 * List<AttemptInfoEntity> attemptInfoEntities = new ArrayList<>(); // from
		 * above e.g. this will run from 6 to 8 inserting 3 entries if
		 * ("C".equalsIgnoreCase(retryInfoEntity.getRetryType())) { for (int i =
		 * savedAttemptEntities.size() + 1; i <= retryCount + 1; i++) {
		 * AttemptInfoEntity attemptInfoEntity = new AttemptInfoEntity();
		 * attemptInfoEntity.setStatus("Pending"); if (i == 1) {
		 * attemptInfoEntity.setQualifiedCount(getTotalValidNumbers(leadInfoEntity)); }
		 * else { attemptInfoEntity.setQualifiedCount(0); }
		 * attemptInfoEntity.setProcessedNumCount(0); attemptInfoEntity.setRound(i);
		 * attemptInfoEntity.setRetryId(leadInfoEntity.getRetryId());
		 * LOGGER.info("Added Attempt #" + i + " in lead:" + leadInfo.getLeadId());
		 * attemptInfoEntities.add(attemptInfoEntity); attemptUpdated = true;
		 * 
		 * } if (attemptUpdated) { attemptRepository.saveAll(attemptInfoEntities);
		 * LOGGER.info("Saved Attempts [" + attemptInfoEntities.size() + "] for [" +
		 * leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName() + "]"); }
		 * else { LOGGER.info( "No update required in attempts as it is of Type:" +
		 * retryInfoEntity.getRetryType()); }
		 * 
		 * } else { LOGGER.info("No update required in attempts as it is of Type:" +
		 * retryInfoEntity.getRetryType()); } } else {
		 * LOGGER.error("Retry not found for " + leadInfoEntity.getLeadId() + "=>" +
		 * leadInfoEntity.getLeadName()); }
		 * 
		 * } if (isUpdate && !attemptUpdated) { // This means a test call is initiated,
		 * we need to reset this here // else for second call, before cdr comes, the
		 * round is considered completed and // causing issue is summary
		 * AttemptInfoEntity attemptInfoEntity =
		 * attemptRepository.getRoundOneEntity(leadInfoEntity.getRetryId()); if
		 * (attemptInfoEntity != null) { attemptInfoEntity.setStatus("Pending");
		 * attemptRepository.save(attemptInfoEntity);
		 * LOGGER.info("Resetted Attempt to Pending Status"); } }
		 */
		// If any one of these numbers are updated, then we have to increase the attempt
		// number of the ones that didnt got the update
		// Else if in next turn someone updates them, they will not be picked up due to
		// old attempt number

		/*
		 * if ((isFailedRetryUpdated || isBusyRetryUpdated || isNoAnsRetryUpdated)) {
		 * 
		 * if (isFailedRetryUpdated) { if
		 * (!"C".equalsIgnoreCase(retryInfoEntity.getRetryType())) {
		 * leadInfoDetailRepository.updateCdrsAsTBRedialedWithStatus(leadInfoEntity.
		 * getLeadId(), "FAILED"); } else {
		 * leadInfoDetailRepository.updateAttemptNumberForStatus(leadInfoEntity.
		 * getLeadId(), "FAILED", incInRetryNoForFail);
		 * retryInfoEntity.setRetryOnFail(retryCount); }
		 * LOGGER.info("Updated Attempt Number for Failed CDRs for leadId " +
		 * leadInfoEntity.getLeadId() + " with value:" + incInRetryNoForFail); }
		 * 
		 * if (isBusyRetryUpdated) { if
		 * (!"C".equalsIgnoreCase(retryInfoEntity.getRetryType())) {
		 * leadInfoDetailRepository.updateCdrsAsTBRedialedWithStatus(leadInfoEntity.
		 * getLeadId(), "BUSY"); } else {
		 * leadInfoDetailRepository.updateAttemptNumberForStatus(leadInfoEntity.
		 * getLeadId(), "BUSY", incInRetryNoForBusy);
		 * retryInfoEntity.setRetryOnBusy(retryCount); }
		 * LOGGER.info("Updated Attempt Number for Busy CDRs for leadId " +
		 * leadInfoEntity.getLeadId() + " with value:" + incInRetryNoForBusy); }
		 * 
		 * if (isNoAnsRetryUpdated) { if
		 * (!"C".equalsIgnoreCase(retryInfoEntity.getRetryType())) {
		 * leadInfoDetailRepository.updateCdrsAsTBRedialedWithStatus(leadInfoEntity.
		 * getLeadId(), "NO ANSWER"); } else {
		 * leadInfoDetailRepository.updateAttemptNumberForStatus(leadInfoEntity.
		 * getLeadId(), "NO ANSWER", incInRetryNoForNoAns);
		 * retryInfoEntity.setRetryOnNoAns(retryCount); }
		 * LOGGER.info("Updated Attempt Number for No Answer CDRs for leadId " +
		 * leadInfoEntity.getLeadId() + " with value:" + incInRetryNoForNoAns); }
		 * 
		 * } if (retryCount > 0 && "C".equalsIgnoreCase(retryInfoEntity.getRetryType()))
		 * { retryInfoEntity.setNoOfRetry(retryCount); retryInfoEntity =
		 * retryRepository.save(retryInfoEntity); LOGGER.info("Updated Retry Info:" +
		 * retryInfoEntity); } LOGGER.info("Lead [ " + leadInfo.getLeadName() +
		 * " ] Created Successfully [" + leadInfoEntity.toString() + "]"); if
		 * (leadInfoEntity.getCountOfNumbers() != 0) {
		 * LOGGER.info("Notifying about Lead Update");
		 * Notifications.getsInstance().notifyEventCaptured(LEAD_CREATED,
		 * leadInfoEntity); } else { LOGGER.info("Skipped Notifying about Lead Update" +
		 * leadInfoEntity.getCountOfNumbers()); }
		 * 
		 * UserEntity fUserEntity = userEntity; LeadInfoEntity fleadInfoEntity =
		 * leadInfoEntity; if
		 * ("Y".equalsIgnoreCase(userEntity.getNotificationRequired())) { new Thread(()
		 * -> { EmailRequestModel emailRequestModel = new EmailRequestModel();
		 * emailRequestModel.setEventType(LEAD_UPLOAD);
		 * emailRequestModel.setEmailTo(fUserEntity.getEmail()); Map<String, String>
		 * valueToReplace = new HashMap<>(); valueToReplace.put("username",
		 * fUserEntity.getFirstName()); valueToReplace.put("campaignName",
		 * campaignEntity.getCampaignName()); valueToReplace.put("leadName",
		 * fleadInfoEntity.getLeadName()); valueToReplace.put("totalCount",
		 * fleadInfoEntity.getCountOfNumbers().toString());
		 * valueToReplace.put("blacklistCount",
		 * fleadInfoEntity.getCountOfBlackListNumbers().toString());
		 * valueToReplace.put("duplicateCount",
		 * fleadInfoEntity.getCountOfDuplicateNumbers().toString());
		 * valueToReplace.put("invalidCount",
		 * fleadInfoEntity.getCountOfInvalidNumbers().toString()); DateFormat dateFormat
		 * = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); valueToReplace.put("datetime",
		 * dateFormat.format(new Date())); emailService.sendEmail(valueToReplace,
		 * emailRequestModel); }).start(); } new Thread(() -> { List<String>
		 * toPhoneNumbers = new ArrayList<>(); if
		 * ("Y".equalsIgnoreCase(fUserEntity.getNotificationRequired()) &&
		 * StringUtils.hasLength(fUserEntity.getPhone())) {
		 * toPhoneNumbers.add(fUserEntity.getPhone()); } try { UserEntity admin =
		 * userService.getAdminUser().get(0); if
		 * ("Y".equalsIgnoreCase(admin.getNotificationRequired()) &&
		 * StringUtils.hasLength(admin.getPhone())) {
		 * toPhoneNumbers.add(admin.getPhone()); } } catch (Exception e) {
		 * e.printStackTrace(); } if (!toPhoneNumbers.isEmpty()) { for (String
		 * phoneNumber : toPhoneNumbers) { Map<String, String> valueToReplace = new
		 * HashMap<>(); valueToReplace.put("leadname", fleadInfoEntity.getLeadName());
		 * valueToReplace.put("username", fUserEntity.getUserName());
		 * valueToReplace.put("campname", campaignEntity.getCampaignName());
		 * valueToReplace.put("vaildMSISDN",
		 * String.valueOf(getTotalValidNumbers(fleadInfoEntity))); try { //
		 * smsService.sendSms(LEAD_CREATION, valueToReplace, phoneNumber); TODO } catch
		 * (Exception e) { LOGGER.error("Error Sending OTP on Mobile"); } } } else {
		 * LOGGER.
		 * info("No Phone Number Found configured to Send SMS Notification for Lead:" +
		 * fleadInfoEntity.getLeadName()); }
		 * 
		 * }).start();
		 */

		return leadInfoEntity;
	}

	private int maxOf(int incInRetryNoForBusy, int incInRetryNoForFail, int incInRetryNoForNoAns) {
		return Math.max(incInRetryNoForBusy, Math.max(incInRetryNoForFail, incInRetryNoForNoAns));
	}

	public int getTotalValidNumbers(LeadInfo returnedLead) {

		return returnedLead.getCountOfNumbers()
				- (returnedLead.getCountOfNonRcsNumbers() != null ? returnedLead.getCountOfNonRcsNumbers().intValue()
						: 0)
				- (returnedLead.getCountOfDuplicateNumbers() != null
						? returnedLead.getCountOfDuplicateNumbers().intValue()
						: 0)
				- (returnedLead.getCountOfInvalidNumbers() != null ? returnedLead.getCountOfInvalidNumbers().intValue()
						: 0)
				- (returnedLead.getCountOfBlackListNumbers() != null
						? returnedLead.getCountOfBlackListNumbers().intValue()
						: 0);

	}

	private int getTotalValidNumbers(LeadInfoEntity leadInfoEntity) {
		return leadInfoEntity.getCountOfNumbers()
				- (leadInfoEntity.getCountOfNonRcsNumbers() != null
						? leadInfoEntity.getCountOfNonRcsNumbers().intValue()
						: 0)
				- (leadInfoEntity.getCountOfDuplicateNumbers() != null
						? leadInfoEntity.getCountOfDuplicateNumbers().intValue()
						: 0)
				- (leadInfoEntity.getCountOfInvalidNumbers() != null
						? leadInfoEntity.getCountOfInvalidNumbers().intValue()
						: 0)
				- (leadInfoEntity.getCountOfBlackListNumbers() != null
						? leadInfoEntity.getCountOfBlackListNumbers().intValue()
						: 0);

	}

	private boolean validPhoneNumber(String phoneNumber) {
		boolean isValid = true;
		if (phoneNumber == null) {
			return false;
		}
//        if (phoneNumber.startsWith("+")) {
//            return true;
//        }
//        if (phoneNumber.length() != 10 ) {
//            return false;
//        }
		try {
			long l = Long.parseLong(phoneNumber);
			if (l < 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return isValid;

	}

	public boolean isLeadToBeRunToday(LeadInfo leadInfo) {
		boolean ret = false;
		long currentTime = System.currentTimeMillis() + (1 * 60 * 1000);
		if (leadInfo.getLeadSchedule() == null) {
			LOGGER.error("Schedule not found for Lead " + leadInfo.getLeadName());
		}
		ScheduleEntity schedule = scheduleRepository.getByScheduleId(leadInfo.getLeadSchedule().getScheduleId());
		LOGGER.info("isLeadToBeRunToday - schedule:" + schedule);
		if (schedule != null) {
			if (schedule.getScheduleStartDtm().getTime() <= currentTime
					&& schedule.getScheduleEndDtm().getTime() >= currentTime) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentTime);
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);// sunday=1,monday=2
				String days = schedule.getScheduleDay();
				if (days != null && days.contains(String.valueOf(dayOfWeek))) {
					if (StringUtils.hasLength(schedule.getWindowRequired())
							&& "Y".equalsIgnoreCase(schedule.getWindowRequired())) {
						Date now = new Date();
						Date windowStartTime;
						Date windowEndTime;
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String windowStartTimeStr = df.format(now) + " " + schedule.getWindowStartTime() + ":00";
						String windowEndTimeStr = df.format(now) + " " + schedule.getWindowEndTime() + ":00";
						try {
							windowStartTime = sdf.parse(windowStartTimeStr);
							windowEndTime = sdf.parse(windowEndTimeStr);
							if (now.getTime() >= windowStartTime.getTime()
									&& now.getTime() <= windowEndTime.getTime()) {
								ret = true;
							} else {
								LOGGER.info("Window Expired for Lead:" + leadInfo.getLeadId() + "=>"
										+ leadInfo.getLeadName() + "  Windows:" + windowEndTimeStr + "-"
										+ windowEndTimeStr);
								ret = false;
							}
						} catch (Exception e) {
							LOGGER.error("Scheduled Service: Got Exception", e);
							ret = true;
						}
					} else {
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	public boolean isLeadToBeRunToday(LeadInfoEntity leadInfo) {
		boolean ret = false;
		long currentTime = System.currentTimeMillis() + (1 * 60 * 1000);
		ScheduleEntity scheduleEntity = scheduleRepository.getByScheduleId(leadInfo.getScheduleId());
		if (scheduleEntity == null) {
			LOGGER.error("Schedule not found for Lead " + leadInfo.getLeadName());
		}
		Schedule schedule = new Schedule();
		try {
			beanUtils.copyProperties(schedule, scheduleEntity);
		} catch (Exception e) {
			LOGGER.info("Exception: ", e);
		}
		if (schedule != null && schedule.getScheduleStartDtm() != null) {
			if (schedule.getScheduleStartDtm().getTime() <= currentTime
					&& schedule.getScheduleEndDtm().getTime() >= currentTime) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentTime);
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);// sunday=1,monday=2
				String days = schedule.getScheduleDay();
				if (days != null && days.contains(String.valueOf(dayOfWeek))) {
					if (StringUtils.hasLength(scheduleEntity.getWindowRequired())
							&& "Y".equalsIgnoreCase(scheduleEntity.getWindowRequired())) {
						Date now = new Date();
						Date windowStartTime;
						Date windowEndTime;
						DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String windowStartTimeStr = df.format(now) + " " + scheduleEntity.getWindowStartTime() + ":00";
						String windowEndTimeStr = df.format(now) + " " + scheduleEntity.getWindowEndTime() + ":00";
						try {
							windowStartTime = sdf.parse(windowStartTimeStr);
							windowEndTime = sdf.parse(windowEndTimeStr);
							if (now.getTime() >= windowStartTime.getTime()
									&& now.getTime() <= windowEndTime.getTime()) {
								ret = true;
							} else {
								LOGGER.info("Window Expired for Lead:" + leadInfo.getLeadId() + "=>"
										+ leadInfo.getLeadName() + "  Windows:" + windowEndTimeStr + "-"
										+ windowEndTimeStr);
								ret = false;
							}
						} catch (Exception e) {
							LOGGER.error("Scheduled Service: Got Exception", e);
							ret = true;
						}
					} else {
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	private void partitionCheck(Long leadId) {
		try {
			// Partition Check
			leadInfoDetailRepository.getPhoneNumbersBasedOnNotCompleteStatus(leadId, 1);
		} catch (JpaSystemException e) {
			LOGGER.error("Partition does not exists for Lead:" + leadId + " Removing this lead from processing");
			leadInfoRepository.updateLeadCompletionStatus(leadId, COMPLETED);
			throw new RuntimeException("Some Problem with this lead, pls choose some other lead !!");
		}
	}

	public void fillCounters(LeadInfo leadInfo) {

		List<Object> stats = null;
		try {
			stats = leadInfoDetailRepository.getLeadStats(leadInfo.getLeadId());
		} catch (Exception e) {

		}
		// Inititalize with 0
		leadInfo.setCountOfDuplicateNumbers(0);
		leadInfo.setCountOfNumbers(0);
		leadInfo.setCountOfNonRcsNumbers(0);
		leadInfo.setCountOfBlackListNumbers(0);
		leadInfo.setCountOfInvalidNumbers(0);

		if (!CollectionUtils.isEmpty(stats)) {
			for (Object object : stats) {
				String statType = (String) ((Object[]) object)[0];
				if (null == statType) {
					LOGGER.error("No PHONE_NUMBER_STATUS available in leadInfo:" + leadInfo);
					continue;
				}
				switch (statType) {
				case INVALID_NUMBER:
					leadInfo.setCountOfInvalidNumbers(((BigInteger) ((Object[]) object)[1]).intValue());
					break;
				case BLACKLIST_NUMBER:
					leadInfo.setCountOfBlackListNumbers(((BigInteger) ((Object[]) object)[1]).intValue());
					break;
				case NONRCS_NUMBER:
					leadInfo.setCountOfNonRcsNumbers(((BigInteger) ((Object[]) object)[1]).intValue());
					break;
				case OK:
					leadInfo.setCountOfNumbers(((BigInteger) ((Object[]) object)[1]).intValue());
					break;
				case DUPLICATE_NUMBER:
					leadInfo.setCountOfDuplicateNumbers(((BigInteger) ((Object[]) object)[1]).intValue());
					break;
				}
			}
			Integer totalNumbers = leadInfo.getCountOfNumbers() + leadInfo.getCountOfInvalidNumbers()
					+ leadInfo.getCountOfBlackListNumbers() + leadInfo.getCountOfNonRcsNumbers()
					+ leadInfo.getCountOfDuplicateNumbers();
			leadInfo.setCountOfNumbers(totalNumbers);
		}
	}

	public boolean uploadAllowed(int leadId, MultipartFile file) throws Exception {
		boolean uploadAllowed = false;
		LeadInfo leadInfo = null;
		try {
			leadInfo = getLeadInfo(leadId);
		} catch (Exception e) {
			throw e;
		}
		if (leadInfo == null) {
			throw new RuntimeException("Lead Id not found");
		}

		Long userId = leadInfo.getUserId();
		UserEntity userEntity = userRepository.getUserEntityByUserId(userId);
		int dailyLimit = userEntity.getDailyUsageLimit();
		Date now = new Date();
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			now = dateFormat.parse(dateFormat.format(new Date()));
		} catch (Exception e) {

		}
		UserDailyUsageEntity userDailyUsageEntity = userDailyUsageRepository
				.getUserDailyUsageEntityByUserIdAndUploadDate(userId, now);
		ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
		long recordCount = 0;
		long starttime = System.currentTimeMillis();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(bis))) {
			recordCount = br.lines().count();
		}
		int uploadCount = 0;
		if (userDailyUsageEntity != null) {
			uploadCount = userDailyUsageEntity.getUploadCount();
		} else {
			userDailyUsageEntity = new UserDailyUsageEntity();
			userDailyUsageEntity.setUploadDate(now);
			userDailyUsageEntity.setUploadCount(0);
			userDailyUsageEntity.setUserId(userId);
		}
		LOGGER.info("Total time taken in counting records:" + (System.currentTimeMillis() - starttime) + " Ms");
		if (dailyLimit - uploadCount > recordCount) {
			uploadAllowed = true;
			userDailyUsageEntity.setUploadCount(userDailyUsageEntity.getUploadCount() + (int) recordCount);
			LOGGER.info("For User: " + userId + " Upload allowed for record count: " + recordCount);
			userDailyUsageRepository.save(userDailyUsageEntity);
		} else {
			LOGGER.info("User Id " + userId + " Upload NOT allowed for record count :" + recordCount + " Daily Limit:"
					+ dailyLimit + " total Uploaded Records:" + userDailyUsageEntity.getUploadCount());
		}
		return uploadAllowed;
	}

	public LeadInfo getLeadInfo(int Id) throws Exception {
		LeadInfoEntity leadInfoEntity = leadInfoRepository.findById((long) Id).orElse(null);
		if (leadInfoEntity != null) {
			LeadInfo leadInfo = new LeadInfo();
			beanUtils.copyProperties(leadInfo, leadInfoEntity);

			ScheduleEntity scheduleEntity = leadInfoEntity.getScheduleId() != null
					? scheduleRepository.findById(leadInfoEntity.getScheduleId()).orElse(null)
					: null;
			if (scheduleEntity != null) {
				Schedule schedule = new Schedule();
				beanUtils.copyProperties(schedule, scheduleEntity);
				leadInfo.setLeadSchedule(schedule);
			}
			RetryInfoEntity retryInfoEntity = leadInfoEntity.getRetryId() != null
					? retryRepository.findById(leadInfoEntity.getRetryId()).orElse(null)
					: null;
			if (retryInfoEntity != null) {
				RetryInfo retryInfo = new RetryInfo();
				beanUtils.copyProperties(retryInfo, retryInfoEntity);
				leadInfo.setRetryInfo(retryInfo);
			}
			return leadInfo;
		} else
			return null;
	}

	@Override
	public boolean uploadAllowed(LeadInfo leadInfo, MultipartFile file) throws Exception {
		boolean uploadAllowed = false;
		try {
			if (leadInfo.getLeadId() != null && leadInfo.getLeadId() != 0) {
				return uploadAllowed(leadInfo.getLeadId().intValue(), file);
			} else {
				CampaignEntity campaignEntity = campaignRepository.findById(leadInfo.getCampaignId()).orElse(null);
				if (campaignEntity == null) {
					throw new RuntimeException("No Campaign found with ID " + leadInfo.getCampaignId() + " for Lead "
							+ leadInfo.getLeadName());
				}
				Long userId = campaignEntity.getUserId();
				UserEntity userEntity = userRepository.getUserEntityByUserId(userId);
				int dailyLimit = userEntity.getDailyUsageLimit();
				Date now = new Date();
				try {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					now = dateFormat.parse(dateFormat.format(new Date()));
				} catch (Exception e) {
				}
				UserDailyUsageEntity userDailyUsageEntity = userDailyUsageRepository
						.getUserDailyUsageEntityByUserIdAndUploadDate(userId, now);
				ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
				long recordCount = 0;
				long starttime = System.currentTimeMillis();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(bis))) {
					recordCount = br.lines().count();
				}
				LOGGER.info("Total time taken in counting records:" + (System.currentTimeMillis() - starttime) + " Ms");

				int uploadCount = 0;
				if (userDailyUsageEntity != null) {
					uploadCount = userDailyUsageEntity.getUploadCount();
				} else {
					userDailyUsageEntity = new UserDailyUsageEntity();
					userDailyUsageEntity.setUploadDate(now);
					userDailyUsageEntity.setUploadCount(0);
					userDailyUsageEntity.setUserId(userId);
				}
				LOGGER.info("Total time taken in counting records:" + (System.currentTimeMillis() - starttime) + " Ms");
				if (dailyLimit - uploadCount > recordCount) {
					uploadAllowed = true;
					userDailyUsageEntity.setUploadCount(userDailyUsageEntity.getUploadCount() + (int) recordCount);
					LOGGER.info("For User: " + userId + " Upload allowed for record count: " + recordCount);
					userDailyUsageRepository.save(userDailyUsageEntity);
				} else {
					LOGGER.info("User Id " + userId + " Upload NOT allowed for record count :" + recordCount
							+ " Daily Limit:" + dailyLimit + " total Uploaded Records:"
							+ userDailyUsageEntity.getUploadCount());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uploadAllowed;
	}

	@Override
	public boolean uploadAllowed(LeadInfo leadInfo) {
		boolean uploadAllowed = false;
		CampaignEntity campaignEntity = campaignRepository.findById(leadInfo.getCampaignId()).orElse(null);
		if (campaignEntity == null) {
			throw new RuntimeException(
					"No Campaign found with ID " + leadInfo.getCampaignId() + " for Lead " + leadInfo.getLeadName());
		}
		UserEntity userEntity = userRepository.getUserEntityByUserId(campaignEntity.getUserId());
		int dailyLimit = userEntity.getDailyUsageLimit();
		Date now = new Date();
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			now = dateFormat.parse(dateFormat.format(new Date()));
		} catch (Exception e) {
		}
		UserDailyUsageEntity userDailyUsageEntity = userDailyUsageRepository
				.getUserDailyUsageEntityByUserIdAndUploadDate(userEntity.getUserId(), now);
		long recordCount = leadInfo.getLeadInfoDetails().size();
		int uploadCount = 0;
		if (userDailyUsageEntity != null) {
			uploadCount = userDailyUsageEntity.getUploadCount();
		} else {
			userDailyUsageEntity = new UserDailyUsageEntity();
			userDailyUsageEntity.setUploadDate(now);
			userDailyUsageEntity.setUploadCount(0);
			userDailyUsageEntity.setUserId(userEntity.getUserId());
		}
		if (dailyLimit - uploadCount > recordCount) {
			uploadAllowed = true;
			userDailyUsageEntity.setUploadCount(userDailyUsageEntity.getUploadCount() + (int) recordCount);
			LOGGER.info("For User: " + userEntity.getUserId() + " Upload allowed for record count: " + recordCount);
			userDailyUsageRepository.save(userDailyUsageEntity);
		} else {
			LOGGER.info("User Id " + userEntity.getUserId() + " Upload NOT allowed for record count :" + recordCount
					+ " Daily Limit:" + dailyLimit + " total Uploaded Records:"
					+ userDailyUsageEntity.getUploadCount());
		}
		return uploadAllowed;
	}

	@Override
	public void processTestNumber(String phoneNumber) {
		LeadInfoEntity leadInfo = leadInfoRepository.findFirstByLeadName("TestLeadGoogle");
		if (leadInfo == null)
			throw new RuntimeException("Lead not found !!");
		UserEntity userEntity = userRepository.getUserEntityByUserId(leadInfo.getUserId());
		LeadInfoDetailEntity leadInfoDetailEntity = new LeadInfoDetailEntity();
		if (!validPhoneNumber(phoneNumber)) {
			throw new RuntimeException("Invalid Number !!");
		} else if (isBlackListed(phoneNumber, leadInfo.getUserId(), userEntity.getParentUserId())) {
			throw new RuntimeException("Blacklist Number !!");
		} /*
			 * else if (isNonRcs(phoneNumber)) { throw new
			 * RuntimeException("RCS not supported on this number"); }
			 */
		leadInfoDetailEntity.setLeadId(leadInfo.getLeadId());
		leadInfoDetailEntity.setStatus(CREATED);
		leadInfoDetailEntity.setNextCallDtm(new Date());
		leadInfoDetailEntity.setPhoneNumber(phoneNumber);
		leadInfoDetailEntity.setCreatedDate(new Date());
		leadInfoDetailEntity.setLastModifiedDate(new Date());
		leadInfoDetailEntity.setLastModifiedBy(leadInfo.getLastModifiedBy());
		leadInfoDetailEntity.setCreatedBy(userEntity.getUserName());
		leadInfoDetailEntity.setPhoneNumberStatus(OK);
		leadInfoDetailEntity.setAttemptNum(0);
		leadInfoDetailRepository.save(leadInfoDetailEntity);
		LOGGER.info("Saved a number - " + phoneNumber + " in TestLeadGoogle");
	}

	private boolean isNonRcs(String phoneNumber) {
		Class<? extends LeadServiceImpl> aClass = LeadServiceImpl.class;
		RbmApiHelper rbmApiHelper = new RbmApiHelper(new File(rcsMsgCredentials));// ("/opt/rcsmessaging/rbm-agent-service-account-credentials.json"));

		boolean isNonRcs = false;
		if (!phoneNumber.startsWith("+91")) {
			phoneNumber = "+91" + phoneNumber;
		}
		List<String> numbers = new ArrayList<>();
		numbers.add(phoneNumber);
		try {
			BatchGetUsersResponse users = rbmApiHelper.getUsers(numbers);
			if (CollectionUtils.isEmpty(users.getReachableUsers()) || users.getReachableUsers().isEmpty()) {
				isNonRcs = true;
			} else {
				LOGGER.info("Number " + phoneNumber + " is RCS compatible");
			}
		} catch (Exception e) {
			isNonRcs = true;
		}
		return isNonRcs;
	}

	@Override
	public List<LeadInfo> getAllLeadInfoByUserIdAndDateBetween(String startDate, String endDate, Long userId,
			Integer start, Integer limit, String campaignId, String leadId) throws Exception {
		List<LeadInfoEntity> leadInfoEntityList = null;
		LOGGER.info("getAllLeadInfo ->");

		if (limit == 0) {
			leadInfoEntityList = leadInfoRepository.findAllLeadInfoByUserIdAndDateBetween(startDate + " 00:00:00",
					endDate + " 23:59:59", userId);
		} else {
			start = start - 1;
			int pageSize = limit;
			int pageNum = start != 0 ? start / pageSize : 0;
			Pageable pageable = PageRequest.of(pageNum, pageSize);
			if (Objects.nonNull(campaignId) && Objects.isNull(leadId)) {
				leadInfoEntityList = leadInfoRepository.getAllLeadInfoByCampaignBetweenDate(startDate + " 00:00:00",
						endDate + " 23:59:59", userId, pageable, campaignId);
			} else if (Objects.nonNull(campaignId) && Objects.nonNull(leadId)) {
				leadInfoEntityList = leadInfoRepository.getAllLeadInfoByCampaignAndLeadIdBetweenDate(
						startDate + " 00:00:00", endDate + " 23:59:59", userId, pageable, campaignId, leadId);
			} else {
				leadInfoEntityList = leadInfoRepository.getAllLeadInfoByUserIdAndDateBetween(startDate + " 00:00:00",
						endDate + " 23:59:59", userId, pageable);
			}
		}
		List<LeadInfo> leadInfos = new ArrayList<>();
		for (LeadInfoEntity leadInfoEntity : leadInfoEntityList) {
			LeadInfo leadInfo = new LeadInfo();
			leadInfo.setCampaignName(
					campaignRepository.findById(leadInfoEntity.getCampaignId()).get().getCampaignName());
			beanUtils.copyProperties(leadInfo, leadInfoEntity);
			/*
			 * List<LeadInfoDetailEntity> leadInfoDetailEntityList =
			 * leadInfoDetailRepository
			 * .getLeadInfoDetailsByLeadId(leadInfoEntity.getLeadId()); List<LeadInfoDetail>
			 * leadInfoDetails = new ArrayList<>(); for (LeadInfoDetailEntity
			 * leadInfoDetailEntity : leadInfoDetailEntityList) { LeadInfoDetail
			 * leadInfoDetail = new LeadInfoDetail();
			 * beanUtils.copyProperties(leadInfoDetail, leadInfoDetailEntity);
			 * leadInfoDetails.add(leadInfoDetail); }
			 * 
			 * leadInfo.setLeadInfoDetails(leadInfoDetails);
			 */
			ScheduleEntity scheduleEntity = scheduleRepository.findById(leadInfoEntity.getScheduleId()).orElse(null);
			if (scheduleEntity != null) {
				Schedule schedule = new Schedule();
				beanUtils.copyProperties(schedule, scheduleEntity);
				leadInfo.setLeadSchedule(schedule);
			}

			leadInfos.add(leadInfo);
		}

		return leadInfos;

	}

	@Override
	public boolean checkIfNameExists(String leadName) {
		boolean leadExists = false;
		LeadInfoEntity firstByLeadName = leadInfoRepository.findFirstByLeadName(leadName);
		if (firstByLeadName != null) {
			leadExists = true;
		}
		return leadExists;
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	public LeadInfo saveLeadInfoAndDetails(LeadInfo leadInfo, MultipartFile file, boolean isDND, boolean isDuplicate)
			throws Exception {
		LOGGER.info("Service Upload New Lead => " + file.getOriginalFilename());
		CampaignEntity campaignEntity = campaignRepository.findById(leadInfo.getCampaignId()).orElse(null);
		if (campaignEntity == null) {
			LOGGER.error(
					"No Campaign found with ID " + leadInfo.getCampaignId() + " for Lead " + leadInfo.getLeadName());
			return null;
		}
		if (campaignEntity.getCampaignStatus().equalsIgnoreCase("Completed")) {
			LOGGER.info("**** save() inside campaignRepository.updateCampaignStatus(" + campaignEntity.getCampaignId()
					+ ",Active *****");
			campaignRepository.updateCampaignStatus(campaignEntity.getCampaignId(), "Active");
		}
		if (leadInfo == null) {
			throw new Exception("Lead info is null");
		}

		LeadInfoEntity leadInfoEntity = saveLead(leadInfo);

		if (leadInfoEntity == null) {
			throw new Exception("Lead info could not be saved");
		}
		leadInfoRepository.updateLeadStatus(leadInfoEntity.getLeadId(), "Inactive");
		LOGGER.info("Marked Lead As Inactive " + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName());
		LeadInfo returnedLead = save(leadInfoEntity.getLeadId().intValue(), file, isDND, isDuplicate);
		return returnedLead;
	}

	@SuppressWarnings("unused")
	public static List<List<String>> excelToStringList(MultipartFile is, Integer noOfLength) throws IOException {
		List<List<String>> dataSet = new ArrayList<>();
		// List<String> rowArray=new ArrayList();
		Workbook workbook = null;
		try {
			workbook = new HSSFWorkbook(is.getInputStream());

			if (workbook == null) {
				workbook = new XSSFWorkbook(is.getInputStream());
			}
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter dataFormatter = new DataFormatter();

			// Iterating over Rows and Columns using Java 8 forEach with lambda
			sheet.forEach(row -> {
				List<String> rowArray = new ArrayList<>();
				if (row.getLastCellNum() > noOfLength / 2) {
					/*
					 * because my excel sheet has max 5 columns, in case last column is empty then
					 * row.getLastCellNum() will
					 */
					int lastColumn = Math.max(row.getLastCellNum(), noOfLength);
					for (int cn = 0; cn < lastColumn; cn++) {
						Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
						if (cell.getCellType().equals(CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell)) {
							try {
								String cellValue = new SimpleDateFormat("MM/dd/yyyy").format(cell.getDateCellValue());
								rowArray.add(cellValue);
							} catch (Exception e) {
								rowArray.add(cell.getDateCellValue().toString());
							}
						} else {
							String cellValue = dataFormatter.formatCellValue(cell);
							rowArray.add(cellValue);
						}
					}
					dataSet.add(rowArray);
				}
			});

			workbook.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return dataSet;
	}

	private List<List<String>> parseExcelFile(List<List<String>> csvData) {
		return csvData.stream().filter(dataSet -> Boolean.FALSE.equals(dataSet.stream().allMatch(StringUtil::isBlank)))
				.collect(Collectors.toList());
	}

	private List<List<String>> parseCsvFile(List<String[]> csvData) {
		return csvData.stream().map(row -> new ArrayList<>(Arrays.asList(row)))
				.filter(dataSet -> Boolean.FALSE.equals(dataSet.stream().allMatch(StringUtil::isBlank)))
				.collect(Collectors.toList());
	}

	public String getDynmaicParamFromFileHeader(File file) {
		List<List<String>> dataList = new ArrayList<>();
		List<String[]> csvData = null;
		LOGGER.info("***** Started MisService.getDynmaicParamFromFileHeader() ******");
		List<List<String>> excelData = null;
		String response = "";
		try {
			if ("csv".equals(file.getName().substring(file.getName().lastIndexOf('.') + 1))) {
				LOGGER.info("***** MisService.getDynmaicParamFromFileHeader() Inside CSV ******");
				csvData = CSVHelper.convertToCSVStringList(new FileInputStream(file));
				dataList = parseCsvFile(csvData);
			} else {
				LOGGER.info("***** MisService.getDynmaicParamFromFileHeader() Inside Excel ******");
				excelData = CSVUtils.excelToStringList(file, 10);
				dataList = parseExcelFile(excelData);

			}
			if (dataList.size() > 0) {

				List<String> ss = new ArrayList<>(
						Arrays.asList(dataList.get(0).toString().trim().replaceAll("[\\[\\]]", "").split(",")));
				ss.remove(0);
				LOGGER.info("***** MisService.getDynmaicParamFromFileHeader() After Saving  Incoming Did List ******");

				return ss.toString().replaceAll("[\\[\\]]", "");

			}

		} catch (Exception ee) {
			response = "Fail To Store Given File." + ee.getMessage();
			ee.printStackTrace();
			// throw new RuntimeException("Fail To Store Given File." + ee.getMessage());
		}
		LOGGER.info("***** Successfully Executed MisService.getDynmaicParamFromFileHeader() Got Response ******"
				+ response);

		return null;

	}

	public LeadInfo createLeadInfoDetailAfterUploadFileFromDB(int Id, File file, boolean isDND, boolean isDuplicate)
			throws Exception {

		LOGGER.info("SavedLead: createLead ->");
		LOGGER.info("Service Update Existing Lead via file");
		boolean ikslCampaign = false;
		BufferedReader bufferedReader = null;
		BufferedReader br = null;
		BufferedReader bufR = null;
		LeadInfo leadInfo = null;
		TokenPojo pojo = null;
		UserEntity userEntity = null;
		boolean isTransactionalCampaign = false;
		LOGGER.info("upload lead file leadId->" + Id);
		String fileName = file.getName();
		try {
			long startTime = System.currentTimeMillis();
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			bufferedReader.readLine();
			int idx = 0;
			List<Integer> dateIndexes = new ArrayList<>();
			List<Integer> numIndexes = new ArrayList<>();
			List<Integer> textIndexes = new ArrayList<>();
			List<Integer> langIndexes = new ArrayList<>();
			List<Integer> playIndexes = new ArrayList<>();
			int phoneNumberIdx = -1;

			leadInfo = getLeadInfo(Id);

			userEntity = userRepository.getUserEntityByUserId(leadInfo.getUserId());
			CampaignEntity campaignEntity = campaignRepository.getCampaignEntityByCampaignId(leadInfo.getCampaignId());

			LOGGER.info("Upload lead file leadInfo->" + leadInfo);
			List<Thread> fileReadThreadList = new ArrayList<>();
			String line = null;
			int duplicateCount = 0;
			List<String> duplicateRecords = new ArrayList<>();
			Thread dupThread = null;
			String dynmaicParamFromFileHeader = getDynmaicParamFromFileHeader(file);// (convert(file));
			if (leadInfo != null) {
				partitionCheck(leadInfo.getLeadId());

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Set<String> setLines = new HashSet<>();
				if (isDuplicate) {
					leadInfo.setLeadStatus(INACTIVE);
					while ((line = bufferedReader.readLine()) != null) {
						if (!setLines.contains(line)) {
							setLines.add(line);
						} else {
							duplicateCount++;
							duplicateRecords.add(line);
						}
					}
					bufferedReader.close();

					for (String str : setLines) {
						StringBuilder sb = new StringBuilder();
						sb.append(str).append("\n");
						baos.write(sb.toString().getBytes());
					}
					byte[] bytes = baos.toByteArray();
					InputStream in = new ByteArrayInputStream(bytes);
					bufferedReader = new BufferedReader(new InputStreamReader(in));
				}
				int threadNo = 8;// 10;
				Thread t1;

				/**
				 * 
				 */
				OperatorApiConfigEntity operatorApiConfigEntity = null;
				UserBotMappingEntity userBotMappingEntity = null;
				Optional<Template> tem = tempRepository.findById(campaignEntity.getTemplateId());
				if (Objects.nonNull(tem.get().getUserBotMappingId() != null)) {
					userBotMappingEntity = userBotMappingRepository.findById(tem.get().getUserBotMappingId());

					operatorApiConfigEntity = operatorApiConfigRepository.findByApiTypeAndTemplateTypeAndOperatorId(
							"TEMPLATE",
							operatorRepository.findByOperatorName(userBotMappingEntity.getOperator()).getOperatorId());

					if (Objects.nonNull(operatorApiConfigEntity)) {
						pojo = new Gson()
								.fromJson(botTokenAPIService.getTokenFromClientAPI(operatorApiConfigEntity.getApiUrl(),
										userBotMappingEntity.getBasicToken()), TokenPojo.class);
					}
				}

				for (int i = 0; i < threadNo; i++) {
					LOGGER.info("upload lead file starting thread->" + i);

					t1 = new Thread(new FileReadThread(fileName, leadInfo, leadInfoDetailRepository, bufferedReader,
							isDND, isDuplicate, dateIndexes, numIndexes, textIndexes, langIndexes, playIndexes,
							blackListRepository, userEntity, jdbcTemplate, false, botTokenAPIService, dndRepository,
							pojo, dynmaicParamFromFileHeader, rcsEnabled), "Thread " + i);
					fileReadThreadList.add(t1);
					t1.start();
				}
				if (isDuplicate && !duplicateRecords.isEmpty()) {
					baos = new ByteArrayOutputStream();
					for (String str : duplicateRecords) {
						StringBuilder sb = new StringBuilder();
						sb.append(str).append("\n");
						baos.write(sb.toString().getBytes());
					}
					byte[] bytes = baos.toByteArray();
					InputStream in = new ByteArrayInputStream(bytes);
					bufR = new BufferedReader(new InputStreamReader(in));
					dupThread = new Thread(new FileReadThread(fileName, leadInfo, leadInfoDetailRepository,
							bufferedReader, isDND, isDuplicate, dateIndexes, numIndexes, textIndexes, langIndexes,
							playIndexes, blackListRepository, userEntity, jdbcTemplate, false, botTokenAPIService,
							dndRepository, pojo, dynmaicParamFromFileHeader, rcsEnabled), "Thread Dup");
					fileReadThreadList.add(dupThread);
					dupThread.start();
				}
			}
			for (Thread t : fileReadThreadList) {
				t.join();
			}

			LOGGER.info("Filling Counters");
			fillCounters(leadInfo);
			LOGGER.info("Filled Counters");
			int actualDups = leadInfo.getCountOfDuplicateNumbers();
			leadInfo.setCountOfDuplicateNumbers(duplicateCount);
			leadInfo.setCountOfNumbers(leadInfo.getCountOfNumbers() + duplicateCount - actualDups);
			long startT = System.currentTimeMillis();
			leadInfo.setLeadStatus(ACTIVE);
			LeadInfoEntity leadInfoEntity = leadInfoRepository.findById(leadInfo.getLeadId()).orElse(null);
			if (leadInfoEntity != null) {
				LOGGER.info("Lead already existed, seems tested before uploading file..");
				leadInfoEntity.setCountOfNumbers(leadInfo.getCountOfNumbers());
				leadInfoEntity.setCountOfNonRcsNumbers(leadInfo.getCountOfNonRcsNumbers());
				leadInfoEntity.setCountOfDuplicateNumbers(leadInfo.getCountOfDuplicateNumbers());
				leadInfoEntity.setCountOfInvalidNumbers(leadInfo.getCountOfInvalidNumbers());
				leadInfoEntity.setCountOfBlackListNumbers(leadInfo.getCountOfBlackListNumbers());
				leadInfoEntity.setLeadStatus("Active");
				LOGGER.info("Previous Lead status ====> " + leadInfoEntity.getLeadCompletionStatus());

				if (COMPLETED.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())
						&& isLeadToBeRunToday(leadInfoEntity)) {
					LOGGER.info("Marking Lead as Running");
					leadInfo.setLeadCompletionStatus("Running");
					leadInfoEntity.setLeadCompletionStatus("Running");
				} else {
					if (!isLeadToBeRunToday(leadInfo)) {
						Schedule schedule = leadInfo.getLeadSchedule();
						if ("0".equalsIgnoreCase(schedule.getScheduleDay())) {
							LOGGER.info("Marking Lead as " + "Run Manually");
							leadInfo.setLeadCompletionStatus("Run Manually");
							leadInfoEntity.setLeadCompletionStatus("Run Manually");
						} else {
							LOGGER.info("Marking Lead as " + "Scheduled Later");
							leadInfo.setLeadCompletionStatus("Scheduled Later");
							leadInfoEntity.setLeadCompletionStatus("Scheduled Later");
						}
					} else {
						LOGGER.info("Marking Lead as " + leadInfoEntity.getLeadCompletionStatus());
						leadInfo.setLeadCompletionStatus(leadInfoEntity.getLeadCompletionStatus());
					}
				}
				leadInfoEntity = leadInfoRepository.save(leadInfoEntity);
				long totalT = System.currentTimeMillis() - startT;
				LOGGER.info("CHECKIT:Total Time in saving duplicates Leads for File " + fileName + " time taken: "
						+ (totalT) + " Ms");
				LeadInfoEntity finalLead = leadInfoEntity;
				Runnable r = () -> Notifications.getsInstance().notifyEventCaptured(LEAD_CREATED, finalLead);
				new Thread(r).start();
				LOGGER.info("Saved Lead via fileupload:" + leadInfoEntity);
				long endTime = System.currentTimeMillis();
				LOGGER.info("CHECKIT: Saved Leads for File " + fileName + " time taken: " + (endTime - startTime));
				LOGGER.info("SaveLead leadInfo.getCountOfNumbers(): " + leadInfoEntity.getCountOfNumbers());
				LOGGER.info(
						"SaveLead leadInfo.getCountOfInvalidNumbers(): " + leadInfoEntity.getCountOfInvalidNumbers());
				LOGGER.info("SaveLead leadInfo.getCountOfNonRcsNumbers(): " + leadInfoEntity.getCountOfNonRcsNumbers());
				LOGGER.info("SaveLead leadInfo.getCountOfBlackListNumbers(): "
						+ leadInfoEntity.getCountOfBlackListNumbers());
			} else {
				LOGGER.info("Saving Lead after file reading without testing..");
				saveLead(leadInfo);// leadInfo with counters
			}
		} catch (Exception ex) {
			LOGGER.error("Error:", ex);
			ex.printStackTrace();
			throw ex;
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufR != null) {
				bufR.close();
				bufR = null;
			}
		}

		return leadInfo;
	}

	/**
	 * This method is useful for check bulk rcs or not First get count mno based on
	 * active lead then divide each request to 5000 or below
	 */

	public void checkBulkRcsPhoneNo(UserEntity userEntity, LeadInfo leadInfo) {
		TokenPojo pojo = null;
		try {
			/**
			 * 
			 */
			OperatorApiConfigEntity operatorApiConfigEntity = null;
			UserBotMappingEntity userBotMappingEntity = null;
			CampaignEntity campaignEntity = campaignRepository.getCampaignEntityByCampaignId(leadInfo.getCampaignId());

			Optional<Template> tem = tempRepository.findById(campaignEntity.getTemplateId());
			if (Objects.nonNull(tem.get().getUserBotMappingId() != null)) {
				userBotMappingEntity = userBotMappingRepository.findById(tem.get().getUserBotMappingId());

				operatorApiConfigEntity = operatorApiConfigRepository.findByApiTypeAndTemplateTypeAndOperatorId(
						"TEMPLATE",
						operatorRepository.findByOperatorName(userBotMappingEntity.getOperator()).getOperatorId());

				if (Objects.nonNull(operatorApiConfigEntity)) {
					pojo = new Gson()
							.fromJson(botTokenAPIService.getTokenFromClientAPI(operatorApiConfigEntity.getApiUrl(),
									userBotMappingEntity.getBasicToken()), TokenPojo.class);
				}
			}

			List<String> noCodeList = null;
			long totalCountphoneNo = leadInfoDetailRepository.findMnoListCountByLeadId(leadInfo.getLeadId());
			LOGGER.info("Total Count For This LeadID:: " + leadInfo.getLeadId() + " And " + totalCountphoneNo);
			for (int i = 0; i <= totalCountphoneNo; i = i + 10000) {
				noCodeList = new ArrayList<>();

				LOGGER.info(
						"leadInfoDetailRepository.findMnoListCountByLeadId(leadInfo.getLeadId() OFFSET Size ::" + i);
				List<String> phoneNumberList = leadInfoDetailRepository
						.findPhoneNumberBasedOnLimitAndOffset(leadInfo.getLeadId(), 10000, i);
				System.out.println("phoneNumberList Size after offset " + phoneNumberList.size());
				for (String phoneNumber : phoneNumberList) {
					// System.out.println(phoneNumber);
					if (phoneNumber.length() == 10)
						noCodeList.add("+91" + phoneNumber.substring(phoneNumber.length() - 10));
				}

				LOGGER.info("Going On Verified RCS or Non-Rcs:::=>" + noCodeList.size());
				// send list ot check rcs api then api will give rcs no take again loop update
				// no rcs
				// phoneNumber = "+91" + phoneNumber.substring(phoneNumber.length() - 10);

				updateRcsNoAfterBulkApiResponse(userEntity, noCodeList, pojo, leadInfo);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Async(value = "checkRcsBulkTaskExecutor")
	public void updateRcsNoAfterBulkApiResponse(UserEntity userEntity, List<String> noCodeList, TokenPojo pojo,
			LeadInfo leadInfoEntity) {
		try {
			LOGGER.info("Started for updating updateRcsNoAfterBulkApiResponse() got Size " + noCodeList.size()
					+ " AND Thread Name::" + Thread.currentThread().getName());

			// String noListJson = new Gson().toJson(noCodeList).toString();
			String rcsEnableContactsResponse = null;

			rcsEnableContactsResponse = botTokenAPIService.checkBulkRcsNumber(pojo.getAccess_token(), noCodeList,
					userEntity.getBotId());

			if (rcsEnableContactsResponse.contains("rcsEnabledContacts")) {
				BulkTokenResponse bulkTokenResponse = new Gson().fromJson(rcsEnableContactsResponse,
						BulkTokenResponse.class);
				LOGGER.info("Response From Rcs Bulk API ::" + bulkTokenResponse.toString());

				for (String number : bulkTokenResponse.getRcsEnabledContacts()) {

					if (number.startsWith("+91"))
						number = number.replace("+91", "");
					LOGGER.info("update is going on ::" + number);
					leadInfoDetailRepository.updatePhoneNumberStatusByLeadIdAndPhoneNumber(number, "OK",
							leadInfoEntity.getLeadId());
				}
			}
			LOGGER.info("Ended for updating updateRcsNoAfterBulkApiResponse()");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File convert(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}
}
