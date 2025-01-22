package com.messaging.rcs.service;

import static com.messaging.rcs.util.SystemConstants.MEDIA;
import static com.messaging.rcs.util.SystemConstants.RICH_CARDS;
import static com.messaging.rcs.util.SystemConstants.SUGGESTED_REPLIES;
import static com.messaging.rcs.util.SystemConstants.TEXT;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.rcsbusinessmessaging.v1.model.StandaloneCard;
import com.messaging.rcs.domain.AttemptInfoEntity;
import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.domain.MessageTypeEntity;
import com.messaging.rcs.domain.RetryInfoEntity;
import com.messaging.rcs.domain.ScheduleEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.model.EmailRequestModel;
import com.messaging.rcs.email.service.EmailService;
import com.messaging.rcs.rbm.MessagingAgent;
import com.messaging.rcs.rbm.messages.Media;
import com.messaging.rcs.rbm.messages.RichCards;
import com.messaging.rcs.rbm.messages.SuggestedReplies;
import com.messaging.rcs.rbm.messages.Text;
import com.messaging.rcs.repository.AttemptRepository;
import com.messaging.rcs.repository.BlackListRepository;
import com.messaging.rcs.repository.CampaignRepository;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.repository.LeadInfoRepository;
import com.messaging.rcs.repository.MessageResponseLogsRepository;
import com.messaging.rcs.repository.MessageSummaryRepository;
import com.messaging.rcs.repository.MessageTypeRepository;
import com.messaging.rcs.repository.RetryRepository;
import com.messaging.rcs.repository.ScheduleRepository;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.util.CampaignConfigurationTree;
import com.messaging.rcs.util.CampaignTaskManager;
import com.messaging.rcs.util.LeadTaskManager;
import com.messaging.rcs.util.UserConfigurationTree;

/*
Main Process Flow:
1.	System will first select all lead IDs which are schedule/ready to be run.
2.	Calculate the number of free channel(s) at each slave/node. Each node must not be send dialing data more 3 times of number of free channels at that node.
3.	Calculate channels to be allocated for each lead ID as per the user/campaign/lead priority and channel allocation scheme for each user/campaign/lead as defined/configured in the system.
4.	Select data from each lead by considering calculation done in step #2 and #3.
5.	Distribute the data to each node/slave and marked it as “Processing”.
6.	GoTo step#1.
*/
@Service
public class MessageProcessingEngine implements EventNotificationI {

	private static final Logger LOGGER = Logger.getLogger(MessageProcessingEngine.class.getName());
	private static final Integer MULTIPYING_FACTOR = 1;
	private static final String RUNNING = "Running";
	private static final String TEMP_PAUSE = "Temp Pause";
	private static final String CYCLIC = "C";
	private static final String LEAD_COMPLETED = "LEAD_COMPLETED";
	public static final String SCHEDULE_EXPIRED = "Schedule Expired";
	private static final String COMPLETED = "Completed";

	@Value("#{new Boolean('${optimized.publishing.enabled:false}')}")
	private Boolean optimizedPublishingEnabled;

	@Value("${com.obd.admin.senderid:SHIVTL}")
	private String adminSenderId;
	@Value("${com.obd.admin.sms.username:qverifer.trans}")
	private String smsUserName;
	@Value("${com.obd.admin.sms.password:s2HgQ}")
	private String smsPassword;
	@Value("${com.obd.panel.name:Panel}")
	private String panelName;

	private Map<Long, Object> campaignWiseRbmMessage = new HashMap<>();
	private Map<Long, MessagingAgent> leadWiseMessagingAgent = new HashMap<>();

	private static final String DYNAMIC_CHANNEL_ALLOCATION_SCHEME = "dynamic";
	private static final String STATIC_CHANNEL_ALLOCATION_SCHEME = "static";

	public static final String CLI_SCHEME_DYNAMIC = "dynamic";
	private static final String ACTIVE = "Active";
	private static final String SEQUENTIAL = "SEQUENTIAL";
	private static final String ROUND_ROBIN = "RR";
	private static final String AUTO = "AUTO";
	private static final String WEIGHTED_AUTO = "WAUTO";
	private static final String HIGH = "HIGH";
	private static final String MEDIUM = "MEDIUM";
	private static final String LOW = "LOW";
	private static final String DIALED = "Dialed";
	private static final String PUBLISHED = "Published";
	private static final String CREATED = "Created";
	private static final String DIALFAIL = "DialFail";
	private static final String START_ACTION = "Start";
	private static final String STOP_ACTION = "Stop";
	private static final String COMPLETED_ACTION = "Completed";
	private static final String LEAD_START_ACTION = "Running";
	private static final String ATTEMPT_PENDING_STATUS = "Pending";
	private static final String POOL_CREATED = "POOL_CREATED";
	private static final String USER_CREATED = "USER_CREATED";
	private static final String CAMPAIGN_CREATED = "CAMPAIGN_CREATED";
	private static final String LEAD_CREATED = "LEAD_CREATED";
	private static final Integer SLEEP_DURATION_MS = 200;
	private Boolean appStarted = false;
	private Boolean pauseUsed = false;

	public MessageProcessingEngine() {
		Notifications.getsInstance().registerEventNotificationListener(this, 10);
	}

	@Autowired
	private LeadInfoRepository leadInfoRepository;
	@Autowired
	private CampaignRepository campaignRepository;
	@Autowired
	private LeadInfoDetailRepository leadInfoDetailRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AttemptRepository attemptRepository;
	@Autowired
	private RetryRepository retryRepository;
	@Autowired
	private MessageSummaryRepository messageSummaryRepository;
	@Autowired
	private LeadService leadService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private MessageTypeRepository messageTypeRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MessageResponseLogsRepository messageResponseLogsRepository;
	@Autowired
	private BlackListRepository blackListRepository;

	private BeanUtilsBean beanUtils = new BeanUtilsBean();
	private UserEntity adminUser = null;
	private MessagingAgent messagingAgent = null;

	@PersistenceUnit()
	private EntityManagerFactory entityManagerFactory;

	private List<UserConfigurationTree> userConfigurationTreeList = new CopyOnWriteArrayList<>();
	private List<Long> activeLeads = new CopyOnWriteArrayList<>();
	private ReentrantReadWriteLock acitveLeadLock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock.ReadLock leadReadLock = acitveLeadLock.readLock();
	private ReentrantReadWriteLock.WriteLock leadWriteLock = acitveLeadLock.writeLock();

	/**
	 * User - P2U1 Campaign - P2U1C1 Lead - P2U1C1L1 - A P2U1C1L2 - I
	 */

//@PostConstruct
	public void init() {
		// Read Configuration and prepare a tree in memory
		prepareConfigurationTreeBasedOnCurrentSavedConfigurations();

		// Invoke processing of Pool Threads
		invokeProcessingOfAllUsers();
		appStarted = true;
		adminUser = userRepository.getAdminUser().get(0);
		messagingAgent = new MessagingAgent(messageResponseLogsRepository, leadInfoDetailRepository, activeLeads,
				leadReadLock, leadInfoRepository, blackListRepository);
	}

	public void addOrReplaceMessage(CampaignEntity campaignEntity) {
		campaignWiseRbmMessage.put(campaignEntity.getCampaignId(), campaignEntity.getMessageJson());
	}

	@Override
	public void notifyEventCaptured(String eventType, Object... pArgs) {

		switch (eventType) {

		case CAMPAIGN_CREATED:
			CampaignEntity campaignEntity = (CampaignEntity) pArgs[0];
			updatePoolConfigurationTreeMap(campaignEntity);
			LOGGER.info(CAMPAIGN_CREATED + " => " + campaignEntity.getCampaignName());
			break;
		case LEAD_CREATED:
			LeadInfoEntity leadInfoEntity = (LeadInfoEntity) pArgs[0];
			try {
				leadWriteLock.lock();
				activeLeads.add(leadInfoEntity.getLeadId());
			} finally {
				leadWriteLock.unlock();
			}
			updatePoolConfigurationTreeMap(leadInfoEntity);
			LOGGER.info(LEAD_CREATED + " => " + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName());
			break;
		default:
			LOGGER.info("Unknown Event Type [ " + eventType + " ] received");
		}
	}

	private void updatePoolConfigurationTreeMap(Object object) {

		LOGGER.info("updatePoolConfigurationTreeMap");
		if (object instanceof CampaignEntity) {

			if (!userConfigurationTreeList.isEmpty()) {
				Long userId = ((CampaignEntity) object).getUserId();
				UserEntity userEntity = userRepository.findById(userId).orElse(null);

				if (userEntity != null) {
					UserConfigurationTree userConfigurationTree = userConfigurationTreeList.stream()
							.filter(t -> t.getUserEntity().getUserId() == userEntity.getUserId()).findFirst()
							.orElse(null);
					CampaignConfigurationTree campaignConfigurationTree = new CampaignConfigurationTree();
					campaignConfigurationTree.setCampaignEntity((CampaignEntity) object);

					campaignConfigurationTree.setThreadStarted(false);
					campaignConfigurationTree.setCampaignStatus(ACTIVE);
					// Check if campaign list exists
					if (userConfigurationTree != null && userConfigurationTree.getUserWiseCampaignList() != null
							&& !userConfigurationTree.getUserWiseCampaignList().isEmpty()) {
						CopyOnWriteArrayList<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
								.getUserWiseCampaignList();
						// check if this campaign already exists
						CampaignConfigurationTree campaignConfigurationTree1 = campaignConfigurationTrees.stream()
								.filter(t -> t.getCampaignEntity() != null && t.getCampaignEntity().getCampaignId()
										.longValue() == ((CampaignEntity) object).getCampaignId().longValue())
								.findFirst().orElse(null);
						if (campaignConfigurationTree1 != null) {
							// update already existing campaign
							LOGGER.info("Updating Existing campaign.."
									+ campaignConfigurationTree1.getCampaignEntity().getCampaignId() + "=>"
									+ campaignConfigurationTree1.getCampaignEntity().getCampaignName());
							String campaignUpdatedStatus = ((CampaignEntity) object).getCampaignStatus();
							campaignConfigurationTree1.setCampaignStatus(campaignUpdatedStatus);
							LOGGER.info(
									"Marked Campaign [" + campaignConfigurationTree1.getCampaignEntity().getCampaignId()
											+ "] " + campaignUpdatedStatus + " via notification");
							if (ACTIVE.equalsIgnoreCase(campaignUpdatedStatus)) {
								userConfigurationTree.getActiveCampaignList()
										.add(campaignConfigurationTree1.getCampaignEntity().getCampaignId());
							}
							campaignRepository.updateCampaignStatus(
									campaignConfigurationTree1.getCampaignEntity().getCampaignId(),
									campaignUpdatedStatus);
							campaignConfigurationTree1.setCampaignEntity((CampaignEntity) object);
						} else {
							// add new campaign
							LOGGER.info("New Campaign added in userConfigurationTree"
									+ campaignConfigurationTree.getCampaignEntity().getCampaignName());
							userConfigurationTree.getUserWiseCampaignList().add(campaignConfigurationTree);
							userConfigurationTree.getUserWiseAllCampaignList().add(campaignConfigurationTree);
						}
					} else {
						// else create a new CampaignConfigurationTree list and add this campaign to it
						LOGGER.info("Created a new CampaignConfigurationTree list and added this campaign to it"
								+ campaignConfigurationTree.getCampaignEntity().getCampaignName());
						CopyOnWriteArrayList<CampaignConfigurationTree> campaignConfigurationTrees = new CopyOnWriteArrayList<>();
						campaignConfigurationTrees.add(campaignConfigurationTree);
						userConfigurationTree.setUserWiseCampaignList(campaignConfigurationTrees);
						userConfigurationTree.getUserWiseAllCampaignList().add(campaignConfigurationTree);
					}

				} else {
					LOGGER.info("Error: PoolEntity does not exists for User" + userEntity.getFirstName());
				}
			} else {
				LOGGER.info(
						"Seems some error as there are no Pools exists, create a Pool first then User and then campaign");
			}

		} else if (object instanceof LeadInfoEntity) {
			LOGGER.info("LeadInfo updated recieved");
			if (!userConfigurationTreeList.isEmpty()) {
				Long campaignId = ((LeadInfoEntity) object).getCampaignId();
				CampaignEntity campaignEntity = campaignRepository.findById(campaignId).orElse(null);
				if (campaignEntity != null) {
					UserEntity userEntity = userRepository.findById(campaignEntity.getUserId()).orElse(null);
					if (userEntity != null) {

						UserConfigurationTree userConfigurationTree = userConfigurationTreeList.stream()
								.filter(t -> t.getUserEntity().getUserId() == userEntity.getUserId()).findFirst()
								.orElse(null);
						if (userConfigurationTree != null) {
							// find respective campaign from user
							CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree
									.getUserWiseCampaignList().stream().filter(t -> t.getCampaignEntity()
											.getCampaignId().longValue() == campaignId.longValue())
									.findFirst().orElse(null);
							if (campaignConfigurationTree != null) {
								// check if lead exists in this campaign
								LeadInfoEntity leadInfoEntity = campaignConfigurationTree.getCampaignWiseLeadList()
										.stream().filter(t -> t != null && t.getLeadId()
												.longValue() == ((LeadInfoEntity) object).getLeadId().longValue())
										.findFirst().orElse(null);
								if (leadInfoEntity != null) {
									LOGGER.info("Lead [" + leadInfoEntity.getLeadId() + " - "
											+ leadInfoEntity.getLeadName() + " already saved to configuration tree");
									campaignConfigurationTree.setCampaignStatus(ACTIVE);
									LOGGER.info("Marked Campaign ["
											+ campaignConfigurationTree.getCampaignEntity().getCampaignId()
											+ "] Active via notification");
									userConfigurationTree.getActiveCampaignList()
											.add(campaignConfigurationTree.getCampaignEntity().getCampaignId());
									campaignRepository.updateCampaignStatus(
											campaignConfigurationTree.getCampaignEntity().getCampaignId(), ACTIVE);
									campaignConfigurationTree.getCampaignEntity().setCampaignStatus(ACTIVE);
								} else {
									campaignConfigurationTree.getCampaignWiseLeadList().add((LeadInfoEntity) object);
									campaignConfigurationTree.setCampaignStatus(ACTIVE);
									campaignConfigurationTree.getCampaignEntity().setCampaignStatus(ACTIVE);
									LOGGER.info("Marked Campaign ["
											+ campaignConfigurationTree.getCampaignEntity().getCampaignId()
											+ "] Active via notification");
									if (!ACTIVE.equalsIgnoreCase(campaignEntity.getCampaignStatus())) {
										campaignRepository.updateCampaignStatus(
												campaignConfigurationTree.getCampaignEntity().getCampaignId(), ACTIVE);
									}
									userConfigurationTree.getActiveCampaignList()
											.add(campaignConfigurationTree.getCampaignEntity().getCampaignId());
									LOGGER.info("New Lead [" + ((LeadInfoEntity) object).getLeadName()
											+ " ] Added in Campaign "
											+ campaignConfigurationTree.getCampaignEntity().getCampaignName()
											+ " via notification");
								}
							} else {
								LOGGER.info("CampaignConfigurationTree is null for User Id" + userEntity.getUserId()
										+ " and campaign Id " + campaignId);
								campaignConfigurationTree = new CampaignConfigurationTree();
								CampaignEntity campaignEntity1 = campaignRepository
										.findById((((LeadInfoEntity) object).getCampaignId())).orElse(null);
								campaignRepository.updateCampaignStatus(campaignEntity1.getCampaignId(), ACTIVE);
								campaignConfigurationTree.setCampaignStatus(ACTIVE);

								campaignConfigurationTree.setCampaignEntity(campaignEntity1);
								campaignConfigurationTree.getCampaignEntity().setCampaignStatus(ACTIVE);
								campaignConfigurationTree.setThreadStarted(false);
								LOGGER.info("Marked Campaign ["
										+ campaignConfigurationTree.getCampaignEntity().getCampaignId()
										+ "] Active via notification");
								userConfigurationTree.getActiveCampaignList()
										.add(campaignConfigurationTree.getCampaignEntity().getCampaignId());
								campaignConfigurationTree.getCampaignWiseLeadList().add((LeadInfoEntity) object);
								userConfigurationTree.getUserWiseCampaignList().add(campaignConfigurationTree);
								LOGGER.info("Added an Active Campaign in userConfigurationTree "
										+ userConfigurationTree.getUserEntity().getFirstName());
							}
						}

					} else {
						LOGGER.info("UserEntity is null for Campaign Id " + campaignEntity.getCampaignId());
					}
				} else {
					LOGGER.info("CampaignEntity is null for Lead Id " + ((LeadInfoEntity) object).getLeadId());
				}
			} else {
				LOGGER.info(
						"Seems some error as there are no Pools exists, create a Pool first then User and then campaign, then lead");
			}

		} else {
			LOGGER.info("Unknown type instance" + object.getClass().getName());
		}
	}

