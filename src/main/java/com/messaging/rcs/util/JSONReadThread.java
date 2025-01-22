package com.messaging.rcs.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.rcsbusinessmessaging.v1.RbmApiHelper;
import com.google.api.services.rcsbusinessmessaging.v1.model.BatchGetUsersResponse;
import com.messaging.rcs.consumer.RabbitMQSenderTelco;
import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.model.WebEnagage;
import com.messaging.rcs.email.repository.DNDRepository;
import com.messaging.rcs.model.LeadInfo;
import com.messaging.rcs.repository.BlackListRepository;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.schedular.BotTokenAPIService;
import com.messaging.rcs.schedular.TokenPojo;
import com.messaging.rcs.vi.bot.request.RcsEnableContactsResponse;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class JSONReadThread implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(JSONReadThread.class);
	private static final String DUPLICATE_NUMBER = "DUPLICATE_NUMBER";
	private static final String INVALID_NUMBER = "INVALID_NUMBER";
	private static final String BLACKLIST_NUMBER = "BLACKLIST_NUMBER";
	public static final String NONRCS_NUMBER = "NONRCS_NUMBER";
	private static final String CREATED = "Created";
	private static final String DND = "DND";
	private static final String OK = "OK";
	public static final long TTS_CAMPAIGN = 5l;
	private TokenPojo pojo;
	private BufferedReader br = null;
	private List<String> list;
	private LeadInfo leadInfo;
	private LeadInfoDetailRepository leadInfoDetailRepository;
	private BlackListRepository blackListRepository;
	private UserEntity userEntity;
	private boolean isDuplicateCheck;
	private JdbcTemplate jdbcTemplate;
	// private static RbmApiHelper rbmApiHelper;
	private boolean markDuplicate = false;
	private long campaignTypeId = 0;
	private BotTokenAPIService botTokenAPIService;
	private DNDRepository dndRepository;
	private String rcsEnabled;
	private RabbitMQSenderTelco rabbitMQSender;

	public JSONReadThread(LeadInfo leadInfo, LeadInfoDetailRepository leadInfoDetailRepository,
			BufferedReader bufferedReader, boolean isDuplicate, BlackListRepository blackListRepository,
			UserEntity userEntity, JdbcTemplate jdbcTemplate, boolean markDuplicate, long campaignTypeId,
			BotTokenAPIService botTokenAPIService, DNDRepository dndRepository, TokenPojo pojo, String rcsEnabled,
			RabbitMQSenderTelco rabbitMQSender) {
		this.leadInfo = leadInfo;
		this.leadInfo.setCountOfBlackListNumbers(0);
		this.leadInfoDetailRepository = leadInfoDetailRepository;
		this.br = bufferedReader;
		this.isDuplicateCheck = isDuplicate;
		this.blackListRepository = blackListRepository;
		this.userEntity = userEntity;
		this.jdbcTemplate = jdbcTemplate;
		this.markDuplicate = markDuplicate;
		this.campaignTypeId = campaignTypeId;
		this.botTokenAPIService = botTokenAPIService;
		this.dndRepository = dndRepository;
		this.pojo = pojo;
		this.rcsEnabled = rcsEnabled;
		this.rabbitMQSender = rabbitMQSender;
		// Class<? extends JSONReadThread> aClass = JSONReadThread.class;
		/*
		 * rbmApiHelper = new RbmApiHelper(new File(aClass
		 * .getClassLoader().getResource("rbm-agent-service-account-credentials.json").
		 * getFile()));
		 */ }

	public void run() {
		String line = null;
		int count = 0;
		while (true) {
			this.list = new ArrayList<>();
			synchronized (br) {
				try {
					while ((line = br.readLine()) != null) {
						if (count < 1000) {
							list.add(line);
							count++;
						} else {
							list.add(line);
							count = 0;
							break;
						}
					}
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
			try {
				Thread.sleep(1);
				LOGGER.info("JSON Read Thread Working on batch Th ID:" + Thread.currentThread().getId());
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
			process(this.list);
			if (line == null) {
				LOGGER.info("Breaking out of loop ThreadId:" + Thread.currentThread().getId());
				break;
			}
		}

	}

	public void process(List<String> list) {
		long starttime = System.currentTimeMillis();
		try {

			List<LeadInfoDetailEntity> leadInfoDetailEntityList = new ArrayList<>();
			// LOGGER.info("LeadInfo-> " + leadInfo);
			String headerColumns[] = null;
			Date now = new Date();
			long totaltime = 0;
			int recordCount = 0;
			if (leadInfo != null) {

				for (String line : list) {
					{
						/**
						 * phoneNumber + "," + additonalDataInfoNumeric + "," + additonalDataInfoDate +
						 * "," + additonalDataInfoText2 + "," + playWords;
						 */
						int d_fr = 0;
						int n_fr = 0;
						int t_fr = 0;
						int p_fr = 0;
						// LOGGER.info("Print "+line + "ThreadId:"+Thread.currentThread().getId());
						String rowColumns[] = line.split("::");
						LeadInfoDetailEntity leadInfoDetailEntity = new LeadInfoDetailEntity();
						leadInfoDetailEntity.setLeadId(leadInfo.getLeadId());
						leadInfoDetailEntity.setStatus(CREATED);
						leadInfoDetailEntity.setNextCallDtm(now);
						if (rowColumns[0].startsWith("91") || rowColumns[0].startsWith("+91")) {
							String noIndiaCodeNo = rowColumns[0].substring(rowColumns[0].length() - 10);
							leadInfoDetailEntity.setPhoneNumber(noIndiaCodeNo);

						} else {
							leadInfoDetailEntity.setPhoneNumber(rowColumns[0]);

						}
						// leadInfoDetailEntity.setPhoneNumber(rowColumns[0]);
						// leadInfoDetailEntity.setAdditonalDataInfoNumeric(rowColumns[1]);
						if (StringUtils.hasLength(rowColumns[1]) && !"null".equalsIgnoreCase(rowColumns[1])) {
							leadInfoDetailEntity.setAdditonalDataInfoText(rowColumns[1]);
						}
						if (StringUtils.hasLength(rowColumns[2]) && !"null".equalsIgnoreCase(rowColumns[2])) {

							leadInfoDetailEntity.setAdditonalDataInfoText2(rowColumns[2]);
						}

						leadInfoDetailEntity.setCreatedDate(new Date());
						leadInfoDetailEntity.setLastModifiedDate(new Date());
						leadInfoDetailEntity.setLastModifiedBy(userEntity.getUserName());
						leadInfoDetailEntity.setCreatedBy(userEntity.getUserName());

						/*
						 * if (StringUtils.hasLength(rowColumns[4]) &&
						 * !"null".equalsIgnoreCase(rowColumns[4])) {
						 * leadInfoDetailEntity.setPlayWords(rowColumns[4]); }
						 */
						leadInfoDetailEntity.setStatus(CREATED);
						leadInfoDetailEntity.setNextCallDtm(now);
						leadInfoDetailEntity.setAttemptNum(0);
						if (markDuplicate) {
							leadInfoDetailEntity.setPhoneNumberStatus(DUPLICATE_NUMBER);
						} else {

							if (!validatePhoneNumber(rowColumns[0])) {
								leadInfoDetailEntity.setPhoneNumberStatus(INVALID_NUMBER);
							} else if (isBlackListed(leadInfoDetailEntity.getPhoneNumber(), leadInfo.getUserId())) {
								leadInfoDetailEntity.setPhoneNumberStatus(BLACKLIST_NUMBER);
							} else if (leadInfoDetailEntity.getPhoneNumber() != null
									&& getDNDStatus(leadInfoDetailEntity.getPhoneNumber()) == true) {
								leadInfoDetailEntity.setPhoneNumberStatus(DND);

							} else if (rcsEnabled.equalsIgnoreCase(OK)) {
								if (isNonRcs(leadInfoDetailEntity.getPhoneNumber()) == true) {
									leadInfoDetailEntity.setPhoneNumberStatus(OK);

								} else {
									leadInfoDetailEntity.setPhoneNumberStatus(NONRCS_NUMBER);
								}
							} else {
								leadInfoDetailEntity.setPhoneNumberStatus(NONRCS_NUMBER);
							}

						}
						leadInfoDetailEntityList.add(leadInfoDetailEntity);
					}
				}
			}
			totaltime = System.currentTimeMillis() - starttime;
			LOGGER.info("Total time in making [" + leadInfoDetailEntityList.size() + "] records is [" + totaltime
					+ "] Ms   LeadId:" + leadInfo.getLeadId() + " ThreadId:" + Thread.currentThread().getId());
			starttime = System.currentTimeMillis();

			/*
			 * jdbcTemplate.batchUpdate(
			 * "insert into lead_info_detail (status, lead_id, phone_number, attempt_num, ADDITIONAL_DATA_INFO_NUMERIC,"
			 * +
			 * " ADDITIONAL_DATA_INFO_DATE, ADDITIONAL_DATA_INFO_TEXT2, ADDITIONAL_DATA_INFO_TEXT, cli, language, "
			 * +
			 * " phone_number_status, next_call_dtm, retry_attempt, retry_duration, retry_type, play_words ,"
			 * +
			 * " created_by, created_date, last_modified_by, last_modified_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			 * , leadInfoDetailEntityList, 10001, (ps, argument) -> { ps.setString(1,
			 * argument.getStatus()); ps.setLong(2, argument.getLeadId()); ps.setString(3,
			 * argument.getPhoneNumber()); ps.setInt(4, argument.getAttemptNum() != null ?
			 * argument.getAttemptNum() : 0); ps.setString(5,
			 * argument.getAdditonalDataInfoNumeric()); ps.setString(6,
			 * argument.getAdditonalDataInfoDate()); ps.setString(7,
			 * argument.getAdditonalDataInfoText2()); ps.setString(8,
			 * argument.getAdditonalDataInfoText()); ps.setString(9, argument.getCli());
			 * ps.setString(10, argument.getLanguage()); ps.setString(11,
			 * argument.getPhoneNumberStatus()); ps.setDate(12,
			 * convertJavaDateToSqlDate(argument.getNextCallDtm())); ps.setInt(13,
			 * argument.getRetryAttempt() != null ? argument.getRetryAttempt() : 0);
			 * ps.setInt(14, argument.getRetryDuration() != null ?
			 * argument.getRetryDuration() : 0); ps.setString(15, argument.getRetryType());
			 * ps.setString(16, argument.getPlayWords()); ps.setString(17,
			 * argument.getCreatedBy()); ps.setDate(18,
			 * convertJavaDateToSqlDate(argument.getCreatedDate())); ps.setString(19,
			 * argument.getLastModifiedBy()); ps.setDate(20,
			 * convertJavaDateToSqlDate(argument.getLastModifiedDate())); });
			 */
			if (leadInfoDetailEntityList.size() > 0) {
				// Send To user wise Queue
				if (leadInfo.getRcsMsgTypeId() == 1 || leadInfo.getRcsMsgTypeId() == 3) {
					WebEnagage setLeadInfoDetailsForRMQ = new WebEnagage();
					setLeadInfoDetailsForRMQ.setLeadInfo(leadInfo);
					setLeadInfoDetailsForRMQ.setLeadInfoDetailEntities(leadInfoDetailEntityList);
					setLeadInfoDetailsForRMQ.setUserEntity(userEntity);
					setLeadInfoDetailsForRMQ.setToken(pojo);
					rabbitMQSender.sendToBatchRecordToUserNameQueue(userEntity, setLeadInfoDetailsForRMQ);
				} else {
					WebEnagage setLeadInfoDetailsForRMQ = new WebEnagage();
					setLeadInfoDetailsForRMQ.setLeadInfo(leadInfo);
					setLeadInfoDetailsForRMQ.setLeadInfoDetailEntities(leadInfoDetailEntityList);
					setLeadInfoDetailsForRMQ.setUserEntity(userEntity);
					setLeadInfoDetailsForRMQ.setToken(pojo);
					rabbitMQSender.smsSendToBatchRecordToUserNameQueue(userEntity, setLeadInfoDetailsForRMQ);
				}

			}

			totaltime = System.currentTimeMillis() - starttime;
			LOGGER.info("Total time in saving [" + leadInfoDetailEntityList.size() + "] records is [" + totaltime
					+ "] Ms  ThreadId:" + Thread.currentThread().getId());

		} catch (Exception e) {
			throw new RuntimeException("Some Problem with this lead, pls choose someother lead !!");
		}

	}

	public java.sql.Date convertJavaDateToSqlDate(Date date) {
		return new java.sql.Date(date.getTime());
	}

	private boolean validatePhoneNumber(String phoneNumber) {
		boolean isValid = true;
		if (phoneNumber == null) {
			return false;
		}

		if (phoneNumber.length() != 10)
			return false;

		try {
			long l = Long.parseLong(phoneNumber);
			if (l < 0) {
				isValid = false;
			}
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;

	}

	private boolean isBlackListed(String phoneNumber, long userId) {
		boolean isBlackList = false;
		// long starttime = System.currentTimeMillis();
		// String prefix = phoneNumber.substring(0, 3);
		String phNumber = blackListRepository.isBlackList(phoneNumber, userId);// ,
																				// parentUserId);
		// long totaltime = System.currentTimeMillis() - starttime;
		// LOGGER.info("Total time in checking blackList for phonenumber:"+phoneNumber+"
		// is ["+totaltime+"] Ms ThreadId:"+Thread.currentThread().getId());
		if (StringUtils.hasLength(phNumber)) {
			LOGGER.info("Phone Number[" + phoneNumber + "] is BlackListed");
			isBlackList = true;
		}
		return isBlackList;
	}

	private boolean isNonRcs(String phoneNumber) {
		boolean isNonRcs = false;
		/*
		 * if (!phoneNumber.startsWith("+91")) { phoneNumber = "+91" + phoneNumber; }
		 */
		phoneNumber = "+91" + phoneNumber.substring(phoneNumber.length() - 10);
		LOGGER.info("Going On Verified RCS or Non-Rcs:::=>" + phoneNumber);

		ObjectMapper om = new ObjectMapper();
		String rcsEnableContactsResponse = null;

		try {
			rcsEnableContactsResponse = botTokenAPIService.checkRcsNumber(pojo.getAccess_token(), phoneNumber,
					userEntity.getBotId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (rcsEnableContactsResponse.contains("features")) {
				LOGGER.info("Status Of " + phoneNumber + " ::" + rcsEnableContactsResponse);

				isNonRcs = true;
			} else {
				isNonRcs = false;
				LOGGER.info("Number " + phoneNumber + " is RCS compatible");
			}
		} catch (Exception e) {
			isNonRcs = false;
		}

		return isNonRcs;
	}

	public boolean getDNDStatus(String mobilenumber) {
		// String prefix = mobilenumber.substring(0, 3);
		boolean status = false;
		String phoneNumber = dndRepository.isDNDNo(mobilenumber);
		try {
			if (Objects.nonNull(phoneNumber)) {
				LOGGER.info("Phone Number[" + phoneNumber + "] is  DND");
				status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
}