	/**
	 * Prepares a configuration Tree based on the saved configurations of below type
	 * * Pool-P2 User - P2U1 Campaign - P2U1C1 Lead - P2U1C1L1 - A P2U1C1L2 - I
	 */
	private void prepareConfigurationTreeBasedOnCurrentSavedConfigurations() {
		// Prepare all data based on current configurations saved in the system and
		// spawn worker threads accordingly

		List<UserEntity> userEntityList = userRepository.findAll();
		for (UserEntity userEntity : userEntityList) {
			LOGGER.info("Got User:" + userEntity.getUserId());
			UserConfigurationTree userConfigurationTree = new UserConfigurationTree();
			userConfigurationTree.setUserEntity(userEntity);
			LOGGER.info("userConfigurationTree for " + userConfigurationTree.getUserEntity().getFirstName()
					+ "setThreadStarted(false) ");
			userConfigurationTree.setThreadStarted(false);
			List<CampaignEntity> campaignEntityList = campaignRepository
					.getActiveCampaignsByUserId(userEntity.getUserId());
			List<CampaignEntity> allCampaignList = campaignRepository.getCampaignByUserId(userEntity.getUserId());
			if (!CollectionUtils.isEmpty(allCampaignList)) {
				for (CampaignEntity campaignEntity : allCampaignList) {
					CampaignConfigurationTree campaignConfigurationTree = new CampaignConfigurationTree();
					campaignConfigurationTree.setCampaignEntity(campaignEntity);
					LOGGER.info("Added Campaign for User " + userEntity.getUserId() + " Campaigns:" + campaignEntity);
					userConfigurationTree.getUserWiseAllCampaignList().add(campaignConfigurationTree);
				}
			} else {
				LOGGER.info("NOT FOUND Added Campaign for User " + userEntity.getUserId());
			}
			List<CampaignConfigurationTree> campaignConfigurationTrees = new CopyOnWriteArrayList<>();

			for (CampaignEntity campaignEntity : campaignEntityList) {
				CampaignConfigurationTree campaignConfigurationTree = new CampaignConfigurationTree();
				campaignConfigurationTree.setCampaignEntity(campaignEntity);
				campaignConfigurationTree.setThreadStarted(false);
				campaignConfigurationTree.setCampaignStatus(ACTIVE);
				List<LeadInfoEntity> leadInfoEntityList = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				if (!CollectionUtils.isEmpty(leadInfoEntityList)) {
					leadInfoEntityList.forEach(t -> activeLeads.add(t.getLeadId()));
				}
				campaignConfigurationTree.setCampaignWiseLeadList(new CopyOnWriteArrayList<>(leadInfoEntityList));
				userConfigurationTree.getActiveCampaignList().add(campaignEntity.getCampaignId());
				campaignConfigurationTrees.add(campaignConfigurationTree);
			}

			userConfigurationTree.setUserWiseCampaignList(new CopyOnWriteArrayList<>(campaignConfigurationTrees));
			userConfigurationTreeList.add(userConfigurationTree);
		}
	}

	/**
	 * This checks poolConfigurationTreeList map and if anything found then spawns a
	 * thread or process it
	 */
	private void invokeProcessingOfAllUsers() {

		if (!userConfigurationTreeList.isEmpty()) {
			for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
				startUserThread(userConfigurationTree);
				userConfigurationTree.setThreadStarted(true);
			}
		} else {
			LOGGER.info("No Configuration found");
		}
	}

	private void startUserThread(UserConfigurationTree userConfigurationTree) {

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.execute(() -> {
			try {
				LOGGER.info("Thread started for User====>" + userConfigurationTree.getUserEntity().getUserName());
				processThisUser(userConfigurationTree);
				Thread.sleep(SLEEP_DURATION_MS);
			} catch (Exception e) {
				LOGGER.error("Got Exception ", e);
			}

		});
		userConfigurationTree.setThreadStarted(true);
	}

	/**
	 *
	 */
	private void processThisUser(UserConfigurationTree userConfigurationTree) {

		while (true) {

			try {
				List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
						.getUserWiseCampaignList() != null
								? userConfigurationTree.getUserWiseCampaignList().stream()
										.filter(t -> t != null && ACTIVE.equalsIgnoreCase(t.getCampaignStatus()))
										.collect(Collectors.toList())
								: null;
				List<LeadInfoEntity> allLeadInfoList = new ArrayList<>();
				if (!CollectionUtils.isEmpty(campaignConfigurationTrees)) {
					for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
						if (campaignConfigurationTree != null
								&& null != campaignConfigurationTree.getCampaignWiseLeadList()
								&& !campaignConfigurationTree.getCampaignWiseLeadList().isEmpty()) {
							LOGGER.info("Got leads for " + userConfigurationTree.getUserEntity().getFirstName()
									+ " Size: " + campaignConfigurationTree.getCampaignWiseLeadList().size());
							allLeadInfoList.addAll(campaignConfigurationTree.getCampaignWiseLeadList());
						} else {
							LOGGER.info("No Leads in Campaign "
									+ campaignConfigurationTree.getCampaignEntity().getCampaignName());
						}
					}
				} else {
					LOGGER.info("No Campaigns for user " + userConfigurationTree.getUserEntity().getFirstName()
							+ " with active status");
				}

				if (allLeadInfoList.isEmpty()) {
					LOGGER.info(
							"No Lead Found in User [ " + userConfigurationTree.getUserEntity().getUserName() + " ]");
					Thread.sleep(SLEEP_DURATION_MS);
					continue;
				}
				LOGGER.info("getScheduledLeadsFromGivenPool");
				List<LeadInfoEntity> scheduledLeadsFromGivenPool = getScheduledLeadsFromGivenPool(allLeadInfoList);
				// 2. Process Users sequentially
				LOGGER.info("extractUsersFromThisLeadListAndProcess");
				extractStaticCampaignsFromTheseLeadsAndProcess(scheduledLeadsFromGivenPool);

				Thread.sleep(SLEEP_DURATION_MS);
			} catch (Exception e) {
				LOGGER.error("Got Exception ", e);
			}
		}
	}

	public void processUsersInAutoOrWAuto(Long userId, List<CampaignEntity> campaignEntityList,
			int totalNumberOfChannels) {

		List<Long> processedUsers1 = new ArrayList<>();

		UserConfigurationTree userConfigurationTree = userConfigurationTreeList.stream()
				.filter(t -> t.getUserEntity().getUserId() == userId).findFirst().orElse(null);
		if (userConfigurationTree == null) {
			LOGGER.info("userConfigurationTree is null ");
			return;
		}

		if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)) {

			Set<Long> processedCampaigns = new HashSet<>();
			int processedCampaignFlag;
//            if(processedUsers1.size() == userCount1){
//                //All Users processed
//                break;
//            }
			// Process U1->C1->L1 and break
			for (CampaignEntity campaignEntity : campaignEntityList) {
				if (processedCampaigns.size() == campaignEntityList.size()) {
					// All Campaigns for this user are processed
					processedUsers1.add(userId);
					break;
				}
				List<CampaignEntity> tempCampaignList = new ArrayList<>();
				tempCampaignList.add(campaignEntity);
				List<LeadInfoEntity> leadInfoEntityList = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId = leadInfoEntityList.stream()
						.filter(t -> t.getCampaignId() != null).collect(Collectors
								.groupingBy(LeadInfoEntity::getCampaignId, HashMap::new, Collectors.toList()));

				processedCampaignFlag = processCampaignsForDynamicScheme(tempCampaignList,
						leadInfoEntityListByCampaignId, totalNumberOfChannels);
				if (processedCampaignFlag < 0) {
					processedCampaigns.add(campaignEntity.getCampaignId());
				}
				LOGGER.info("Processed 1 batch of campaign " + campaignEntity.getCampaignName());
				break;
			}

		} else if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(ROUND_ROBIN)) {
			Set<Long> processedCampaigns = new HashSet<>();
			int processedCampaignFlag;
//            if(processedUsers1.size() == userCount1){
//                //All Users processed
//                break;
//            }
			// Process U1->C1->L1 and break
			CopyOnWriteArraySet<Long> processedCampaignList = userConfigurationTree.getProcessedCampaignList();

			for (CampaignEntity campaignEntity : campaignEntityList) {
				if (processedCampaigns.size() == campaignEntityList.size()) {
					// All Campaigns for this user are processed
					processedUsers1.add(userId);
					break;
				}
				if (processedCampaignList.size() == userConfigurationTree.getUserWiseCampaignList().size()) {
					processedCampaignList.clear();
				}

				if (processedCampaignList.contains(campaignEntity.getCampaignId())) {
					LOGGER.info(
							"Processed 1 batch for campaign " + campaignEntity.getCampaignName() + " now leaving..");
					continue;
				}
				List<CampaignEntity> tempCampaignList = new ArrayList<>();
				tempCampaignList.add(campaignEntity);
				List<LeadInfoEntity> leadInfoEntityList = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId = leadInfoEntityList.stream()
						.filter(t -> t.getCampaignId() != null).collect(Collectors
								.groupingBy(LeadInfoEntity::getCampaignId, HashMap::new, Collectors.toList()));
				processedCampaignFlag = processCampaignsForDynamicScheme(tempCampaignList,
						leadInfoEntityListByCampaignId, totalNumberOfChannels);
				processedCampaignList.add(campaignEntity.getCampaignId());
				if (processedCampaignFlag < 0) {
					processedCampaigns.add(campaignEntity.getCampaignId());
				}
				LOGGER.info("Processed 1 batch of campaign " + campaignEntity.getCampaignName());
				break;
			}

		} else if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(AUTO)) {

			int allCampaignSize = 0;
			for (CampaignEntity campaignEntity : campaignEntityList) {
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					allCampaignSize += leadInfoEntity.getCountOfNumbers();
				}
			}
			for (CampaignEntity campaignEntity : campaignEntityList) {
				int totalCampaignSize = 0;
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					totalCampaignSize += leadInfoEntity.getCountOfNumbers();
				}
				HashMap<Long, List<LeadInfoEntity>> tempLeadInfoEntitiesByCampaignId = new HashMap<>();
				tempLeadInfoEntitiesByCampaignId.put(campaignEntity.getCampaignId(), leadInfoEntityList1);
				if (allCampaignSize < 1) {
					LOGGER.error("allCampaignSize  is " + allCampaignSize + " please check configuration "
							+ campaignEntity.getCampaignName());
					continue;
				}
				int totalNumberOfChannelsForCampaign = (int) Math
						.ceil(totalNumberOfChannels * (totalCampaignSize / (double) allCampaignSize));
				CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree.getUserWiseCampaignList()
						.stream().filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
								.getCampaignId().longValue())
						.findFirst().orElse(null);
				if (campaignConfigurationTree != null && !campaignConfigurationTree.isThreadStarted()) {
					campaignConfigurationTree.setThreadStarted(true);
					Runnable task = new CampaignTaskManager(totalNumberOfChannelsForCampaign, campaignEntity, this,
							tempLeadInfoEntitiesByCampaignId, campaignConfigurationTree);
					new Thread(task).start();

				} else {
					LOGGER.info("Thread already started for Campaign " + campaignEntity.getCampaignName());
				}
			}

		} else if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(WEIGHTED_AUTO)) {

			int allCampaignSize = 0;
			for (CampaignEntity campaignEntity : campaignEntityList) {
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					if (leadInfoEntity.getLeadPriority() != null) {
						allCampaignSize += leadInfoEntity.getLeadPriority();
					}
				}
			}
			for (CampaignEntity campaignEntity : campaignEntityList) {
				int totalCampaignSize = 0;
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					totalCampaignSize += leadInfoEntity.getCountOfNumbers();
				}
				List<LeadInfoEntity> leadInfoEntityList = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId = leadInfoEntityList.stream()
						.filter(t -> t.getCampaignId() != null).collect(Collectors
								.groupingBy(LeadInfoEntity::getCampaignId, HashMap::new, Collectors.toList()));
				int totalNumberOfChannelsForCampaign = (totalNumberOfChannels / allCampaignSize) * totalCampaignSize;
				CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree.getUserWiseCampaignList()
						.stream().filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
								.getCampaignId().longValue())
						.findFirst().orElse(null);
				if (campaignConfigurationTree != null && !campaignConfigurationTree.isThreadStarted()) {
					campaignConfigurationTree.setThreadStarted(true);
					Runnable task = new CampaignTaskManager(totalNumberOfChannelsForCampaign, campaignEntity, this,
							leadInfoEntityListByCampaignId, campaignConfigurationTree);
					new Thread(task).start();

				} else {
					LOGGER.info("Thread already started for Campaign " + campaignEntity.getCampaignName());
				}
			}

		} else {
			LOGGER.warn("Invalid getChannelPriorityScheme for User "
					+ userConfigurationTree.getUserEntity().getChannelPriorityScheme());
		}

	}

	public void processUsersInAutoOrWAutoDynamicInStatic(Long userId, List<CampaignEntity> campaignEntityList,
			int totalNumberOfChannels) {

		List<Long> processedUsers1 = new ArrayList<>();

		UserConfigurationTree userConfigurationTree = userConfigurationTreeList.stream()
				.filter(t -> t.getUserEntity().getUserId() == userId).findFirst().orElse(null);
		if (userConfigurationTree == null) {
			LOGGER.info("userConfigurationTree is null ");
			return;
		}

		if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)) {

			Set<Long> processedCampaigns = new HashSet<>();
			int processedCampaignFlag;
//            if(processedUsers1.size() == userCount1){
//                //All Users processed
//                break;
//            }
			// Process U1->C1->L1 and break
			for (CampaignEntity campaignEntity : campaignEntityList) {
				if (processedCampaigns.size() == campaignEntityList.size()) {
					// All Campaigns for this user are processed
					processedUsers1.add(userId);
					break;
				}
				List<CampaignEntity> tempCampaignList = new ArrayList<>();
				tempCampaignList.add(campaignEntity);
				List<LeadInfoEntity> leadInfoEntityList = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId = leadInfoEntityList.stream()
						.filter(t -> t.getCampaignId() != null).collect(Collectors
								.groupingBy(LeadInfoEntity::getCampaignId, HashMap::new, Collectors.toList()));

				processedCampaignFlag = processCampaignsForDynamicScheme(tempCampaignList,
						leadInfoEntityListByCampaignId, totalNumberOfChannels);
				if (processedCampaignFlag < 0) {
					processedCampaigns.add(campaignEntity.getCampaignId());
				}
				LOGGER.info("Processed 1 batch of campaign " + campaignEntity.getCampaignName());
				break;
			}

		} else if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(ROUND_ROBIN)) {
			Set<Long> processedCampaigns = new HashSet<>();
			int processedCampaignFlag;
//            if(processedUsers1.size() == userCount1){
//                //All Users processed
//                break;
//            }
			// Process U1->C1->L1 and break TODO: SAURABH check if processedCampaignList
			// needs to be checked for completed campaings
			CopyOnWriteArraySet<Long> processedCampaignList = userConfigurationTree.getProcessedCampaignList();

			for (CampaignEntity campaignEntity : campaignEntityList) {
				if (processedCampaigns.size() == campaignEntityList.size()) {
					// All Campaigns for this user are processed
					processedUsers1.add(userId);
					break;
				}
				if (processedCampaignList.size() > campaignEntityList.size()) {
					for (Long campaignId : processedCampaignList) {
						CampaignConfigurationTree campConf = getCampaignConfigurationTreeForGivenCampaignId(campaignId);
						if ("Completed".equalsIgnoreCase(campConf.getCampaignEntity().getCampaignStatus())) {
							processedCampaignList.remove(campaignId);
							LOGGER.info("Campaign " + campaignId + " removed from processed list as it was completed");
						}
					}
				}

				if (processedCampaignList.size() >= campaignEntityList.size()) {
					processedCampaignList.clear();
				}

				if (processedCampaignList.contains(campaignEntity.getCampaignId())) {
					LOGGER.info(
							"Processed 1 batch for campaign " + campaignEntity.getCampaignName() + " now leaving..");
					continue;
				}
				List<CampaignEntity> tempCampaignList = new ArrayList<>();
				tempCampaignList.add(campaignEntity);
				List<LeadInfoEntity> leadInfoEntityList = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId = leadInfoEntityList.stream()
						.filter(t -> t.getCampaignId() != null).collect(Collectors
								.groupingBy(LeadInfoEntity::getCampaignId, HashMap::new, Collectors.toList()));
				processedCampaignFlag = processCampaignsForDynamicScheme(tempCampaignList,
						leadInfoEntityListByCampaignId, totalNumberOfChannels);
				processedCampaignList.add(campaignEntity.getCampaignId());
				if (processedCampaignFlag < 0) {
					processedCampaigns.add(campaignEntity.getCampaignId());
				}
				LOGGER.info("Processed 1 batch of campaign " + campaignEntity.getCampaignName());
				break;
			}

		} else if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(AUTO)) {

			int allCampaignSize = 0;
			for (CampaignEntity campaignEntity : campaignEntityList) {
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					allCampaignSize += leadInfoEntity.getCountOfNumbers();
				}
			}
			for (CampaignEntity campaignEntity : campaignEntityList) {
				int totalCampaignSize = 0;
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					totalCampaignSize += leadInfoEntity.getCountOfNumbers();
				}
				HashMap<Long, List<LeadInfoEntity>> tempLeadInfoEntitiesByCampaignId = new HashMap<>();
				tempLeadInfoEntitiesByCampaignId.put(campaignEntity.getCampaignId(), leadInfoEntityList1);
				if (allCampaignSize < 1) {
					LOGGER.error("allCampaignSize  is " + allCampaignSize + " please check configuration "
							+ campaignEntity.getCampaignName());
					continue;
				}
				int totalNumberOfChannelsForCampaign = (int) Math
						.ceil(totalNumberOfChannels * (totalCampaignSize / (double) allCampaignSize));
				CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree.getUserWiseCampaignList()
						.stream().filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
								.getCampaignId().longValue())
						.findFirst().orElse(null);
				if (campaignConfigurationTree != null && !campaignConfigurationTree.isThreadStarted()) {
					campaignConfigurationTree.setThreadStarted(true);
					Runnable task = new CampaignTaskManager(totalNumberOfChannelsForCampaign, campaignEntity, this,
							tempLeadInfoEntitiesByCampaignId, campaignConfigurationTree);
					new Thread(task).start();

				} else {
					LOGGER.info("Thread already started for Campaign " + campaignEntity.getCampaignName());
				}
			}

		} else if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(WEIGHTED_AUTO)) {

			int allCampaignSize = 0;
			for (CampaignEntity campaignEntity : campaignEntityList) {
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					if (leadInfoEntity.getLeadPriority() != null) {
						allCampaignSize += leadInfoEntity.getLeadPriority();
					}
				}
			}
			for (CampaignEntity campaignEntity : campaignEntityList) {
				int totalCampaignSize = 0;
				List<LeadInfoEntity> leadInfoEntityList1 = leadInfoRepository
						.getLeadInfoEntitiesByCampaignId(campaignEntity.getCampaignId());
				for (LeadInfoEntity leadInfoEntity : leadInfoEntityList1) {
					totalCampaignSize += leadInfoEntity.getCountOfNumbers();
				}
				List<LeadInfoEntity> leadInfoEntityList = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId = leadInfoEntityList.stream()
						.filter(t -> t.getCampaignId() != null).collect(Collectors
								.groupingBy(LeadInfoEntity::getCampaignId, HashMap::new, Collectors.toList()));
				int totalNumberOfChannelsForCampaign = (totalNumberOfChannels / allCampaignSize) * totalCampaignSize;
				CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree.getUserWiseCampaignList()
						.stream().filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
								.getCampaignId().longValue())
						.findFirst().orElse(null);
				if (campaignConfigurationTree != null && !campaignConfigurationTree.isThreadStarted()) {
					campaignConfigurationTree.setThreadStarted(true);
					Runnable task = new CampaignTaskManager(totalNumberOfChannelsForCampaign, campaignEntity, this,
							leadInfoEntityListByCampaignId, campaignConfigurationTree);
					new Thread(task).start();

				} else {
					LOGGER.info("Thread already started for Campaign " + campaignEntity.getCampaignName());
				}
			}

		} else {
			LOGGER.warn("Invalid getChannelPriorityScheme for User "
					+ userConfigurationTree.getUserEntity().getChannelPriorityScheme());
		}

	}

	private void processStaticPartOfThisUser(String userChannelAllocationScheme,
			List<UserConfigurationTree> userConfigurationTrees) {
		LOGGER.info("Starting Thread for processing [ " + userChannelAllocationScheme + " ] users");
		while (true) {
			LOGGER.info("processStaticPartOfThisUser Thread Running...");
			List<LeadInfoEntity> leadInfoEntityList;
			CopyOnWriteArrayList<LeadInfoEntity> allLeadInfoList = new CopyOnWriteArrayList<>();
			try {
				if (userChannelAllocationScheme.equalsIgnoreCase(DYNAMIC_CHANNEL_ALLOCATION_SCHEME)) {
					// no-op
					LOGGER.info(
							"No-op " + DYNAMIC_CHANNEL_ALLOCATION_SCHEME + " found in place of static, please check");

				} else if (userChannelAllocationScheme.equalsIgnoreCase(STATIC_CHANNEL_ALLOCATION_SCHEME)) {

					for (UserConfigurationTree userConfigurationTree : userConfigurationTrees) {
						List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
								.getUserWiseCampaignList() != null ? userConfigurationTree.getUserWiseCampaignList()
										.stream()
										.filter(t -> t != null && ACTIVE.equalsIgnoreCase(t.getCampaignStatus()))
										.collect(Collectors.toList()) : null;
						if (!CollectionUtils.isEmpty(campaignConfigurationTrees)) {
							for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
								if (campaignConfigurationTree != null
										&& null != campaignConfigurationTree.getCampaignWiseLeadList()
										&& !campaignConfigurationTree.getCampaignWiseLeadList().isEmpty()) {
									LOGGER.info("Got leads for " + userConfigurationTree.getUserEntity().getFirstName()
											+ " Size: " + campaignConfigurationTree.getCampaignWiseLeadList().size());
									allLeadInfoList.addAll(campaignConfigurationTree.getCampaignWiseLeadList());
								} else {
									LOGGER.info("No Leads in Campaign "
											+ campaignConfigurationTree.getCampaignEntity().getCampaignName());
								}
							}
						} else {
							LOGGER.info("No Campaigns in [ " + STATIC_CHANNEL_ALLOCATION_SCHEME + "] for user "
									+ userConfigurationTree.getUserEntity().getFirstName() + " with active status");
						}
					}
					if (allLeadInfoList.isEmpty()) {
						LOGGER.info("No Lead Found");
						Thread.sleep(SLEEP_DURATION_MS);
						continue;
					}
					LOGGER.info("getScheduledLeadsFromGivenPool");
					leadInfoEntityList = getScheduledLeadsFromGivenPool(allLeadInfoList);
					// 2. Process Users sequentially
					LOGGER.info("extractUsersFromThisLeadListAndProcess");
					extractStaticCampaignsFromTheseLeadsAndProcess(leadInfoEntityList);

				} else {
					// no-op
				}
				Thread.sleep(SLEEP_DURATION_MS);
			} catch (Exception e) {
				LOGGER.error("Got Exception ", e);
			}

		}
	}

	private void extractStaticCampaignsFromTheseLeadsAndProcess(List<LeadInfoEntity> leadInfoEntityList) {
		LOGGER.info("Start extractStaticCampaignsFromTheseLeadsAndProcess");
		HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId = leadInfoEntityList.stream()
				.filter(t -> t.getCampaignId() != null)
				.collect(Collectors.groupingBy(LeadInfoEntity::getCampaignId, HashMap::new, Collectors.toList()));
		List<CampaignEntity> campaignEntities = new ArrayList<>();
		for (Map.Entry<Long, List<LeadInfoEntity>> entry : leadInfoEntityListByCampaignId.entrySet()) {
			LOGGER.info("leadInfoEntityListByCampaignId[entry.getKey()] = " + entry.getKey());
			CampaignEntity campaignEntity = campaignRepository.findById(entry.getKey()).orElse(null);
			if (campaignEntity != null) {
				LOGGER.info("extractUsersFromThisLeadListAndProcess CampaignId = " + campaignEntity.getCampaignId());
				campaignEntities.add(campaignEntity);
			} else {
				LOGGER.info("CampaignEntity returned null for ID " + entry.getKey());
			}
		}
		if (campaignEntities.isEmpty())
			return;
		// Spawn Threads per campaign as these are statically allocated and can work
		// independently
		campaignEntities.forEach(campaignEntity -> {
			CampaignConfigurationTree campaignConfigurationTree = getActiveCampaignConfigurationTreeForGivenCampaignId(
					campaignEntity.getCampaignId());
			if (campaignConfigurationTree == null)
				return;
			boolean isThreadStarted = campaignConfigurationTree.isThreadStarted();
			if (!isThreadStarted) {
				LOGGER.info("Going to Start Thread for Campaign " + campaignEntity.getCampaignName());
				campaignConfigurationTree.setThreadStarted(true);
				CampaignConfigurationTree campConf = getCampaignConfigurationTreeForGivenCampaignId(
						campaignEntity.getCampaignId());
				campConf.setThreadStarted(true);
				startCampaignThread(campaignEntity, leadInfoEntityListByCampaignId);
				try {
					Thread.sleep(SLEEP_DURATION_MS);
				} catch (Exception e) {
				}
			}
		});
	}

	private void startCampaignThread(CampaignEntity campaignEntity,
			HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId) {
		LOGGER.info("startCampaignThread1");

		CampaignConfigurationTree campaignConfigurationTree = getActiveCampaignConfigurationTreeForGivenCampaignId(
				campaignEntity.getCampaignId());
		boolean campaignCompleted = campaignConfigurationTree == null;
		Runnable task = () -> {
			while (!campaignCompleted && campaignConfigurationTree.isThreadStarted()) {
				try {
					LOGGER.info("startCampaignThread2");
					List<CampaignEntity> campaignEntityList = new ArrayList<>();
					campaignEntityList.add(campaignEntity);
					LOGGER.info("Thread started for Campaign " + campaignEntity.getCampaignName());
					LOGGER.info("startCampaignThread4");
					processCampaignsForStaticScheme(campaignEntityList, leadInfoEntityListByCampaignId);
					if (getActiveCampaignConfigurationTreeForGivenCampaignId(campaignEntity.getCampaignId()) == null) {
						// This means after processing Campaign Got Completed
						CampaignConfigurationTree ctree = getCampaignConfigurationTreeForGivenCampaignId(
								campaignEntity.getCampaignId());
						LOGGER.info("Thread Set to false for campaign - " + campaignEntity.getCampaignId());
						ctree.setThreadStarted(false);
						return;
					}
					Thread.sleep(SLEEP_DURATION_MS);
					LOGGER.info("Sleeping... " + campaignEntity.getCampaignName());
				} catch (Exception e) {
					LOGGER.error("Got Exception ", e);
				}
			}
		};
		new Thread(task).start();
	}

	private void processCampaigns(List<CampaignEntity> campaignEntities,
			HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId) {
		LOGGER.info("Inside processCampaigns");
		boolean allProcessed = false;
		while (!allProcessed) {
			LOGGER.info("Campaign Thread running ....");

			// For each Campaign
			int processedCampaignSize = 0;
			for (CampaignEntity campaignEntity : campaignEntities) {
				CampaignConfigurationTree campaignCTree = getActiveCampaignConfigurationTreeForGivenCampaignId(
						campaignEntity.getCampaignId());
				if (campaignCTree == null) {
					LOGGER.info("Campaign Completed - " + campaignEntity.getCampaignId());
					allProcessed = true;
					continue;
				}
				Set<Long> processedLeads = new HashSet<>();
				CopyOnWriteArrayList<LeadInfoEntity> highPriorityLeads = new CopyOnWriteArrayList<>();
				CopyOnWriteArrayList<LeadInfoEntity> mediumPriorityLeads = new CopyOnWriteArrayList<>();
				CopyOnWriteArrayList<LeadInfoEntity> lowPriorityLeads = new CopyOnWriteArrayList<>();
				CopyOnWriteArrayList<LeadInfoEntity> allLeads = new CopyOnWriteArrayList<>();

				LOGGER.info("Looping for " + campaignEntity.getCampaignName() + " inside processCampaigns");
				// Bucket up HIGH/MEDIUM/LOW Leads
				List<LeadInfoEntity> leadInfoEntities = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				LOGGER.info("Extracted Leads : " + leadInfoEntities.size() + " for campaign "
						+ campaignEntity.getCampaignName());
				if (CollectionUtils.isEmpty(leadInfoEntities)) {
					LOGGER.info("leadInfoEntities is null for campaign " + campaignEntity.getCampaignName());
					continue;
				}
				for (LeadInfoEntity leadInfoEntity : leadInfoEntities) {
					if (HIGH.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
						LOGGER.info("Captured High Priority Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						highPriorityLeads.add(leadInfoEntity);
					} else if (MEDIUM.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
						LOGGER.info("Captured Medium Priority Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						mediumPriorityLeads.add(leadInfoEntity);
					} else if (LOW.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
						LOGGER.info("Captured Low Priority Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						lowPriorityLeads.add(leadInfoEntity);
					} else {
						LOGGER.info("Captured Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						allLeads.add(leadInfoEntity);
					}
				}
				// Take the total Static Channels in this campaign
				Integer totalNumberOfPhoneNumbersThatCanBePublished = 100;// TODO
				totalNumberOfPhoneNumbersThatCanBePublished *= MULTIPYING_FACTOR;
				LOGGER.info(
						"totalNumberOfPhoneNumbersThatCanBePublished =  " + totalNumberOfPhoneNumbersThatCanBePublished
								+ " Campaign " + campaignEntity.getCampaignName());
				if (totalNumberOfPhoneNumbersThatCanBePublished == 0) {
					LOGGER.info("totalNumberOfPhoneNumbersThatCanBePublished is 0 from campaign "
							+ campaignEntity.getCampaignName()
							+ " check campaign_channels_allocation.NUMBER_OF_CHANNELS in DB");
					continue;
				}

				if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)) {
					LOGGER.info("ChannelPriorityScheme is sequential for campaign " + campaignEntity.getCampaignName());

					// Process All High priority leads
					LOGGER.info("Processing All High priority leads for campaign " + campaignEntity.getCampaignName());
					while (true) {
						LOGGER.info("Working on High Priority Leads..");
						int processFlag = processLeads(highPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
								false);
						if (processFlag < 0) {
							LOGGER.info(
									"Processed High Priority leads for Campaign " + campaignEntity.getCampaignName());
							break;
						}
						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {

						}
					}
					// Process All Medium priority leads
					LOGGER.info(
							"Processing All Medium priority leads for campaign " + campaignEntity.getCampaignName());
					while (true) {
						LOGGER.info("Working on Medium Priority Leads..");
						int processFlag = processLeads(mediumPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
								false);
						if (processFlag < 0) {
							LOGGER.info(
									"Processed Medium Priority leads for Campaign " + campaignEntity.getCampaignName());
							break;
						}
						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {

						}
					}
					// Process All Low priority leads
					LOGGER.info("Processing All Low priority leads for campaign " + campaignEntity.getCampaignName());

					while (true) {
						LOGGER.info("Working on Low Priority Leads..");
						int processFlag = processLeads(lowPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
								false);
						if (processFlag < 0) {
							LOGGER.info(
									"Processed Low Priority leads for Campaign " + campaignEntity.getCampaignName());
							break;
						}
						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {

						}
					}
					processedCampaignSize += 1;

				} else if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(ROUND_ROBIN)) {
					LOGGER.info(
							"ChannelPriorityScheme is ROUND_ROBIN for campaign " + campaignEntity.getCampaignName());

					allLeads.addAll(highPriorityLeads);
					allLeads.addAll(mediumPriorityLeads);
					allLeads.addAll(lowPriorityLeads);
					// shuffle here to give all equal opportunity
					Collections.shuffle(allLeads);
					while (true) {
						for (LeadInfoEntity leadInfoEntity : allLeads) {
							int ret = 0;
							List<LeadInfoEntity> tempLeadInfoEntityList = new ArrayList<>();
							tempLeadInfoEntityList.add(leadInfoEntity);
							// if this is not processed then only try for processing
							if (!processedLeads.contains(leadInfoEntity.getLeadId())) {
								ret = processLeads(tempLeadInfoEntityList, totalNumberOfPhoneNumbersThatCanBePublished,
										true);
							}
							if (ret < 0) {
								// If all processed then add this to processed list
								processedLeads.add(leadInfoEntity.getLeadId());
							}
						}
						// if all leads are processed then break from this loop
						if (allLeads.size() == processedLeads.size()) {
							LOGGER.info(
									"Processed All leads for Campaign " + campaignEntity.getCampaignName() + " in RR ");
							processedCampaignSize += 1;
							break;
						}
						UserConfigurationTree userConfigurationTree = null;

						for (UserConfigurationTree userConfigurationTree1 : userConfigurationTreeList) {
							List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree1
									.getUserWiseCampaignList() != null ? userConfigurationTree1
											.getUserWiseCampaignList().stream()
											.filter(t -> t != null && ACTIVE.equalsIgnoreCase(t.getCampaignStatus()))
											.collect(Collectors.toList()) : null;
							for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
								if (campaignConfigurationTree.getCampaignEntity().getCampaignId()
										.longValue() == campaignEntity.getCampaignId().longValue()) {
									userConfigurationTree = userConfigurationTree1;
									break;
								}
							}
							if (userConfigurationTree != null) {
								break;
							}
						}

						if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme()
								.equalsIgnoreCase(SEQUENTIAL)) {
							LOGGER.info("Processed a batch and leaving  as User Channel Priority Scheme is:  "
									+ userConfigurationTree.getUserEntity().getChannelPriorityScheme());
							break;
						}

						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {
						}
					}
				} else if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(AUTO)) {
					LOGGER.info("Processing campaign " + campaignEntity.getCampaignName() + " in AUTO mode");
					Integer totalLeadSize = 0;
					if (CollectionUtils.isEmpty(allLeads)) {
						LOGGER.info("Nothing to process in this AUTO call for " + campaignEntity.getCampaignName());
						continue;
					}

					UserConfigurationTree userConfigurationTree = userConfigurationTreeList != null
							? userConfigurationTreeList.stream()
									.filter(t -> t.getUserEntity().getUserId() == campaignEntity.getUserId())
									.findFirst().orElse(null)
							: null;
					CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree != null
							? userConfigurationTree.getUserWiseCampaignList().stream()
									.filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
											.getCampaignId().longValue())
									.findFirst().orElse(null)
							: null;

					CopyOnWriteArrayList<Long> processedLeadList = campaignConfigurationTree != null
							? campaignConfigurationTree.getProcessedLeadList()
							: null;
					CopyOnWriteArrayList<Long> threadStartLeadList = campaignConfigurationTree != null
							? campaignConfigurationTree.getThreadStartLeadList()
							: null;

					checkforProcessedLeads(allLeads, processedLeadList, campaignConfigurationTree);
					for (LeadInfoEntity leadInfoEntity : allLeads) {
						totalLeadSize += leadInfoEntity.getCountOfNumbers();
					}
					for (LeadInfoEntity leadInfoEntity : allLeads) {

						if (processedLeadList.size() >= allLeads.size()) {
							LOGGER.info("All leads are processed atleast once so clearing processedLeadList ");
							processedLeadList.clear();
						} else {
							LOGGER.info("Processed Lead Size [" + processedLeadList.size() + "] and allLead size ["
									+ allLeads.size() + "]");
						}
						if (processedLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("This lead is already processed once hence ignoring.. "
									+ leadInfoEntity.getLeadName() + " campaign priority Scheme is "
									+ campaignEntity.getChannelPriorityScheme());
							LOGGER.info("Not Clearing Lead " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + " from processedLeadList as it contains :");
							for (LeadInfoEntity leadInfoEntity1 : allLeads) {
								LOGGER.info(
										"Lead " + leadInfoEntity1.getLeadId() + "=>" + leadInfoEntity1.getLeadName());
							}

							LOGGER.info("Total Lead Ids as in processedLeadList are:");
							for (Long leadId : processedLeadList) {
								LOGGER.info(leadId);
							}
							continue;
						}

						if (totalLeadSize < 1) {
							LOGGER.error("totalLeadSize is " + totalLeadSize + " please check configuration "
									+ campaignEntity.getCampaignName());
							continue;
						}
						int totalNumberOfChannelsApplicable = (int) Math
								.ceil(totalNumberOfPhoneNumbersThatCanBePublished
										* (leadInfoEntity.getCountOfNumbers() / (double) totalLeadSize));
						if (threadStartLeadList != null && !threadStartLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("Starting LeadTaskManager for Lead [" + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + "] with totalNumberOfChannelsApplicable ="
									+ totalNumberOfChannelsApplicable);
							threadStartLeadList.add(leadInfoEntity.getLeadId());
							Runnable task = new LeadTaskManager(totalNumberOfChannelsApplicable, leadInfoEntity, this,
									threadStartLeadList);
							new Thread(task).start();
						} else {
							LOGGER.info("Thread already started for " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName());
						}
						if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme()
								.equalsIgnoreCase(SEQUENTIAL)
								&& !processedLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("Added lead [" + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + "] as processed as User Priority is "
									+ userConfigurationTree.getUserEntity().getChannelPriorityScheme() + " user name "
									+ userConfigurationTree.getUserEntity().getFirstName());

							processedLeadList.add(leadInfoEntity.getLeadId());
						}

					}
					// if all leads are processed then break from this loop
					if (allLeads.size() == processedLeads.size()) {
						LOGGER.info(
								"Processed All leads for Campaign " + campaignEntity.getCampaignName() + " in AUTO ");
						processedCampaignSize += 1;
						break;
					}

					try {
						Thread.sleep(SLEEP_DURATION_MS);
					} catch (Exception e) {

					}
				} else if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(WEIGHTED_AUTO)) {
					Integer totalLeadSize = 0;
					UserConfigurationTree userConfigurationTree = userConfigurationTreeList != null
							? userConfigurationTreeList.stream()
									.filter(t -> t.getUserEntity().getUserId() == campaignEntity.getUserId())
									.findFirst().orElse(null)
							: null;
					CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree != null
							? userConfigurationTree.getUserWiseCampaignList().stream()
									.filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
											.getCampaignId().longValue())
									.findFirst().orElse(null)
							: null;

					CopyOnWriteArrayList<Long> processedLeadList = campaignConfigurationTree != null
							? campaignConfigurationTree.getProcessedLeadList()
							: null;
					CopyOnWriteArrayList<Long> threadStartLeadList = campaignConfigurationTree != null
							? campaignConfigurationTree.getThreadStartLeadList()
							: null;

					checkforProcessedLeads(allLeads, processedLeadList, campaignConfigurationTree);
					for (LeadInfoEntity leadInfoEntity : allLeads) {
						if (leadInfoEntity.getLeadPriority() != null) {
							totalLeadSize += leadInfoEntity.getLeadPriority();
						} else {
							LOGGER.error("Lead " + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
									+ " does not have leadPriority");
						}
					}
					for (LeadInfoEntity leadInfoEntity : allLeads) {
						if (processedLeadList.size() >= allLeads.size()) {
							LOGGER.info("All leads are processed atleast once so clearing processedLeadList ");
							processedLeadList.clear();
						}
						if (processedLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("This lead is already processed once hence ignoring.. "
									+ leadInfoEntity.getLeadName() + " campaign priority Scheme is "
									+ campaignEntity.getChannelPriorityScheme());
							LOGGER.info("Not Clearing Lead " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + " from processedLeadList as it contains :");
							for (LeadInfoEntity leadInfoEntity1 : allLeads) {
								LOGGER.info(
										"Lead " + leadInfoEntity1.getLeadId() + "=>" + leadInfoEntity1.getLeadName());
							}

							LOGGER.info("Total Lead Ids as in processedLeadList are:");
							for (Long leadId : processedLeadList) {
								LOGGER.info(leadId);
							}
							continue;
						}

						if (totalLeadSize < 1) {
							LOGGER.error("totalLeadSize is " + totalLeadSize + " please check configuration"
									+ campaignConfigurationTree.getCampaignEntity().getCampaignName());
							continue;
						}

						int totalNumberOfChannelsApplicable = (int) Math
								.ceil(totalNumberOfPhoneNumbersThatCanBePublished
										* (leadInfoEntity.getLeadPriority() / (double) totalLeadSize));
						if (threadStartLeadList != null && !threadStartLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("Starting LeadTaskManager for Lead [" + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + "] with totalNumberOfChannelsApplicable ="
									+ totalNumberOfChannelsApplicable);
							threadStartLeadList.add(leadInfoEntity.getLeadId());
							Runnable task = new LeadTaskManager(totalNumberOfChannelsApplicable, leadInfoEntity, this,
									threadStartLeadList);
							new Thread(task).start();
						} else {
							LOGGER.info("Thread already started for " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName());
						}

						if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme()
								.equalsIgnoreCase(SEQUENTIAL)
								&& !processedLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("Added lead [" + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + "] as processed as User Priority is "
									+ userConfigurationTree.getUserEntity().getChannelPriorityScheme() + " user name "
									+ userConfigurationTree.getUserEntity().getFirstName());

							processedLeadList.add(leadInfoEntity.getLeadId());
						}
					}
					// if all leads are processed then break from this loop
					if (allLeads.size() == processedLeads.size()) {
						processedCampaignSize += 1;
						LOGGER.info("Processed All leads for Campaign " + campaignEntity.getCampaignName()
								+ " in WEIGHTED_AUTO ");
						break;
					}

					try {
						Thread.sleep(SLEEP_DURATION_MS);
					} catch (InterruptedException e) {
						// no-op
					}
				}
				LOGGER.info("Looping for Campaign " + campaignEntity.getCampaignName());
			}

			try {
				Thread.sleep(SLEEP_DURATION_MS);
			} catch (InterruptedException e) {
			}
		}

	}

	private void processCampaignsForStaticScheme(List<CampaignEntity> campaignEntities,
			HashMap<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId) {
		LOGGER.info("Inside processCampaigns");
		boolean allProcessed = false;
		CampaignEntity campaign = campaignEntities.get(0);
		CampaignConfigurationTree campConf = getActiveCampaignConfigurationTreeForGivenCampaignId(
				campaign.getCampaignId());
		while (!allProcessed && campConf != null && campConf.isThreadStarted()) {
			LOGGER.info("Campaign Thread running ....");

			// For each Campaign
			int processedCampaignSize = 0;
			for (CampaignEntity campaignEntity : campaignEntities) {
				CampaignConfigurationTree campaignCTree = getActiveCampaignConfigurationTreeForGivenCampaignId(
						campaignEntity.getCampaignId());
				if (campaignCTree == null) {
					LOGGER.info("Campaign Completed - " + campaignEntity.getCampaignId());
					allProcessed = true;
					continue;
				}
				Set<Long> processedLeads = new HashSet<>();
				CopyOnWriteArrayList<LeadInfoEntity> highPriorityLeads = new CopyOnWriteArrayList<>();
				CopyOnWriteArrayList<LeadInfoEntity> mediumPriorityLeads = new CopyOnWriteArrayList<>();
				CopyOnWriteArrayList<LeadInfoEntity> lowPriorityLeads = new CopyOnWriteArrayList<>();
				CopyOnWriteArrayList<LeadInfoEntity> allLeads = new CopyOnWriteArrayList<>();

				LOGGER.info("Looping for " + campaignEntity.getCampaignName() + " inside processCampaigns");
				// Bucket up HIGH/MEDIUM/LOW Leads
				List<LeadInfoEntity> leadInfoEntities = getScheduledLeadsFromGivenCampaign(
						campaignEntity.getCampaignId());
				LOGGER.info("Extracted Leads : " + leadInfoEntities.size() + " for campaign "
						+ campaignEntity.getCampaignName());
				if (CollectionUtils.isEmpty(leadInfoEntities)) {
					LOGGER.info("leadInfoEntities is null for campaign " + campaignEntity.getCampaignName());
					continue;
				}
				for (LeadInfoEntity leadInfoEntity : leadInfoEntities) {
					if (HIGH.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
						LOGGER.info("Captured High Priority Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						highPriorityLeads.add(leadInfoEntity);
					} else if (MEDIUM.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
						LOGGER.info("Captured Medium Priority Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						mediumPriorityLeads.add(leadInfoEntity);
					} else if (LOW.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
						LOGGER.info("Captured Low Priority Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						lowPriorityLeads.add(leadInfoEntity);
					} else {
						LOGGER.info("Captured Lead " + leadInfoEntity.getLeadName() + " for "
								+ campaignEntity.getCampaignName());
						allLeads.add(leadInfoEntity);
					}
				}
				// Take the total Static Channels in this campaign
				Integer totalNumberOfPhoneNumbersThatCanBePublished = 10;
				totalNumberOfPhoneNumbersThatCanBePublished *= MULTIPYING_FACTOR;
				LOGGER.info(
						"totalNumberOfPhoneNumbersThatCanBePublished =  " + totalNumberOfPhoneNumbersThatCanBePublished
								+ " Campaign " + campaignEntity.getCampaignName());
				if (totalNumberOfPhoneNumbersThatCanBePublished == 0) {
					LOGGER.info("totalNumberOfPhoneNumbersThatCanBePublished is 0 from campaign "
							+ campaignEntity.getCampaignName()
							+ " check campaign_channels_allocation.NUMBER_OF_CHANNELS in DB");
					continue;
				}

				if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)) {
					LOGGER.info("ChannelPriorityScheme is sequential for campaign " + campaignEntity.getCampaignName());

					// Process All High priority leads
					LOGGER.info("Processing All High priority leads for campaign " + campaignEntity.getCampaignName());
					while (campConf.isThreadStarted()) {
						LOGGER.info("Working on High Priority Leads..");
						int processFlag = processLeads(highPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
								false);
						if (processFlag < 0) {
							LOGGER.info(
									"Processed High Priority leads for Campaign " + campaignEntity.getCampaignName());
							break;
						}
						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {

						}
					}
					// Process All Medium priority leads
					LOGGER.info(
							"Processing All Medium priority leads for campaign " + campaignEntity.getCampaignName());
					while (campConf.isThreadStarted()) {
						LOGGER.info("Working on Medium Priority Leads..");
						int processFlag = processLeads(mediumPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
								false);
						if (processFlag < 0) {
							LOGGER.info(
									"Processed Medium Priority leads for Campaign " + campaignEntity.getCampaignName());
							break;
						}
						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {

						}
					}
					// Process All Low priority leads
					LOGGER.info("Processing All Low priority leads for campaign " + campaignEntity.getCampaignName());

					while (campConf.isThreadStarted()) {
						LOGGER.info("Working on Low Priority Leads..");
						int processFlag = processLeads(lowPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
								false);
						if (processFlag < 0) {
							LOGGER.info(
									"Processed Low Priority leads for Campaign " + campaignEntity.getCampaignName());
							break;
						}
						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {

						}
					}
					processedCampaignSize += 1;

				} else if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(ROUND_ROBIN)) {
					LOGGER.info(
							"ChannelPriorityScheme is ROUND_ROBIN for campaign " + campaignEntity.getCampaignName());

					allLeads.addAll(highPriorityLeads);
					allLeads.addAll(mediumPriorityLeads);
					allLeads.addAll(lowPriorityLeads);
					// shuffle here to give all equal opportunity
					Collections.shuffle(allLeads);
					while (campConf.isThreadStarted()) {
						for (LeadInfoEntity leadInfoEntity : allLeads) {
							int ret = 0;
							List<LeadInfoEntity> tempLeadInfoEntityList = new ArrayList<>();
							tempLeadInfoEntityList.add(leadInfoEntity);
							// if this is not processed then only try for processing
							if (!processedLeads.contains(leadInfoEntity.getLeadId())) {
								ret = processLeads(tempLeadInfoEntityList, totalNumberOfPhoneNumbersThatCanBePublished,
										true);
							}
							if (ret < 0) {
								// If all processed then add this to processed list
								processedLeads.add(leadInfoEntity.getLeadId());
							}
						}
						// if all leads are processed then break from this loop
						if (allLeads.size() == processedLeads.size()) {
							LOGGER.info(
									"Processed All leads for Campaign " + campaignEntity.getCampaignName() + " in RR ");
							processedCampaignSize += 1;
							break;
						}
						UserConfigurationTree userConfigurationTree = null;
						for (UserConfigurationTree configurationTree : userConfigurationTreeList) {
							List<CampaignConfigurationTree> campaignConfigurationTrees = configurationTree
									.getUserWiseCampaignList() != null ? configurationTree.getUserWiseCampaignList()
											.stream()
											.filter(t -> t != null && ACTIVE.equalsIgnoreCase(t.getCampaignStatus()))
											.collect(Collectors.toList()) : null;
							for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
								if (campaignConfigurationTree.getCampaignEntity().getCampaignId()
										.longValue() == campaignEntity.getCampaignId().longValue()) {
									userConfigurationTree = configurationTree;
									break;
								}
							}
							if (userConfigurationTree != null) {
								break;
							}
						}
						if (userConfigurationTree != null) {
							break;
						}

						if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme()
								.equalsIgnoreCase(SEQUENTIAL)) {
							LOGGER.info("Processed a batch and leaving  as User Channel Priority Scheme is:  "
									+ userConfigurationTree.getUserEntity().getChannelPriorityScheme());
							break;
						}

						try {
							Thread.sleep(SLEEP_DURATION_MS);
						} catch (Exception e) {
						}
					}
				} else if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(AUTO)) {
					LOGGER.info("Processing campaign " + campaignEntity.getCampaignName() + " in AUTO mode");
					Integer totalLeadSize = 0;
					if (CollectionUtils.isEmpty(allLeads)) {
						LOGGER.info("Nothing to process in this AUTO call for " + campaignEntity.getCampaignName());
						continue;
					}

					UserConfigurationTree userConfigurationTree = userConfigurationTreeList != null
							? userConfigurationTreeList.stream()
									.filter(t -> t.getUserEntity().getUserId() == campaignEntity.getUserId())
									.findFirst().orElse(null)
							: null;
					CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree != null
							? userConfigurationTree.getUserWiseCampaignList().stream()
									.filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
											.getCampaignId().longValue())
									.findFirst().orElse(null)
							: null;

					CopyOnWriteArrayList<Long> processedLeadList = campaignConfigurationTree != null
							? campaignConfigurationTree.getProcessedLeadList()
							: null;
					CopyOnWriteArrayList<Long> threadStartLeadList = campaignConfigurationTree != null
							? campaignConfigurationTree.getThreadStartLeadList()
							: null;

					checkforProcessedLeads(allLeads, processedLeadList, campaignConfigurationTree);
					for (LeadInfoEntity leadInfoEntity : allLeads) {
						totalLeadSize += leadInfoEntity.getCountOfNumbers();
					}
					for (LeadInfoEntity leadInfoEntity : allLeads) {

						if (processedLeadList.size() >= allLeads.size()) {
							LOGGER.info("All leads are processed atleast once so clearing processedLeadList ");
							processedLeadList.clear();
						} else {
							LOGGER.info("Processed Lead Size [" + processedLeadList.size() + "] and allLead size ["
									+ allLeads.size() + "]");
						}
						if (processedLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("This lead is already processed once hence ignoring.. "
									+ leadInfoEntity.getLeadName() + " campaign priority Scheme is "
									+ campaignEntity.getChannelPriorityScheme());
							LOGGER.info("Not Clearing Lead " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + " from processedLeadList as it contains :");
							for (LeadInfoEntity leadInfoEntity1 : allLeads) {
								LOGGER.info(
										"Lead " + leadInfoEntity1.getLeadId() + "=>" + leadInfoEntity1.getLeadName());
							}

							LOGGER.info("Total Lead Ids as in processedLeadList are:");
							for (Long leadId : processedLeadList) {
								LOGGER.info(leadId);
							}
							continue;
						}

						if (totalLeadSize < 1) {
							LOGGER.error("totalLeadSize is " + totalLeadSize + " please check configuration "
									+ campaignEntity.getCampaignName());
							continue;
						}
						int totalNumberOfChannelsApplicable = (int) Math
								.ceil(totalNumberOfPhoneNumbersThatCanBePublished
										* (leadInfoEntity.getCountOfNumbers() / (double) totalLeadSize));
						if (threadStartLeadList != null && !threadStartLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("Starting LeadTaskManager for Lead [" + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + "] with totalNumberOfChannelsApplicable ="
									+ totalNumberOfChannelsApplicable);
							threadStartLeadList.add(leadInfoEntity.getLeadId());
							Runnable task = new LeadTaskManager(totalNumberOfChannelsApplicable, leadInfoEntity, this,
									threadStartLeadList);
							new Thread(task).start();
						} else {
							LOGGER.info("Thread already started for " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName());
						}
						if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme()
								.equalsIgnoreCase(SEQUENTIAL)
								&& !processedLeadList.contains(leadInfoEntity.getLeadId())) {
							LOGGER.info("Added lead [" + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName() + "] as processed as User Priority is "
									+ userConfigurationTree.getUserEntity().getChannelPriorityScheme() + " user name "
									+ userConfigurationTree.getUserEntity().getFirstName());

							processedLeadList.add(leadInfoEntity.getLeadId());
						}
					}
					// if all leads are processed then break from this loop
					if (allLeads.size() == processedLeads.size()) {
						LOGGER.info(
								"Processed All leads for Campaign " + campaignEntity.getCampaignName() + " in AUTO ");
						processedCampaignSize += 1;
						break;
					}

					try {
						Thread.sleep(SLEEP_DURATION_MS);
					} catch (Exception e) {

					}
				}
				LOGGER.info("Looping for Campaign " + campaignEntity.getCampaignName());
			}

			try {
				Thread.sleep(SLEEP_DURATION_MS);
			} catch (InterruptedException e) {
			}
		}

	}

	public int processCampaignsForDynamicScheme(List<CampaignEntity> campaignEntities,
			Map<Long, List<LeadInfoEntity>> leadInfoEntityListByCampaignId,
			Integer totalNumberOfPhoneNumbersThatCanBePublished) {

		if (CollectionUtils.isEmpty(campaignEntities)) {
			LOGGER.info("campaignEntities is null in processCampaignsForDynamicScheme ");
			return -1;
		}
		if (CollectionUtils.isEmpty(leadInfoEntityListByCampaignId)) {
			LOGGER.info(
					"No Current Scheduled Lead Found in Campaign [" + campaignEntities.get(0).getCampaignName() + "]");
			return -1;
		}

		CopyOnWriteArrayList<LeadInfoEntity> highPriorityLeads = new CopyOnWriteArrayList<>();
		CopyOnWriteArrayList<LeadInfoEntity> mediumPriorityLeads = new CopyOnWriteArrayList<>();
		CopyOnWriteArrayList<LeadInfoEntity> lowPriorityLeads = new CopyOnWriteArrayList<>();
		int processFlag = 0;

		// For each Campaign
		int processedCampaignSize = 0;
		int postProcessingStatus = 0;
		for (CampaignEntity campaignEntity : campaignEntities) {
			// Bucket up HIGH/MEDIUM/LOW Leads
			List<LeadInfoEntity> leadInfoEntities = getScheduledLeadsFromGivenCampaign(campaignEntity.getCampaignId());
			if (CollectionUtils.isEmpty(leadInfoEntities)) {
				LOGGER.info("No Active Lead found for campaign [" + campaignEntity.getCampaignId() + "]");
//                for(Map.Entry<Long,List<LeadInfoEntity>> entry: leadInfoEntityListByCampaignId.entrySet()){
//                    LOGGER.info("Key "+ entry.getKey()+" Values "+ entry.getValue());
//                }
				continue;
			}
			for (LeadInfoEntity leadInfoEntity : leadInfoEntities) {
				if (HIGH.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
					LOGGER.info("Added HIGH priority leads " + leadInfoEntity.getLeadId() + "==>"
							+ leadInfoEntity.getLeadName());
					highPriorityLeads.add(leadInfoEntity);
				} else if (MEDIUM.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
					LOGGER.info("Added MEDIUM priority leads " + leadInfoEntity.getLeadId() + "==>"
							+ leadInfoEntity.getLeadName());
					mediumPriorityLeads.add(leadInfoEntity);
				} else if (LOW.equalsIgnoreCase(leadInfoEntity.getLeadPriorityType())) {
					LOGGER.info("Added LOW priority leads " + leadInfoEntity.getLeadId() + "==>"
							+ leadInfoEntity.getLeadName());
					lowPriorityLeads.add(leadInfoEntity);
				} else {
					LOGGER.info("Added Else priority leads " + leadInfoEntity.getLeadId() + "==>"
							+ leadInfoEntity.getLeadName() + " with LeadPriority Type "
							+ leadInfoEntity.getLeadPriorityType());
					highPriorityLeads.add(leadInfoEntity);
				}
			}

			// Take the total Static Channels in this campaign
			if (totalNumberOfPhoneNumbersThatCanBePublished == -1) {
				totalNumberOfPhoneNumbersThatCanBePublished = 10;// TODO
			}
			LOGGER.info("totalNumberOfPhoneNumbersThatCanBePublished => " + totalNumberOfPhoneNumbersThatCanBePublished
					+ " for Campaign " + campaignEntities.get(0).getCampaignName());
			totalNumberOfPhoneNumbersThatCanBePublished *= MULTIPYING_FACTOR;
			Set<Long> processedLeads = new HashSet<>();
			if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)) {

				UserConfigurationTree userConfigurationTree = userConfigurationTreeList != null
						? userConfigurationTreeList.stream()
								.filter(t -> t.getUserEntity().getUserId() == campaignEntities.get(0).getUserId())
								.findFirst().orElse(null)
						: null;

				LOGGER.info("Campaign Channel priority is SEQUENTIAL " + campaignEntity.getCampaignName());

				// Process All High priority leads

				boolean processBatchOnly = false;
				if (userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(ROUND_ROBIN)) {
					processBatchOnly = true;
				}

				if (!CollectionUtils.isEmpty(highPriorityLeads)) {
					processFlag = processLeads(highPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
							processBatchOnly);
					if (processFlag < 0) {
						LOGGER.info("Processed High Priority leads for Campaign " + campaignEntity.getCampaignName());
						postProcessingStatus = 0;
					} else {
						postProcessingStatus = 1;
					}
				}

				// Process All Medium priority leads
				// only process if no publishing of numbers were done in above step
				if (postProcessingStatus == 0 && !CollectionUtils.isEmpty(mediumPriorityLeads)) {
					processFlag = processLeads(mediumPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
							processBatchOnly);
					if (processFlag < 0) {
						LOGGER.info("Processed Medium Priority leads for Campaign " + campaignEntity.getCampaignName());
						postProcessingStatus = 0;
					} else {
						postProcessingStatus = 1;
					}

				}
				processFlag = 0;
				// Process All Low priority leads
				// only process if no publishing of numbers were done in above step
				if (postProcessingStatus == 0 && !CollectionUtils.isEmpty(lowPriorityLeads)) {
					processFlag = processLeads(lowPriorityLeads, totalNumberOfPhoneNumbersThatCanBePublished,
							processBatchOnly);
					if (processFlag < 0) {
						LOGGER.info("Processed Low Priority leads for Campaign " + campaignEntity.getCampaignName());
						break;
					}
				} else {
					LOGGER.info("Skipping Low Priority leads as  postProcessingStatus=  " + postProcessingStatus);
				}
				if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)) {
					LOGGER.info("Processed One Batch as User Priority "
							+ userConfigurationTree.getUserEntity().getChannelPriorityScheme());
					break;
				}

			} else if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(ROUND_ROBIN)) {

				LOGGER.info("Campaign Channel priority is ROUND_ROBIN " + campaignEntity.getCampaignName());

				UserConfigurationTree userConfigurationTree = userConfigurationTreeList != null
						? userConfigurationTreeList.stream()
								.filter(t -> t.getUserEntity().getUserId() == campaignEntity.getUserId()).findFirst()
								.orElse(null)
						: null;
				CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree != null
						? userConfigurationTree.getUserWiseCampaignList().stream()
								.filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
										.getCampaignId().longValue())
								.findFirst().orElse(null)
						: null;

				CopyOnWriteArrayList<Long> processedLeadList = campaignConfigurationTree != null
						? campaignConfigurationTree.getProcessedLeadList()
						: null;

				CopyOnWriteArrayList<LeadInfoEntity> allLeads = new CopyOnWriteArrayList<>();
				allLeads.addAll(highPriorityLeads);
				allLeads.addAll(mediumPriorityLeads);
				allLeads.addAll(lowPriorityLeads);
				// shuffle here to give all equal opportunity
				LOGGER.info("Shuffling the leads");
				Collections.shuffle(allLeads);
				checkforProcessedLeads(allLeads, processedLeadList, campaignConfigurationTree);
				for (LeadInfoEntity leadInfoEntity : allLeads) {

					if (processedLeadList.size() >= allLeads.size()) {
						LOGGER.info("All leads are processed atleast once so clearing processedLeadList ");
						processedLeadList.clear();
					} else {
						LOGGER.info("All leads are not processed  yet");

					}
					if (processedLeadList.contains(leadInfoEntity.getLeadId())) {
						LOGGER.info(
								"This lead is already processed once hence ignoring.. " + leadInfoEntity.getLeadName()
										+ " campaign priority Scheme is " + campaignEntity.getChannelPriorityScheme());
						LOGGER.info("Not Clearing Lead " + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName() + " from processedLeadList as it contains :");
						for (LeadInfoEntity leadInfoEntity1 : allLeads) {
							LOGGER.info("Lead " + leadInfoEntity1.getLeadId() + "=>" + leadInfoEntity1.getLeadName());
						}

						LOGGER.info("Total Lead Ids as in processedLeadList are:");
						for (Long leadId : processedLeadList) {
							LOGGER.info(leadId);
						}
						continue;
					}

					int ret = 0;
					List<LeadInfoEntity> tempLeadInfoEntityList = new ArrayList<>();
					tempLeadInfoEntityList.add(leadInfoEntity);
					// if this is not processed then only try for processing
					ret = processLeads(tempLeadInfoEntityList, totalNumberOfPhoneNumbersThatCanBePublished, true);
					if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)
							&& !processedLeadList.contains(leadInfoEntity.getLeadId())) {
						LOGGER.info("Added lead [" + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
								+ "] as processed as User Priority is "
								+ userConfigurationTree.getUserEntity().getChannelPriorityScheme() + " user name "
								+ userConfigurationTree.getUserEntity().getFirstName());

						processedLeadList.add(leadInfoEntity.getLeadId());
					}

					if (ret < 0) {
						// If all processed then add this to processed list
						LOGGER.info("This lead is processed " + leadInfoEntity.getLeadName());
						processedLeads.add(leadInfoEntity.getLeadId());
					} else {
						LOGGER.info("Processed 1 batch for  " + leadInfoEntity.getLeadName());
						// process 1 batch and break
						break;
					}

				}
				// if all leads are processed then break from this loop
				if (allLeads.size() == processedLeads.size()) {
					LOGGER.info("Processed All leads for Campaign " + campaignEntity.getCampaignName() + " in RR ");
					processFlag = -1;
					break;
				}
				if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)) {
					LOGGER.info("Processed One Batch as User Priority "
							+ userConfigurationTree.getUserEntity().getChannelPriorityScheme());
					break;
				}

			} else if (campaignEntity.getChannelPriorityScheme().equalsIgnoreCase(AUTO)) {

				LOGGER.info("Campaign Channel priority is AUTO " + campaignEntity.getCampaignName());
				Integer totalLeadSize = 0;
				CopyOnWriteArrayList<LeadInfoEntity> allLeads = new CopyOnWriteArrayList<>();
				allLeads.addAll(highPriorityLeads);
				allLeads.addAll(mediumPriorityLeads);
				allLeads.addAll(lowPriorityLeads);

				UserConfigurationTree userConfigurationTree = userConfigurationTreeList != null
						? userConfigurationTreeList.stream()
								.filter(t -> t.getUserEntity().getUserId() == campaignEntity.getUserId()).findFirst()
								.orElse(null)
						: null;
				CampaignConfigurationTree campaignConfigurationTree = userConfigurationTree != null
						? userConfigurationTree.getUserWiseCampaignList().stream()
								.filter(t -> t.getCampaignEntity().getCampaignId().longValue() == campaignEntity
										.getCampaignId().longValue())
								.findFirst().orElse(null)
						: null;

				CopyOnWriteArrayList<Long> processedLeadList = campaignConfigurationTree != null
						? campaignConfigurationTree.getProcessedLeadList()
						: null;
				CopyOnWriteArrayList<Long> threadStartLeadList = campaignConfigurationTree != null
						? campaignConfigurationTree.getThreadStartLeadList()
						: null;
				checkforProcessedLeads(allLeads, processedLeadList, campaignConfigurationTree);
				for (LeadInfoEntity leadInfoEntity : allLeads) {
					if (leadInfoEntity.getLeadName().startsWith("Lead_MC")
							|| leadInfoEntity.getLeadName().startsWith("Lead_RC")) {
						totalLeadSize += leadInfoDetailRepository
								.getUnDialedLeadCountByLeadId(leadInfoEntity.getLeadId());
					} else {
						totalLeadSize += leadInfoEntity.getCountOfNumbers();
					}
				}

				for (LeadInfoEntity leadInfoEntity : allLeads) {
					if (processedLeadList.size() >= allLeads.size()) {
						LOGGER.info("All leads are processed at least once so clearing processedLeadList ");
						processedLeadList.clear();
					}
					if (processedLeadList.contains(leadInfoEntity.getLeadId())) {
						LOGGER.info(
								"This lead is already processed once hence ignoring.. " + leadInfoEntity.getLeadName()
										+ " campaign priority Scheme is " + campaignEntity.getChannelPriorityScheme());
						LOGGER.info("Not Clearing Lead " + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName() + " from processedLeadList as it contains :");
						for (LeadInfoEntity leadInfoEntity1 : allLeads) {
							LOGGER.info("Lead " + leadInfoEntity1.getLeadId() + "=>" + leadInfoEntity1.getLeadName());
						}

						LOGGER.info("Total Lead Ids as in processedLeadList are:");
						for (Long leadId : processedLeadList) {
							LOGGER.info(leadId);
						}
						continue;
					}
					if (totalLeadSize < 1) {
						LOGGER.info("totalLeadSize is " + totalLeadSize + " please check configuration"
								+ campaignConfigurationTree.getCampaignEntity().getCampaignName());
						continue;
					}
					LOGGER.info("totalNumberOfPhoneNumbersThatCanBePublished:"
							+ totalNumberOfPhoneNumbersThatCanBePublished + " leadInfoEntity.getCountOfNumbers():"
							+ leadInfoEntity.getCountOfNumbers() + " totalLeadSize:" + totalLeadSize);
					int totalNumberOfChannelsApplicable = (int) Math.ceil(totalNumberOfPhoneNumbersThatCanBePublished
							* (leadInfoEntity.getCountOfNumbers() / (double) totalLeadSize));
					if (threadStartLeadList != null && !threadStartLeadList.contains(leadInfoEntity.getLeadId())) {
						LOGGER.info("Starting LeadTaskManager for Lead [" + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName() + "] with totalNumberOfChannelsApplicable ="
								+ totalNumberOfChannelsApplicable);
						threadStartLeadList.add(leadInfoEntity.getLeadId());
						Runnable task = new LeadTaskManager(totalNumberOfChannelsApplicable, leadInfoEntity, this,
								threadStartLeadList);
						new Thread(task).start();
					} else {
						LOGGER.info("Thread already started for " + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName());
					}
					if (!userConfigurationTree.getUserEntity().getChannelPriorityScheme().equalsIgnoreCase(SEQUENTIAL)
							&& !processedLeadList.contains(leadInfoEntity.getLeadId())) {
						LOGGER.info("Added lead [" + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
								+ "] as processed as User Priority is "
								+ userConfigurationTree.getUserEntity().getChannelPriorityScheme() + " user name "
								+ userConfigurationTree.getUserEntity().getFirstName());

						processedLeadList.add(leadInfoEntity.getLeadId());
					}

				}
				// if all leads are processed then break from this loop
				if (allLeads.size() == processedLeads.size()) {
					LOGGER.info("Processed All leads for Campaign " + campaignEntity.getCampaignName() + " in AUTO ");
					processFlag = -1;
					break;
				}

			}

		}
		return processFlag;
	}

	private void checkforProcessedLeads(CopyOnWriteArrayList<LeadInfoEntity> allLeads,
			CopyOnWriteArrayList<Long> processedLeadList, CampaignConfigurationTree campaignConfigurationTree) {

		if (!CollectionUtils.isEmpty(allLeads)) {

			for (LeadInfoEntity leadInfoEntity : allLeads) {
//                CopyOnWriteArrayList<Long> fullyProcessedLeads = campaignConfigurationTree.getFullyProcessedLeads();
//                if(!fullyProcessedLeads.contains(leadInfoEntity.getLeadId())) {
				List<LeadInfoDetailEntity> leadInfoDetailEntityList = null;
				try {
					leadInfoDetailEntityList = leadInfoDetailRepository
							.getPhoneNumbersBasedOnNotCompleteStatus(leadInfoEntity.getLeadId(), 1);
				} catch (JpaSystemException e) {
					LOGGER.error("Partition does not exists for Lead:" + leadInfoEntity.getLeadId()
							+ " Removing this lead from processing");
					leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(), COMPLETED);
					continue;
				}
				if (leadInfoDetailEntityList != null && leadInfoDetailEntityList.size() == 0) {
					LOGGER.info("No new number in " + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
							+ "hence clearing it from allLeads");
					allLeads.remove(leadInfoEntity);
					campaignConfigurationTree.getFullyProcessedLeads().add(leadInfoEntity.getLeadId());
					if (processedLeadList.contains(leadInfoEntity.getLeadId())) {
						LOGGER.info("No new number in " + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName() + "hence clearing it from processedLeadList");
						processedLeadList.remove(leadInfoEntity.getLeadId());
					} else {
						LOGGER.info("ProcessedLeadList does nt contain " + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName());
					}
				} else {
					LOGGER.info("Still [" + leadInfoDetailEntityList.size() + "] numbers present in "
							+ leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName());
				}
//                }else{
//                    LOGGER.info("Lead ["+leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName() +"] is completely processed");
//                }
			}
		} else {
			LOGGER.info("AllLeads is empty");
		}
	}

	@Transactional
	public Integer processLeads(List<LeadInfoEntity> leadInfoEntities,
			Integer totalNumberOfPhoneNumbersThatCanBePublished, boolean processBatchOnly) {
		if (totalNumberOfPhoneNumbersThatCanBePublished == 0) {
			LOGGER.info(
					"Nothing to publish in this processLeads call as totalNumberOfPhoneNumbersThatCanBePublished is 0");
			return -1;
		}
		if (CollectionUtils.isEmpty(leadInfoEntities)) {
			LOGGER.info("leadInfoEntities is null in processLeads function ");
			return -1;
		}
		CampaignConfigurationTree campaignConfigurationTree1 = null;
		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			List<CampaignConfigurationTree> campaignConfigurationTrees1 = userConfigurationTree
					.getUserWiseCampaignList();

			List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
					.getUserWiseCampaignList() != null
							? userConfigurationTree.getUserWiseCampaignList().stream()
									.filter(t -> t != null && ACTIVE.equalsIgnoreCase(t.getCampaignStatus())).collect(
											Collectors.toList())
							: null;
			for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
				if (campaignConfigurationTree.getCampaignEntity().getCampaignId().longValue() == leadInfoEntities.get(0)
						.getCampaignId().longValue()) {
					campaignConfigurationTree1 = campaignConfigurationTree;
					break;
				}
			}
			if (campaignConfigurationTree1 != null) {
				break;
			}
		}

		Set<Long> processLeads = new HashSet<>();
		HashMap<String, Integer> leadNameAndProcessedCount = new HashMap<>();
		for (LeadInfoEntity leadInfoEntity : leadInfoEntities) {
			if (processLeads.contains(leadInfoEntity.getLeadId())) {
				LOGGER.info("Lead " + leadInfoEntity.getLeadName() + " is already processed.");
				continue;
			}
			int leadProcessCount = 0;
			RetryInfoEntity retryInfoEntity = null;
			if (null != leadInfoEntity.getRetryId()) {
				retryInfoEntity = retryRepository.findById(leadInfoEntity.getRetryId()).orElse(null);
			}
			List<LeadInfoDetailEntity> leadInfoDetailEntityList;
			Integer totalPublishedCount = leadInfoDetailRepository
					.getPublishedCountByLeadId(leadInfoEntity.getLeadId());
			totalPublishedCount = null != totalPublishedCount ? totalPublishedCount : 0;
			if (Boolean.TRUE.equals(optimizedPublishingEnabled)
					&& (totalPublishedCount.intValue() >= totalNumberOfPhoneNumbersThatCanBePublished.intValue())) {
				LOGGER.info("Skipping Processing for Lead [+" + leadInfoEntity.getLeadId() + "=>"
						+ leadInfoEntity.getLeadName() + "] as totalPublishedCount is = " + totalPublishedCount
						+ " and totalNumberOfPhoneNumbersThatCanBePublished="
						+ totalNumberOfPhoneNumbersThatCanBePublished);
				continue;
			}
			if (retryInfoEntity != null && CYCLIC.equalsIgnoreCase(retryInfoEntity.getRetryType())) {
				LOGGER.info("Getting Data for Lead [" + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
						+ "] as totalPublishedCount is = " + totalPublishedCount
						+ " and totalNumberOfPhoneNumbersThatCanBePublished="
						+ totalNumberOfPhoneNumbersThatCanBePublished);

				leadInfoDetailEntityList = leadInfoDetailRepository.getNewPhoneNumbersBasedOnFreeChannels(
						leadInfoEntity.getLeadId(), totalNumberOfPhoneNumbersThatCanBePublished);
				LOGGER.info("Fetched Data from Lead [ " + leadInfoEntity.getLeadName() + " ] size [ "
						+ leadInfoDetailEntityList.size() + " ]");
				if (leadInfoDetailEntityList != null && leadInfoDetailEntityList.isEmpty()) {
					LOGGER.info("All numbers in lead [" + leadInfoEntity.getLeadId() + "=>"
							+ leadInfoEntity.getLeadName() + "] are processed, checking.. for TBRedial Numbers");
					Integer attemptNum = attemptRepository.getCurrentAttemptNum(leadInfoEntity.getRetryId());
					LOGGER.info("AttemptNum:" + attemptNum);
					if (attemptNum == null) {
						LOGGER.info("No Pending Round but lead is still running - Lead:" + leadInfoEntity.getLeadId()
								+ "=>" + leadInfoEntity.getLeadName());
						markLeadAsCompleted(leadInfoEntity, retryInfoEntity);
						continue;
					}
					String roundStatus = attemptRepository.getRoundStatus(leadInfoEntity.getRetryId(),
							attemptNum == null ? 0 : attemptNum);
					LOGGER.info("RoundStatus:" + roundStatus);
					String previousRoundStatus = null;

					if (leadInfoDetailRepository.getInProcessLeadCountByLeadIdByattemptNum(leadInfoEntity.getLeadId(),
							attemptNum) == null
							&& leadInfoDetailRepository.getTBRedialLeadCountByLeadIdByRedialattemptNum(
									leadInfoEntity.getLeadId(), attemptNum) == null) {
						LOGGER.info("Marked round[" + attemptNum + "] completed for lead " + leadInfoEntity.getLeadId()
								+ "=>" + leadInfoEntity.getLeadName());
						attemptRepository.markRoundCompleted(retryInfoEntity.getRetryId(), attemptNum);
						LOGGER.info("Marked round completed for lead:" + roundStatus);
						roundStatus = COMPLETED_ACTION;
					} else {
						LOGGER.info("Lead detail id "
								+ leadInfoDetailRepository.getInProcessLeadCountByLeadIdByattemptNum(
										leadInfoEntity.getLeadId(), attemptNum)
								+ " still left in TBRedial/DialFai/Published/Hold status");
					}

					// String aboveStatus =
					// attemptRepository.getPreviousRoundStatus(attempt_num,retryInfoEntity.getRetryId());

					if (leadInfoDetailRepository.checkTBRedialNumberLeadId(leadInfoEntity.getLeadId()) == null) {
						LOGGER.info("Mark attempts greater than attemptnum " + attemptNum + " as Not Required");
						attemptRepository.markAttemptsAsNotRequired(retryInfoEntity.getRetryId(), attemptNum);
					} else {
						LOGGER.info("Numbers still left to be redialed..");
					}

					Integer completedRetryCount = attemptRepository
							.getCompletedRetryCount(retryInfoEntity.getRetryId());
					LOGGER.info(
							"CompletedRetryCount" + completedRetryCount + "retryInfoEntity.getNoOfRetry().intValue()+1"
									+ retryInfoEntity.getNoOfRetry().intValue() + 1);
					// Mark Lead as Completed
					if (completedRetryCount != null
							&& (retryInfoEntity.getNoOfRetry().intValue() + 1 == completedRetryCount.intValue())) {
						LOGGER.info("Marked Lead as completed " + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName());
						markLeadAsCompleted(leadInfoEntity, retryInfoEntity);
					} else {
						LOGGER.info("Not updating completion status as retryInfoEntity.getNoOfRetry():"
								+ retryInfoEntity.getNoOfRetry() + " completedRetryCount:" + completedRetryCount);
					}
					if (attemptNum > 1) {
						previousRoundStatus = attemptRepository.getRoundStatus(leadInfoEntity.getRetryId(),
								attemptNum - 1);
					}
					if (COMPLETED.equalsIgnoreCase(roundStatus)
							|| (previousRoundStatus != null && COMPLETED.equalsIgnoreCase(previousRoundStatus))) {
						// attempt_num =
						// leadInfoDetailRepository.getCurrentRediallingAttemptNumber(leadInfoEntity.getLeadId());
						LOGGER.info("roundStatus:" + roundStatus + " and previousRoundStatus" + previousRoundStatus
								+ " and finding TBRedial for attemptNum " + (attemptNum - 1));
						leadInfoDetailEntityList = leadInfoDetailRepository.getTBRedialPhoneNumbersBasedOnFreeChannels(
								leadInfoEntity.getLeadId(), totalNumberOfPhoneNumbersThatCanBePublished,
								attemptNum - 1);
						LOGGER.info("Got [" + leadInfoDetailEntityList.size() + "] numbers To Be Redialed for ["
								+ leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
								+ " ] in Redialling attempt " + attemptNum);
					} else {
						LOGGER.info("Still CDRs are not received for " + leadInfoEntity.getLeadId() + "=>"
								+ leadInfoEntity.getLeadName() + ".. Status is " + roundStatus);
						continue;
					}
				}
			} else {
				leadInfoDetailEntityList = leadInfoDetailRepository.getPhoneNumbersForRorNTypeRetry(
						leadInfoEntity.getLeadId(), totalNumberOfPhoneNumbersThatCanBePublished, new Date());
				LOGGER.info("Fetched Data with RorN TypeRetry from Lead [ " + leadInfoEntity.getLeadName()
						+ " ] size [ " + leadInfoDetailEntityList.size() + " ]");
				if (leadInfoDetailEntityList.isEmpty()) {
					// Check if all the numbers are dialed according to the retry count
					// Dialed, Published
					Integer undialedNumber = leadInfoDetailRepository
							.getUnDialedLeadCountByLeadId(leadInfoEntity.getLeadId());
					if (undialedNumber == null || undialedNumber.intValue() == 0) {
						if (!leadInfoEntity.getLeadName().contains("Lead_SD_")
								&& !leadInfoEntity.getLeadName().contains("Lead_PX_")
								&& !leadInfoEntity.getLeadName().contains("Lead_MC_")) {
							LOGGER.info("Marked Lead as completed " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName());
							if (retryInfoEntity != null) {
								leadInfoRepository.updateLeadCompletionStatusToCompleted(retryInfoEntity.getRetryId(),
										new Date());
							} else {
								leadInfoRepository.updateLeadCompletionStatusToCompleted2(leadInfoEntity.getLeadId(),
										new Date());
							}
							try {
								LOGGER.info("Generate Summary for " + leadInfoEntity.getLeadId() + "=>"
										+ leadInfoEntity.getLeadName());
								generateSummaryForCurrentDayCDRs(leadInfoEntity.getLeadId());
							} catch (Exception e) {
								LOGGER.error("Got exception while generating summary for " + leadInfoEntity.getLeadId()
										+ "=>" + leadInfoEntity.getLeadName(), e);
							}
						}
					} else {
						LOGGER.info("There are still [" + undialedNumber
								+ "] numbers left in created/dialfail/published/tbredial status for "
								+ leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName());
					}
				}
			}
			if (leadInfoDetailEntityList.size() > 0) {
				if (("Scheduled Later".equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus()))) {
					// markLeadAsRunning(leadInfoEntity);
					leadInfoEntity = leadInfoRepository.findById(leadInfoEntity.getLeadId()).orElse(null);
					leadInfoEntity.setLeadCompletionStatus(RUNNING);
					leadInfoEntity.setProcessDtm(new Date());
					leadInfoEntity = leadInfoRepository.save(leadInfoEntity);
					// leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(),
					// LEAD_START_ACTION);
					LOGGER.info("Updated Lead " + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
							+ " as " + leadInfoEntity.getLeadCompletionStatus());
					// this check is to avoid duplicate entries in attempt_info table on restart
					if (retryInfoEntity != null && CYCLIC.equalsIgnoreCase(retryInfoEntity.getRetryType())) {
						List<AttemptInfoEntity> entities = attemptRepository
								.findByRetryId(retryInfoEntity.getRetryId());
						int retryCount = 0;
						if (retryInfoEntity != null && entities.size() == 0) {
							retryCount = retryInfoEntity.getNoOfRetry();
							List<AttemptInfoEntity> attemptInfoEntities = new ArrayList<>();
							for (int i = 1; i <= retryCount + 1; i++) {
								AttemptInfoEntity attemptInfoEntity = new AttemptInfoEntity();
								attemptInfoEntity.setStatus(ATTEMPT_PENDING_STATUS);
								if (i == 1) {
									attemptInfoEntity.setQualifiedCount(getTotalValidNumbers(leadInfoEntity));
								} else {
									attemptInfoEntity.setQualifiedCount(0);
								}
								attemptInfoEntity.setProcessedNumCount(0);
								attemptInfoEntity.setRound(i);
								attemptInfoEntity.setRetryId(leadInfoEntity.getRetryId());
								attemptInfoEntities.add(attemptInfoEntity);
							}
							attemptRepository.saveAll(attemptInfoEntities);
							LOGGER.info("Saved Attempts [" + attemptInfoEntities.size() + "] for ["
									+ leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName() + "]");
						} else {
							LOGGER.info("Retry not found for " + leadInfoEntity.getLeadId() + "=>"
									+ leadInfoEntity.getLeadName());
						}
					} else {
						LOGGER.info("Not saving Attempt Table as retry type is R or N");
					}

				}
				LOGGER.info("leadInfoDetailEntityList size is " + leadInfoDetailEntityList.size()
						+ " calling persistTransactionInDb for lead " + leadInfoEntity.getLeadId() + "=>"
						+ leadInfoEntity.getLeadName());

				LOGGER.info("Calling persistTransactionInDb");
				sendRCSMessageAndPersistInDB(leadInfoEntity, leadInfoDetailEntityList);
				LOGGER.info("Done persistTransactionInDb");
				if (leadNameAndProcessedCount.size() > 0
						&& leadNameAndProcessedCount.get(leadInfoEntity.getLeadName()) != null) {
					int count = leadNameAndProcessedCount.get(leadInfoEntity.getLeadName());
					count += leadInfoDetailEntityList.size();
					LOGGER.info(
							"Adding info in leadNameAndProcessedCount" + leadInfoEntity.getLeadName() + "=>" + count);
					leadNameAndProcessedCount.put(leadInfoEntity.getLeadName(), count);
				} else {
					LOGGER.info("Adding info in leadNameAndProcessedCount" + leadInfoEntity.getLeadName() + "=>"
							+ leadInfoDetailEntityList.size());
					leadNameAndProcessedCount.put(leadInfoEntity.getLeadName(), leadInfoDetailEntityList.size());
				}

				if (processBatchOnly) {
					leadProcessCount = leadInfoDetailRepository
							.getInProcessLeadCountByLeadId(leadInfoEntity.getLeadId());
					if (leadInfoEntity.getCountOfNumbers() == leadProcessCount) {
						LOGGER.info("Indicating all Numbers in this lead are processed [Batch Mode ] "
								+ leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName() + " size ["
								+ leadProcessCount + " ]");
						processLeads.add(leadInfoEntity.getLeadId());
					} else {
						LOGGER.info("Not all leads are processed for lead : " + leadInfoEntity.getLeadName()
								+ " Stats are " + leadNameAndProcessedCount.get(leadInfoEntity.getLeadName()) + "=>"
								+ leadInfoEntity.getCountOfNumbers());
					}
				} else {
					if (leadNameAndProcessedCount.get(leadInfoEntity.getLeadName()).intValue() == leadInfoEntity
							.getCountOfNumbers().intValue()) {
						LOGGER.info(
								"Indicating all Numbers in this lead are processed1 " + leadInfoEntity.getLeadName());
						processLeads.add(leadInfoEntity.getLeadId());
					} else {
						LOGGER.info("Not all leads are processed for lead : " + leadInfoEntity.getLeadName()
								+ " Stats are " + leadNameAndProcessedCount.get(leadInfoEntity.getLeadName()) + "=>"
								+ leadInfoEntity.getCountOfNumbers());
					}
				}
				if (processBatchOnly) {
					LOGGER.info("Processed 1 batch and now breaking from loop");
					break;
				}
			} else {
				LOGGER.info("Indicating all Numbers in this lead are processed2 " + leadInfoEntity.getLeadName());
				processLeads.add(leadInfoEntity.getLeadId());
				// Indicating all Numbers in this lead are processed
			}
			if (processLeads.size() == leadInfoEntities.size()) {
				LOGGER.info("All leads  are processed, total [ " + processLeads.size() + " ]");
				return -1;
			}
		}
		if (processLeads.size() == leadInfoEntities.size()) {
			LOGGER.info("All leads  are processed, total [ " + processLeads.size() + " ]");
			return -1;
		}

		LOGGER.info("processLeads returning 1");
		return 1;

	}

	private void markLeadAsCompleted(LeadInfoEntity leadInfoEntity, RetryInfoEntity retryInfoEntity) {
		if ("TestLeadGoogle".equalsIgnoreCase(leadInfoEntity.getLeadName()))
			return;
		leadInfoRepository.updateLeadCompletionStatusToCompleted(retryInfoEntity.getRetryId(), new Date());
		UserEntity userEntity = userRepository.getUserEntityByUserId(leadInfoEntity.getUserId());
		CampaignEntity campaignEntity = campaignRepository
				.getCampaignEntityByCampaignId(leadInfoEntity.getCampaignId());
		LeadInfoEntity fleadInfoEntity = leadInfoEntity;
		if ("Y".equalsIgnoreCase(userEntity.getNotificationRequired())) {
			new Thread(() -> {
				EmailRequestModel emailRequestModel = new EmailRequestModel();
				emailRequestModel.setEventType(LEAD_COMPLETED);
				emailRequestModel.setEmailTo(userEntity.getEmail());
				Map<String, String> valueToReplace = new HashMap<>();
				valueToReplace.put("username", userEntity.getFirstName());
				valueToReplace.put("campaignName", campaignEntity.getCampaignName());
				valueToReplace.put("leadName", fleadInfoEntity.getLeadName());
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				valueToReplace.put("datetime", dateFormat.format(new Date()));
				emailService.sendEmail(valueToReplace, emailRequestModel);
			}).start();
		}

		try {
			LOGGER.info("Generate Summary for " + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName());
			generateSummaryForCurrentDayCDRs(leadInfoEntity.getLeadId());
		} catch (Exception e) {
			LOGGER.error("Got exception while generating summary for " + leadInfoEntity.getLeadId() + "=>"
					+ leadInfoEntity.getLeadName(), e);
		}

		CampaignConfigurationTree campaignConfigurationTree = getCampaignConfigurationTreeForGivenCampaignId2(
				leadInfoEntity.getCampaignId());
		CopyOnWriteArrayList<LeadInfoEntity> campaignWiseLeadList = campaignConfigurationTree.getCampaignWiseLeadList();
		Iterator<LeadInfoEntity> iterator = campaignWiseLeadList.iterator();
		while (iterator.hasNext()) {
			LeadInfoEntity entity = iterator.next();
			LOGGER.info("Campaign : " + leadInfoEntity.getCampaignId() + " Has Lead : " + leadInfoEntity.getLeadId()
					+ "=>" + leadInfoEntity.getLeadName());
			if (entity.getLeadId().longValue() == leadInfoEntity.getLeadId().longValue()) {
				LOGGER.info("Saurabh Removed lead:" + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
						+ " from campaign Lead List");
				entity.setLeadCompletionStatus(COMPLETED);
				break;
			}
		}
	}

	public void generateSummaryForCurrentDayCDRs(Long leadId) throws Exception {
		// TODO
		LOGGER.info("Summary generation Completed for Lead ID " + leadId);
	}

	public void sendRCSMessageAndPersistInDB(LeadInfoEntity leadInfoEntity,
			List<LeadInfoDetailEntity> leadInfoDetailEntityList) {

		try {

			Integer attemptNumNormal = null;
			Integer attemptNumDialFail = null;
			Date modifiedDate = new Date();
			String status = DIALED;
			String cli = CLI_SCHEME_DYNAMIC;
			List<Long> leadInfoDetailIdsNormal = new ArrayList<>();
			List<Long> leadInfoDetailIdsDialFail = new ArrayList<>();
			Long leadId = null;

			CampaignConfigurationTree campaignConfigurationTreeForGivenCampaignId = getCampaignConfigurationTreeForGivenCampaignId(
					leadInfoEntity.getCampaignId());
			CampaignEntity campaignEntity = campaignConfigurationTreeForGivenCampaignId.getCampaignEntity();
			if (leadInfoEntity.getProcessDtm() == null) {
				leadInfoEntity.setProcessDtm(new Date());
			}
			Map<Integer, List<LeadInfoDetailEntity>> attemptWiseLeadDetailList = leadInfoDetailEntityList.stream()
					.collect(Collectors.groupingBy(LeadInfoDetailEntity::getAttemptNum));
			for (Map.Entry<Integer, List<LeadInfoDetailEntity>> entry : attemptWiseLeadDetailList.entrySet()) {
				LOGGER.info("Working on Attempt:" + entry.getKey());
				leadInfoDetailIdsNormal.clear();
				leadInfoDetailIdsDialFail.clear();
				for (LeadInfoDetailEntity leadInfoDetailEntity : entry.getValue()) {
					if (!leadInfoDetailEntity.getStatus().equalsIgnoreCase(DIALFAIL)) {
						leadInfoDetailIdsNormal.add(leadInfoDetailEntity.getLeadInfoDetailId());
						attemptNumNormal = leadInfoDetailEntity.getAttemptNum() + 1;
						LOGGER.info("Increasing Attempt Num :" + attemptNumNormal + " for leadInfoDetailEntity:"
								+ leadInfoDetailEntity.toString());
						leadInfoDetailEntity.setAttemptNum(attemptNumNormal);
					} else {
						attemptNumDialFail = leadInfoDetailEntity.getAttemptNum();
						leadInfoDetailEntity.setAttemptNum(attemptNumDialFail);
						leadInfoDetailIdsDialFail.add(leadInfoDetailEntity.getLeadInfoDetailId());
						LOGGER.info("Not increasing Attempt Count as it is a DialFail For "
								+ leadInfoDetailEntity.getLeadId() + "=>" + leadInfoDetailEntity.getPhoneNumber());
					}
					if (leadId == null) {
						leadId = leadInfoDetailEntity.getLeadId();
					}
					// leadInfoDetailRepository.updateLeadInfoDetailEntityObject(leadInfoDetailEntity.getLeadInfoDetailId(),attemptNum,strIvrInfo,modifiedDate,modifiedBy,leadInfoDetailEntity.getLeadId(),
					// status, cli);
					// Update Entity to be published

					leadInfoDetailEntity.setLastModifiedDate(new Date());

					leadInfoDetailEntity.setStatus(DIALED);
					String phoneNumber = leadInfoDetailEntity.getPhoneNumber();
					if (!phoneNumber.startsWith("+91")) {
						phoneNumber = "+91" + phoneNumber;
					}

					MessageTypeEntity messageTypeEntity = messageTypeRepository
							.findByMessageId(campaignEntity.getMessageId());
					switch (messageTypeEntity.getMessageType()) {
					case TEXT:
						Text text;
						if (campaignWiseRbmMessage.containsKey(campaignEntity.getCampaignId())) {
							text = (Text) campaignWiseRbmMessage.get(campaignEntity.getCampaignId());
						} else {
							text = objectMapper.readValue(campaignEntity.getMessageJson(), Text.class);
							campaignWiseRbmMessage.put(campaignEntity.getCampaignId(), text);

						}
						messagingAgent.sendTextMessage(text.getTextMessage(), phoneNumber);
						break;
					case MEDIA:
						Media media;
						if (campaignWiseRbmMessage.containsKey(campaignEntity.getCampaignId())) {
							media = (Media) campaignWiseRbmMessage.get(campaignEntity.getCampaignId());
						} else {
							media = objectMapper.readValue(campaignEntity.getMessageJson(), Media.class);
							campaignWiseRbmMessage.put(campaignEntity.getCampaignId(), media);
						}
						messagingAgent.sendMediaMessage(media.getFileUrl(), phoneNumber);
						break;
					case SUGGESTED_REPLIES:
						SuggestedReplies suggestedReplies;
						if (campaignWiseRbmMessage.containsKey(campaignEntity.getCampaignId())) {
							suggestedReplies = (SuggestedReplies) campaignWiseRbmMessage
									.get(campaignEntity.getCampaignId());
						} else {
							suggestedReplies = objectMapper.readValue(campaignEntity.getMessageJson(),
									SuggestedReplies.class);
							campaignWiseRbmMessage.put(campaignEntity.getCampaignId(), suggestedReplies);
						}

						messagingAgent.sendSuggestedRepliesMessage(suggestedReplies, phoneNumber);
						break;
					case RICH_CARDS:
						RichCards richCards;
						StandaloneCard standaloneCard;
						if (campaignWiseRbmMessage.containsKey(campaignEntity.getCampaignId())) {
							standaloneCard = (StandaloneCard) campaignWiseRbmMessage
									.get(campaignEntity.getCampaignId());
						} else {

							richCards = objectMapper.readValue(campaignEntity.getMessageJson(), RichCards.class);
							standaloneCard = messagingAgent.createStandaloneCard(richCards);
							campaignWiseRbmMessage.put(campaignEntity.getCampaignId(), standaloneCard);
						}
						messagingAgent.sendStandaloneCard(standaloneCard, phoneNumber);
						break;
					default:
						break;
					}
					LOGGER.info("Updated " + leadInfoDetailEntity.getLeadInfoDetailId() + "=>"
							+ leadInfoDetailEntity.getPhoneNumber() + " with PUBLISHED status");
				}

				EntityTransaction tx = null;
				if (!CollectionUtils.isEmpty(leadInfoDetailIdsNormal)) {
					final String update = "update lead_info_detail partition(p:partitionId) lid set   lid.status = :status, lid.ATTEMPT_NUM = :attemptNum, lid.LAST_MODIFIED_DATE = :md "
							+ "  where lid.LEAD_INFO_DETAIL_ID in :ids and lid.lead_id = :lid ";
					EntityManager em = entityManagerFactory.createEntityManager();

					try {

						tx = em.getTransaction();
						tx.begin();
						LOGGER.info("Calling Query to update leadinfodetail for normal CDRs");
						em.createNativeQuery(update).setParameter("partitionId", leadId)
								.setParameter("ids", leadInfoDetailIdsNormal)
								.setParameter("attemptNum", attemptNumNormal).setParameter("md", modifiedDate)
								.setParameter("lid", leadId).setParameter("status", status).executeUpdate();

						LOGGER.info("Query to update leadinfodetail for normal CDRs executed");
					} catch (Exception e) {
						LOGGER.error("Got Exception ", e);
					} finally {
						tx.commit();
						em.close();
					}
					// leadInfoDetailRepository.updateLeadInfoDetailEntityObject(leadInfoDetailIdsNormal,attemptNumNormal,strIvrInfo,modifiedDate,modifiedBy,leadId,
					// status, cli);
				}
				if (!CollectionUtils.isEmpty(leadInfoDetailIdsDialFail)) {
					final String update = "update lead_info_detail partition(p:partitionId) lid set  lid.status = :status, lid.ATTEMPT_NUM = :attemptNum, lid.LAST_MODIFIED_DATE = :md "
							+ " where lid.LEAD_INFO_DETAIL_ID in :ids and lid.lead_id = :lid ";
					EntityManager em = entityManagerFactory.createEntityManager();
					try {

						tx = em.getTransaction();
						tx.begin();
						LOGGER.info("Calling Query to update leadinfodetail for Dialfail CDRs");
						em.createNativeQuery(update).setParameter("partitionId", leadId)
								.setParameter("ids", leadInfoDetailIdsDialFail)
								.setParameter("attemptNum", attemptNumDialFail).setParameter("md", modifiedDate)
								.setParameter("lid", leadId).setParameter("status", status).executeUpdate();

					} catch (Exception e) {
						LOGGER.error("Got Exception ", e);
					} finally {
						tx.commit();
						em.close();
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("Got Exception ", e);
		}

	}
//    public void revertTransactionInDb(LeadInfoEntity leadInfoEntity, List<LeadInfoDetailEntity> leadInfoDetailEntityList)  {
//        LOGGER.info("Reverting Transaction in DB");
//        try {
//            String ivrInfo ;
//            String strIvrInfo ;
//            Integer attemptNumNormal = null;
//            Integer attemptNumDialFail = null;
//            Date modifiedDate = new Date();
//            String status = CREATED;
//            String cli ;
//            List<Long> leadInfoDetailIdsNormal = new ArrayList<>();
//            List<Long> leadInfoDetailIdsDialFail = new ArrayList<>();
//            Long leadId = null;
//            CampaignConfigurationTree campaignConfigurationTree = getCampaignConfigurationTreeForGivenLead(leadInfoEntity);
//            if(campaignConfigurationTree.getIvrInfo().size() > 0){
//                LOGGER.info("Lead ["+ leadInfoEntity.getLeadId()+"=>"+leadInfoEntity.getLeadName()+" has Ivr Info "+ campaignConfigurationTree.getIvrInfo().get(0));
//                ivrInfo = campaignConfigurationTree.getIvrInfo().get(0);
//            }else{
//                ivrInfo = getIvrInfoForGivenLead(leadInfoEntity);
//                LOGGER.info("Lead ["+ leadInfoEntity.getLeadId()+"=>"+leadInfoEntity.getLeadName()+" does not have Ivr Info "+ ivrInfo+" Saving..");
//                campaignConfigurationTree.getIvrInfo().add(ivrInfo);
//            }
//            CampaignEntity campaignEntity = campaignConfigurationTree.getCampaignEntity();
//            if ("FALSE".equalsIgnoreCase(globalDailyCLIUsage)) {
//                if (CLI_SCHEME_FIXED.equalsIgnoreCase(campaignEntity.getCliAllocationScheme())) {
//                    cli = campaignEntity.getCliAllocated();
//                } else if (CLI_SCHEME_EXPIRY.equalsIgnoreCase(campaignEntity.getCliAllocationScheme())) {
//                    cli = cliProviderService.getCLIConfiguration(false);
//                } else if (CLI_SCHEME_DAILY.equalsIgnoreCase(campaignEntity.getCliAllocationScheme())) {
//                    cli = cliProviderService.getCLIConfiguration(true);
//                } else {
//                    cli = DYNAMIC_CHANNEL_ALLOCATION_SCHEME;
//                }
//            } else {
//                cli = cliProviderService.getCLIConfiguration(true);
//            }
//            if(!StringUtils.isEmpty(ivrInfo)){
//                strIvrInfo = ivrInfo;
//            }else{
//                strIvrInfo = "-1";
//            }
//            if(leadInfoEntity.getProcessDtm() == null){
//                leadInfoEntity.setProcessDtm(new Date());
//                //leadInfoRepository.save(leadInfoEntity);
//            }
//            for (LeadInfoDetailEntity leadInfoDetailEntity : leadInfoDetailEntityList) {
//
//                leadInfoDetailEntity.setAdditonalDataInfoText(strIvrInfo);
//                if(!leadInfoDetailEntity.getStatus().equalsIgnoreCase("DialFail")) {
//                    leadInfoDetailIdsNormal.add(leadInfoDetailEntity.getLeadInfoDetailId());
//                    attemptNumNormal = leadInfoDetailEntity.getAttemptNum();
//                    LOGGER.info("Reverting Attempt Num :"+attemptNumNormal+" for leadInfoDetailEntity:"+leadInfoDetailEntity.toString());
//                    leadInfoDetailEntity.setAttemptNum(attemptNumNormal);
//                }else{
//                    attemptNumDialFail = leadInfoDetailEntity.getAttemptNum();
//                    leadInfoDetailEntity.setAttemptNum(attemptNumDialFail);
//                    leadInfoDetailIdsDialFail.add(leadInfoDetailEntity.getLeadInfoDetailId());
//                    LOGGER.info("Not increasing Attempt Count as it is a DialFail For "+ leadInfoDetailEntity.getLeadId()+"=>"+ leadInfoDetailEntity.getPhoneNumber());
//                }
//                if(leadId == null){
//                    leadId = leadInfoDetailEntity.getLeadId();
//                }
//                //leadInfoDetailRepository.updateLeadInfoDetailEntityObject(leadInfoDetailEntity.getLeadInfoDetailId(),attemptNum,strIvrInfo,modifiedDate,modifiedBy,leadInfoDetailEntity.getLeadId(), status, cli);
//                //Update Entity to be published
//                leadInfoDetailEntity.setCli(cli);
//                leadInfoDetailEntity.setLastModifiedDate(new Date());
//
//                leadInfoDetailEntity.setStatus(CREATED);
//
//                LOGGER.info("Reverted "+leadInfoDetailEntity.getLeadInfoDetailId()+"=>"+leadInfoDetailEntity.getPhoneNumber()+" with Created status");
//            }
//
//            EntityTransaction tx = null;
//            if(!CollectionUtils.isEmpty(leadInfoDetailIdsNormal)){
//                final String update = "update lead_info_detail lid set  lid.cli = :cli, lid.status = :status, lid.ATTEMPT_NUM = :attemptNum, lid.ADDITIONAL_DATA_INFO_TEXT = :adit, lid.LAST_MODIFIED_DATE = :md" +
//                        " where lid.LEAD_INFO_DETAIL_ID in :ids and lid.lead_id = :lid ";
//                EntityManager em = entityManagerFactory.createEntityManager();
//                try {
//
//                    tx = em.getTransaction();
//                    tx.begin();
//                    LOGGER.info("Calling Query to Revert leadinfodetail for normal CDRs");
//                    em.createNativeQuery(update)
//                            .setParameter("ids", leadInfoDetailIdsNormal)
//                            .setParameter("attemptNum", attemptNumNormal)
//                            .setParameter("adit", strIvrInfo)
//                            .setParameter("md", modifiedDate)
//                            .setParameter("lid", leadId)
//                            .setParameter("status", status)
//                            .setParameter("cli", cli)
//                            .executeUpdate();
//
//                    LOGGER.info("Query to Revert leadinfodetail for normal CDRs executed");
//                }catch (Exception e){
//                    LOGGER.error("Got Exception in Reverting ", e);
//                } finally {
//                    tx.commit();
//                    em.close();
//                }
//                //leadInfoDetailRepository.updateLeadInfoDetailEntityObject(leadInfoDetailIdsNormal,attemptNumNormal,strIvrInfo,modifiedDate,modifiedBy,leadId, status, cli);
//            }
//            if(!CollectionUtils.isEmpty(leadInfoDetailIdsDialFail)){
//                final String update = "update lead_info_detail lid set  lid.cli = :cli, lid.status = :status, lid.ATTEMPT_NUM = :attemptNum, lid.ADDITIONAL_DATA_INFO_TEXT = :adit, lid.LAST_MODIFIED_DATE = :md" +
//                        " where lid.LEAD_INFO_DETAIL_ID in :ids and lid.lead_id = :lid ";
//                EntityManager em = entityManagerFactory.createEntityManager();
//                try {
//
//                    tx = em.getTransaction();
//                    tx.begin();
//                    LOGGER.info("Calling Query to Revert leadinfodetail for Dialfail CDRs");
//                    em.createNativeQuery(update)
//                            .setParameter("ids", leadInfoDetailIdsDialFail)
//                            .setParameter("attemptNum", attemptNumDialFail)
//                            .setParameter("adit", strIvrInfo)
//                            .setParameter("md", modifiedDate)
//                            .setParameter("lid", leadId)
//                            .setParameter("status", status)
//                            .setParameter("cli", cli)
//                            .executeUpdate();
//
//                }catch (Exception e){
//                    LOGGER.error("Got Exception ", e);
//                } finally {
//                    tx.commit();
//                    em.close();
//                }
//            }
//        }catch (Exception e){
//            LOGGER.error("Got Exception ", e);
//        }
//
//    }

	private List<LeadInfoEntity> getScheduledLeadsFromGivenPool(List<LeadInfoEntity> allLeadInfoList) {
		List<LeadInfoEntity> leadInfoEntityList = new ArrayList<>();
		// return leadInfoRepository.findAll();
		long currentTime = System.currentTimeMillis();
		for (LeadInfoEntity leadInfoEntity : allLeadInfoList) {
			if (leadInfoEntity.getScheduleId() == null) {
				LOGGER.error("Schedule ID not found for Lead " + leadInfoEntity.getLeadName());
				continue;
			}
			if ("Created".equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())) {
				LeadInfoEntity leadInfoEntityByLeadId = leadInfoRepository
						.getLeadInfoEntityByLeadId(leadInfoEntity.getLeadId());
				leadInfoEntity.setLeadCompletionStatus(leadInfoEntityByLeadId.getLeadCompletionStatus());
				updateLeadStatusInCampaignObject(leadInfoEntity.getCampaignId(), leadInfoEntity.getLeadId(),
						leadInfoEntityByLeadId.getLeadCompletionStatus());
			}
			ScheduleEntity scheduleEntity = scheduleRepository.findById(leadInfoEntity.getScheduleId()).orElse(null);
			if (TEMP_PAUSE.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())
					&& (!StringUtils.hasLength(scheduleEntity.getWindowRequired())
							|| "N".equalsIgnoreCase(scheduleEntity.getWindowRequired()))) {
				Date now = new Date();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);
				if (calendar.get(Calendar.AM_PM) == 1 && calendar.get(Calendar.HOUR) >= 9) {
					LOGGER.info("Skipping Lead ->" + leadInfoEntity.getLeadId()
							+ " from processing as it is Temp paused and time is post 9 PM");
					continue;
				}

				LOGGER.info("Marking Lead:" + leadInfoEntity.getLeadId() + " as Running");
				leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(), RUNNING);
				leadInfoEntity.setLeadCompletionStatus(RUNNING);
				updateLeadStatusInCampaignObject(leadInfoEntity.getCampaignId(), leadInfoEntity.getLeadId(), RUNNING);

			}
			if (scheduleEntity != null) {
				if (scheduleEntity.getScheduleStartDtm().getTime() <= currentTime
						&& scheduleEntity.getScheduleEndDtm().getTime() >= currentTime) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(currentTime);
					int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);// sunday=1,monday=2
					String days = scheduleEntity.getScheduleDay();
					if (days != null && days.contains(String.valueOf(dayOfWeek))) {
						if (StringUtils.hasLength(scheduleEntity.getWindowRequired())
								&& "Y".equalsIgnoreCase(scheduleEntity.getWindowRequired())) {
							Date now = new Date();
							Date windowStartTime;
							Date windowEndTime;
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String windowStartTimeStr = df.format(now) + " " + scheduleEntity.getWindowStartTime()
									+ ":00";
							String windowEndTimeStr = df.format(now) + " " + scheduleEntity.getWindowEndTime() + ":00";
							try {
								windowStartTime = sdf.parse(windowStartTimeStr);
								windowEndTime = sdf.parse(windowEndTimeStr);
								if (now.getTime() >= windowStartTime.getTime()
										&& now.getTime() <= windowEndTime.getTime()) {
									if (TEMP_PAUSE.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())) {
										LOGGER.info("Marking Lead:" + leadInfoEntity.getLeadId() + " as Running");
										leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(),
												RUNNING);
										leadInfoEntity.setLeadCompletionStatus(RUNNING);
										updateLeadStatusInCampaignObject(leadInfoEntity.getCampaignId(),
												leadInfoEntity.getLeadId(), RUNNING);
									}
									leadInfoEntityList.add(leadInfoEntity);
								} else {
									if (!TEMP_PAUSE.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())) {
										LOGGER.info("Window Expired for Lead:" + leadInfoEntity.getLeadId() + "=>"
												+ leadInfoEntity.getLeadName() + "  Windows:" + windowEndTimeStr + "-"
												+ windowEndTimeStr);
										LOGGER.info("Marking Lead:" + leadInfoEntity.getLeadId() + " as Temp Pause");
										leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(),
												TEMP_PAUSE);
										leadInfoEntity.setLeadCompletionStatus(TEMP_PAUSE);
										updateLeadStatusInCampaignObject(leadInfoEntity.getCampaignId(),
												leadInfoEntity.getLeadId(), TEMP_PAUSE);
									}
									continue;
								}
							} catch (Exception e) {
								LOGGER.error("Scheduled Service: Got Exception", e);
								leadInfoEntityList.add(leadInfoEntity);
							}
						} else {
							leadInfoEntityList.add(leadInfoEntity);
						}
					} else {
						Date now = new Date();
						if (RUNNING.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())) {
							if (!leadInfoEntity.getLeadName().contains("Lead_SD_")
									&& !leadInfoEntity.getLeadName().contains("Lead_RC_")
									&& !leadInfoEntity.getLeadName().contains("Lead_PX_")
									&& !leadInfoEntity.getLeadName().contains("Lead_MC_")
									&& !leadInfoEntity.getLeadName().contains("Lead_C2C_")
									&& now.getTime() < scheduleEntity.getScheduleEndDtm().getTime()) {
								LOGGER.info("Marking Lead:" + leadInfoEntity.getLeadId() + " as Temp Pause");
								leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(), TEMP_PAUSE);
								leadInfoEntity.setLeadCompletionStatus(TEMP_PAUSE);
								updateLeadStatusInCampaignObject(leadInfoEntity.getCampaignId(),
										leadInfoEntity.getLeadId(), TEMP_PAUSE);
								continue;
							} else {
								LOGGER.info("Marking Lead:" + leadInfoEntity.getLeadId() + " as Schedule Expired");
								leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(),
										SCHEDULE_EXPIRED);
								leadInfoEntity.setLeadCompletionStatus(SCHEDULE_EXPIRED);
								updateLeadStatusInCampaignObject(leadInfoEntity.getCampaignId(),
										leadInfoEntity.getLeadId(), SCHEDULE_EXPIRED);
								continue;
							}

						}
					}
				} else if (RUNNING.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())
						&& scheduleEntity.getScheduleEndDtm().getTime() < currentTime) {
					LOGGER.info("Marking Lead:" + leadInfoEntity.getLeadId() + " with status"
							+ leadInfoEntity.getLeadCompletionStatus() + " as Schedule Expired");
					leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(), SCHEDULE_EXPIRED);
					leadInfoEntity.setLeadCompletionStatus(SCHEDULE_EXPIRED);
					updateLeadStatusInCampaignObject(leadInfoEntity.getCampaignId(), leadInfoEntity.getLeadId(),
							SCHEDULE_EXPIRED);

				}
			}
		}
		return leadInfoEntityList;

	}

	private void updateLeadStatusInCampaignObject(Long campaignId, Long leadId, String status) {
		CampaignConfigurationTree campaignConfigurationTreeForGivenCampaignId = getCampaignConfigurationTreeForGivenCampaignId2(
				campaignId);
		CopyOnWriteArrayList<LeadInfoEntity> campaignWiseLeadList = campaignConfigurationTreeForGivenCampaignId
				.getCampaignWiseLeadList();
		Iterator<LeadInfoEntity> iterator = campaignWiseLeadList.iterator();
		while (iterator.hasNext()) {
			LeadInfoEntity next = iterator.next();
			if (next.getLeadId().longValue() == leadId.longValue()) {
				LOGGER.info("Saurabh updated Lead Status in campaignConfigurationTreeForGivenCampaignId as " + status
						+ " for lead: " + leadId);
				next.setLeadCompletionStatus(status);
				break;
			}
		}
	}

	private List<LeadInfoEntity> getScheduledLeadsFromGivenCampaign(Long campaignId) {
		LOGGER.info("Called getScheduledLeadsFromGivenCampaign() for campaignID:" + campaignId);
		List<LeadInfoEntity> leadInfoEntityList = leadInfoRepository.getLeadInfoEntitiesByCampaignId2(campaignId);
		if (CollectionUtils.isEmpty(leadInfoEntityList)) {
			List<LeadInfoEntity> allLeads = leadInfoRepository.getLeadInfoEntitiesByCampaignIdForSummary(campaignId);
			if (!CollectionUtils.isEmpty(allLeads)) {
				for (LeadInfoEntity leadInfoEntity : allLeads) {
					LOGGER.info("Campaign " + campaignId + " has Lead:" + leadInfoEntity);
				}
			}
			// LOGGER.info("Dinesh : leadInfoEntityList is empty for campaignId
			// ["+campaignId+"]");
			CampaignConfigurationTree campaignConfigurationTree = getActiveCampaignConfigurationTreeForGivenCampaignId(
					campaignId);
			if (!campaignConfigurationTree.getCampaignStatus().equalsIgnoreCase(COMPLETED)
					&& !campaignConfigurationTree.getCampaignEntity().getCampaignName().contains("Campaign_SD_")
					&& !campaignConfigurationTree.getCampaignEntity().getCampaignName().contains("Campaign_RC_")
					&& !campaignConfigurationTree.getCampaignEntity().getCampaignName().contains("Campaign_MC_")
					&& !campaignConfigurationTree.getCampaignEntity().getCampaignName().contains("Campaign_C2C_")) {
				campaignRepository.updateCampaignStatusToComplete(campaignId, COMPLETED, new Date());
				campaignConfigurationTree.setCampaignStatus(COMPLETED);
				campaignConfigurationTree.setThreadStarted(false);
				UserConfigurationTree userConfigurationTree = getUserConfigurationTreeForCampaignId(campaignId);
				if (null != userConfigurationTree) {
					userConfigurationTree.getActiveCampaignList().remove(campaignId);
					userConfigurationTree.getProcessedCampaignList().remove(campaignId);
					LOGGER.info("Removed  campaign Id " + campaignId
							+ " from ActiveCampaignList and  ProcessedCampaignList for user : "
							+ userConfigurationTree.getUserEntity().getUserId() + "=>"
							+ userConfigurationTree.getUserEntity().getFirstName());
					LOGGER.info("Marked Campaign " + campaignConfigurationTree.getCampaignEntity().getCampaignId()
							+ "=>" + campaignConfigurationTree.getCampaignEntity().getCampaignName() + " completed");
				} else {
					LOGGER.info(
							"Surprisingly this campaign was not found in Active Campaign List from UserConfiguration:"
									+ campaignId);
				}

			} else {
				LOGGER.info("Campaign " + campaignConfigurationTree.getCampaignEntity().getCampaignId() + "=>"
						+ campaignConfigurationTree.getCampaignEntity().getCampaignName()
						+ " campaignConfigurationTree.getCampaignStatus() =  "
						+ campaignConfigurationTree.getCampaignStatus());
			}
		} else {
			LOGGER.info("leadInfo List not empty [" + leadInfoEntityList.size() + "]");
			for (LeadInfoEntity leadInfoEntity : leadInfoEntityList) {
				LOGGER.info("Lead" + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName());
			}
		}
		List<LeadInfoEntity> scheduledNowLeadList = new ArrayList<>();
		scheduledNowLeadList.addAll(getScheduledLeadsFromGivenPool(leadInfoEntityList));
		if (!CollectionUtils.isEmpty(scheduledNowLeadList)) {
			for (LeadInfoEntity leadInfoEntity : scheduledNowLeadList) {
				if (!"Temp Pause".equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())
						&& !"Pause".equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())
						&& !"Running".equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus())) {
					leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(), RUNNING);
					LOGGER.info("Marked Lead :" + leadInfoEntity.getLeadId() + "=>" + leadInfoEntity.getLeadName()
							+ " as Running");
				}
			}
		} else {
			Date now = new Date();
			for (LeadInfoEntity leadInfoEntity : leadInfoEntityList) {
				ScheduleEntity scheduleEntity = scheduleRepository.getByScheduleId(leadInfoEntity.getScheduleId());
				if (RUNNING.equalsIgnoreCase(leadInfoEntity.getLeadCompletionStatus()) && scheduleEntity != null
						&& scheduleEntity.getScheduleEndDtm().getTime() < now.getTime()) {
					// Schedule Expired
					LOGGER.info("Marking Lead:" + leadInfoEntity.getLeadId() + " as Schedule Expired");
					leadInfoRepository.updateLeadCompletionStatus(leadInfoEntity.getLeadId(), "Schedule Expired");
				}
			}
		}
		return scheduledNowLeadList;

	}

	private CampaignConfigurationTree getCampaignConfigurationTreeForGivenLead(LeadInfoEntity leadInfoEntity) {
		CampaignConfigurationTree ret = null;
		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
					.getUserWiseCampaignList() != null
							? userConfigurationTree.getUserWiseCampaignList().stream()
									.filter(t -> t != null && ACTIVE.equalsIgnoreCase(t.getCampaignStatus())).collect(
											Collectors.toList())
							: null;
			for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
				if (campaignConfigurationTree.getCampaignEntity().getCampaignId().longValue() == leadInfoEntity
						.getCampaignId().longValue()) {
					ret = campaignConfigurationTree;
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}

		return ret;
	}

	public UserConfigurationTree getUserConfigurationTreeForCampaignId(Long campaignId) {
		LOGGER.info("Getting UserConfigurationTreeForCampaignId for Campaign id [" + campaignId + "]");

		UserConfigurationTree ret = null;

		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			LOGGER.info("getUserConfigurationTreeForCampaignId userConfigurationTree"
					+ userConfigurationTree.getUserEntity().getUserId() + "=>"
					+ userConfigurationTree.getUserEntity().getFirstName());
			CopyOnWriteArraySet<Long> activeCampaigns = userConfigurationTree.getActiveCampaignList();
			if (!CollectionUtils.isEmpty(activeCampaigns)) {
				LOGGER.info("Active Campaign Ids");
				activeCampaigns.forEach(s -> LOGGER.info(s));
				for (Long id : activeCampaigns) {
					if (id.equals(campaignId)) {
						ret = userConfigurationTree;
						LOGGER.info("Got UserConfigurationTreeForCampaignId: " + userConfigurationTree);
						break;
					} else {
						LOGGER.info("Not matched campaign Id " + id);
					}
				}
			} else {
				LOGGER.info("No active campaigns found for User " + userConfigurationTree.getUserEntity().getUserId()
						+ "=>" + userConfigurationTree.getUserEntity().getFirstName());
			}
			if (ret != null) {
				break;
			}
		}

		if (ret == null) {
			LOGGER.info("getUserConfigurationTreeForCampaignId is returning null");
		}
		return ret;
	}

	public UserConfigurationTree getUserConfigurationTreeForUserId(Long userId) {
		Optional<UserConfigurationTree> first = userConfigurationTreeList.stream()
				.filter(t -> t.getUserEntity().getUserId() == userId).findFirst();
		if (first.isPresent()) {
			return first.get();
		}

		return null;
	}

	public CampaignConfigurationTree getActiveCampaignConfigurationTreeForGivenCampaignId(long campaignId) {

		CampaignConfigurationTree ret = null;

		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
					.getUserWiseCampaignList() != null
							? userConfigurationTree.getUserWiseCampaignList().stream()
									.filter(t -> t != null && ACTIVE.equalsIgnoreCase(t.getCampaignStatus())).collect(
											Collectors.toList())
							: null;
			for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
				if (campaignConfigurationTree.getCampaignEntity().getCampaignId().longValue() == campaignId) {
					ret = campaignConfigurationTree;
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}

		return ret;
	}

	public CampaignConfigurationTree getCampaignConfigurationTreeForGivenCampaignId(long campaignId) {

		CampaignConfigurationTree ret = null;

		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
					.getUserWiseAllCampaignList() != null
							? userConfigurationTree.getUserWiseAllCampaignList().stream().collect(Collectors.toList())
							: null;
			for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
				if (campaignConfigurationTree.getCampaignEntity().getCampaignId().longValue() == campaignId) {
					ret = campaignConfigurationTree;
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}
		return ret;
	}

	public CampaignConfigurationTree getCampaignConfigurationTreeForGivenCampaignId2(long campaignId) {

		CampaignConfigurationTree ret = null;

		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
					.getUserWiseCampaignList();
			for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
				LOGGER.info("CampaignConfigurationTree getCampaignId:"
						+ campaignConfigurationTree.getCampaignEntity().getCampaignId());

				if (campaignConfigurationTree.getCampaignEntity().getCampaignId().longValue() == campaignId) {
					ret = campaignConfigurationTree;
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}

		return ret;
	}

	public UserConfigurationTree getUserConfigurationTreeForGivenCampaignId(long campaignId) {

		UserConfigurationTree ret = null;

		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
					.getUserWiseCampaignList();
			for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
				if (campaignConfigurationTree.getCampaignEntity().getCampaignId().longValue() == campaignId) {
					LOGGER.info("Got  userConfigurationTree tree");
					ret = userConfigurationTree;
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}

		return ret;
	}

	public UserConfigurationTree getUserConfigurationTreeForGivenCampaignIdWithNoCheck(long campaignId) {

		UserConfigurationTree ret = null;
		for (UserConfigurationTree userConfigurationTree : userConfigurationTreeList) {
			List<CampaignConfigurationTree> campaignConfigurationTrees = userConfigurationTree
					.getUserWiseAllCampaignList();
			for (CampaignConfigurationTree campaignConfigurationTree : campaignConfigurationTrees) {
				if (campaignConfigurationTree.getCampaignEntity().getCampaignId().longValue() == campaignId) {
					LOGGER.info("Got  userConfigurationTree tree");
					ret = userConfigurationTree;
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}

		return ret;
	}

	private int getTotalValidNumbers(LeadInfoEntity returnedLead) {
		return returnedLead.getCountOfNumbers()
				- (returnedLead.getCountOfNonRcsNumbers() != null ? returnedLead.getCountOfNonRcsNumbers().intValue()
						: 0)
				- (returnedLead.getCountOfDuplicateNumbers() != null
						? returnedLead.getCountOfDuplicateNumbers().intValue()
						: 0)
				- (returnedLead.getCountOfInvalidNumbers() != null ? returnedLead.getCountOfInvalidNumbers().intValue()
						: 0);

	}

}
