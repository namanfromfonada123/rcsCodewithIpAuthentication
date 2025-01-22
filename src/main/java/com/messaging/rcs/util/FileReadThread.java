package com.messaging.rcs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.rcsbusinessmessaging.v1.RbmApiHelper;
import com.google.gson.Gson;
import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.repository.DNDRepository;
import com.messaging.rcs.model.LeadInfo;
import com.messaging.rcs.repository.BlackListRepository;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.schedular.BotTokenAPIService;
import com.messaging.rcs.schedular.TokenPojo;
import com.messaging.rcs.vi.bot.request.BulkTokenResponse;

public class FileReadThread implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(FileReadThread.class);
	private static final String DUPLICATE_NUMBER = "DUPLICATE_NUMBER";
	private static final String INVALID_NUMBER = "INVALID_NUMBER";
	public static final String NONRCS_NUMBER = "NONRCS_NUMBER";
	private static final String BLACKLIST_NUMBER = "BLACKLIST_NUMBER";
	private static final String CREATED = "Created";
	private static final String OK = "OK";
	private static final String DND = "DND";
	private String dynmaicParamFromFileHeader;
	private BufferedReader br = null;
	private List<String> list;
	private LeadInfo leadInfo;
	private LeadInfoDetailRepository leadInfoDetailRepository;
	private BlackListRepository blackListRepository;
	private UserEntity userEntity;
	private String fileName;
	private boolean isDNDCheck;
	private boolean isDuplicateCheck;
	private List<Integer> dateIndexes;
	private List<Integer> numIndexes;
	private List<Integer> textIndexes;
	private List<Integer> langIndexes;
	private List<Integer> playIndexes;
	private int phoneIdx = -1;
	private JdbcTemplate jdbcTemplate;
	private boolean markDuplicate = false;
	private static RbmApiHelper rbmApiHelper;
	private DNDRepository dndRepository;
	private BotTokenAPIService botTokenAPIService;
	private TokenPojo pojo;
	private String rcsEnabled;

	public FileReadThread(String fileName, LeadInfo leadInfo, LeadInfoDetailRepository leadInfoDetailRepository,
			BufferedReader bufferedReader, boolean isDND, boolean isDuplicate, List<Integer> dateIndexes,
			List<Integer> numIndexes, List<Integer> textIndexes, List<Integer> langIndexes, List<Integer> playIndexes,
			BlackListRepository blackListRepository, UserEntity userEntity, JdbcTemplate jdbcTemplate,
			boolean markDuplicate, BotTokenAPIService botTokenAPIService, DNDRepository dndRepository, TokenPojo pojo,
			String dynmaicParamFromFileHeader, String rcsEnabled) {
		this.fileName = fileName;
		this.leadInfo = leadInfo;
		this.leadInfo.setCountOfBlackListNumbers(0);
		this.leadInfoDetailRepository = leadInfoDetailRepository;
		this.br = bufferedReader;
		this.isDNDCheck = isDND;
		this.isDuplicateCheck = isDuplicate;
		this.dateIndexes = dateIndexes;
		this.numIndexes = numIndexes;
		this.textIndexes = textIndexes;
		this.langIndexes = langIndexes;
		this.playIndexes = playIndexes;
		this.blackListRepository = blackListRepository;
		this.userEntity = userEntity;
		this.jdbcTemplate = jdbcTemplate;
		this.markDuplicate = markDuplicate;
		this.botTokenAPIService = botTokenAPIService;
		this.dndRepository = dndRepository;
		this.pojo = pojo;
		this.rcsEnabled = rcsEnabled;
		this.dynmaicParamFromFileHeader = dynmaicParamFromFileHeader;
		/*
		 * Class<? extends FileReadThread> aClass = FileReadThread.class;
		 * 
		 * rbmApiHelper = new RbmApiHelper(new File(aClass
		 * .getClassLoader().getResource("rbm-agent-service-account-credentials.json").
		 * getFile()));
		 */

	}

	public void run() {
		String line = null;
		int count = 0;
		while (true) {
			this.list = new ArrayList<>();
			synchronized (br) {
				try {
					while ((line = br.readLine()) != null) {
						if (count < 9999) {
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
				LOGGER.info("File Read Thread Working on batch Th ID:" + Thread.currentThread().getId());
				process(this.list);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
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

			DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
			// LOGGER.info("LeadInfo-> " + leadInfo);
			String headerColumns[] = null;
			Date now = new Date();
			long totaltime = 0;
			int recordCount = 0;

			if (leadInfo != null) {

				for (String line : list) {
					{

						int d_fr = 0;
						int n_fr = 0;
						int t_fr = 0;
						int p_fr = 0;
						// LOGGER.info("Print "+line + "ThreadId:"+Thread.currentThread().getId());
						String rowColumns[] = line.split(",");
						LeadInfoDetailEntity leadInfoDetailEntity = new LeadInfoDetailEntity();
						leadInfoDetailEntity.setLeadId(leadInfo.getLeadId());
						leadInfoDetailEntity.setStatus(CREATED);
						leadInfoDetailEntity.setNextCallDtm(now);
						leadInfoDetailEntity.setAdditonalDataInfoText(dynmaicParamFromFileHeader);
						String phoneNumber = null;
						if (phoneIdx == -1) {
							phoneNumber = rowColumns[0];
						} else {
							phoneNumber = rowColumns[phoneIdx];
						}
						if (phoneNumber.startsWith("91") || phoneNumber.startsWith("+91")) {
							String noIndiaCodeNo = phoneNumber.substring(phoneNumber.length() - 10);
							leadInfoDetailEntity.setPhoneNumber(noIndiaCodeNo);

						} else {
							leadInfoDetailEntity.setPhoneNumber(phoneNumber);

						}

						leadInfoDetailEntity.setCreatedDate(new Date());
						leadInfoDetailEntity.setLastModifiedDate(new Date());
						leadInfoDetailEntity.setLastModifiedBy(leadInfo.getLastModifiedBy());
						leadInfoDetailEntity.setCreatedBy(leadInfo.getCreatedBy());
						StringBuffer additonalDataInfoText2 = new StringBuffer();
						StringBuffer additonalDataInfoText2Temp = new StringBuffer();
						StringBuffer additonalDataInfoDate = new StringBuffer();
						StringBuffer additonalDataInfoNumeric = new StringBuffer();
						StringBuffer playWords = new StringBuffer();
						for (int i = 1; i < rowColumns.length; i++) {
							if (t_fr == 0) {
								additonalDataInfoText2.append(rowColumns[i]);
								t_fr++;
							} else {
								additonalDataInfoText2.append(",").append(rowColumns[i]);
								t_fr++;
							}

						}

						leadInfoDetailEntity.setAdditonalDataInfoDate(additonalDataInfoDate.toString());
						leadInfoDetailEntity.setAdditonalDataInfoNumeric(additonalDataInfoNumeric.toString());
						leadInfoDetailEntity.setAdditonalDataInfoText2(additonalDataInfoText2.toString());
						leadInfoDetailEntity.setPlayWords(playWords.toString());
						leadInfoDetailEntity.setStatus(CREATED);
						leadInfoDetailEntity.setAttemptNum(0);
						if (markDuplicate) {
							leadInfoDetailEntity.setPhoneNumberStatus(DUPLICATE_NUMBER);
						} else if (!validatePhoneNumber(leadInfoDetailEntity.getPhoneNumber())) {
							leadInfoDetailEntity.setPhoneNumberStatus(INVALID_NUMBER);
						} else if (isBlackListed(leadInfoDetailEntity.getPhoneNumber(), leadInfo.getUserId())) {
							leadInfoDetailEntity.setPhoneNumberStatus(BLACKLIST_NUMBER);
						} else if (isDNDCheck == true && leadInfoDetailEntity.getPhoneNumber() != null
								&& getDNDStatus(leadInfoDetailEntity.getPhoneNumber()) == true) {
							leadInfoDetailEntity.setPhoneNumberStatus(DND);

						} else if (rcsEnabled.equalsIgnoreCase(OK)) {
							if (isNonRcs(rowColumns[0]) == true) {
								leadInfoDetailEntity.setPhoneNumberStatus(OK);

							} else {
								leadInfoDetailEntity.setPhoneNumberStatus(NONRCS_NUMBER);
							}
						} else {
							leadInfoDetailEntity.setPhoneNumberStatus(NONRCS_NUMBER);
						}

						// We have to create message queue base on LeadId then we have to send to Rabbit
						// MQ
						// VHost

						leadInfoDetailEntityList.add(leadInfoDetailEntity);
					}
				}
			}
			System.out.println("LeadINFOSize:: " + leadInfoDetailEntityList.size());
			if (leadInfoDetailEntityList.size() > 0) {
				//Random random = new Random();
				//int randomTime = random.nextInt(30000) + 10000;
				//LOGGER.info("RCS Thread Sleep Time:: " + randomTime);
				Thread.sleep(60000);

				BulkTokenResponse bulkTokenResponse = updateRcsNoAfterBulkApiResponse(userEntity,
						leadInfoDetailEntityList, pojo, leadInfo);
				if (Objects.nonNull(bulkTokenResponse) && bulkTokenResponse.getRcsEnabledContacts().size() > 0) {
					//&& !bulkTokenResponse.getRcsEnabledContacts().get(0).equals("429")
					LOGGER.info("Going On Set OK Status.......");

					for (LeadInfoDetailEntity leadInfoDetailEntity : leadInfoDetailEntityList) {
						bulkTokenResponse.getRcsEnabledContacts().stream()
								.filter(phone -> phone.contains(leadInfoDetailEntity.getPhoneNumber()))
								.forEach(phone -> leadInfoDetailEntity.setPhoneNumberStatus("OK"));

					}

				}

				/*
				 * else {
				 *//**
					 * If We Will GET 429 Status Then
					 *//*
						 * LOGGER.info("Going On Set 429 Status.......");
						 * leadInfoDetailEntityList.stream() .forEach(leadInfoDetail ->
						 * leadInfoDetail.setPhoneNumberStatus("RCS_ERROR"));
						 * 
						 * }
						 */
			}
			totaltime = System.currentTimeMillis() - starttime;
			LOGGER.info("Total time in making [" + leadInfoDetailEntityList.size() + "] records is [" + totaltime
					+ "] Ms   LeadId:" + leadInfo.getLeadId() + " ThreadId:" + Thread.currentThread().getId());
			starttime = System.currentTimeMillis();

			jdbcTemplate.batchUpdate(
					"insert into lead_info_detail (status, lead_id, phone_number, attempt_num, ADDITIONAL_DATA_INFO_NUMERIC,"
							+ " ADDITIONAL_DATA_INFO_DATE, ADDITIONAL_DATA_INFO_TEXT2, ADDITIONAL_DATA_INFO_TEXT, cli, language, "
							+ " phone_number_status, next_call_dtm, retry_attempt, retry_duration, retry_type, play_words ,"
							+ " created_by, created_date, last_modified_by, last_modified_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					leadInfoDetailEntityList, 10000, (ps, argument) -> {
						ps.setString(1, argument.getStatus());
						ps.setLong(2, argument.getLeadId());
						ps.setString(3, argument.getPhoneNumber());
						ps.setInt(4, argument.getAttemptNum() != null ? argument.getAttemptNum() : 0);
						ps.setString(5, argument.getAdditonalDataInfoNumeric());
						ps.setString(6, argument.getAdditonalDataInfoDate());
						ps.setString(7, argument.getAdditonalDataInfoText2());
						ps.setString(8, argument.getAdditonalDataInfoText());
						ps.setString(9, argument.getCli());
						ps.setString(10, argument.getLanguage());
						ps.setString(11, argument.getPhoneNumberStatus());
						ps.setTimestamp(12, convertJavaDateToSqlDate(argument.getNextCallDtm()));
						ps.setInt(13, argument.getRetryAttempt() != null ? argument.getRetryAttempt() : 0);
						ps.setInt(14, argument.getRetryDuration() != null ? argument.getRetryDuration() : 0);
						ps.setString(15, argument.getRetryType());
						ps.setString(16, argument.getPlayWords());
						ps.setString(17, argument.getCreatedBy());
						ps.setTimestamp(18, convertJavaDateToSqlDate(argument.getCreatedDate()));
						ps.setString(19, argument.getLastModifiedBy());
						ps.setTimestamp(20, convertJavaDateToSqlDate(argument.getLastModifiedDate()));
					});
			// leadInfoDetailRepository.saveAll(leadInfoDetailEntityList);

			totaltime = System.currentTimeMillis() - starttime;

			LOGGER.info("Total time in saving [" + leadInfoDetailEntityList.size() + "] records is [" + totaltime
					+ "] Ms  ThreadId:" + Thread.currentThread().getId());

		} catch (Exception e) {
			LOGGER.error("Got Exception ", e);
		}

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

	public java.sql.Timestamp convertJavaDateToSqlDate(Date date) {
		return new java.sql.Timestamp(date.getTime());
	}

	private boolean validatePhoneNumber(String phoneNumber) {
		boolean isValid = true;
		if (phoneNumber == null) {
			return false;
		}

		if (phoneNumber.length() != 10)
			return false;
//        if (phoneNumber.startsWith("+")) {
//            return true;
//        }
//        if (phoneNumber.length() != 10 ) {
//            return false;
//        }
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
		long starttime = System.currentTimeMillis();
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

	/**
	 * This method is useful for check bulk rcs or not First get count mno based on
	 * active lead then divide each request to 5000 or below
	 */

	public BulkTokenResponse updateRcsNoAfterBulkApiResponse(UserEntity userEntity,
			List<LeadInfoDetailEntity> leadInfoDetailEntityList, TokenPojo pojo, LeadInfo leadInfoEntity) {
		try {
			List<String> noCodeList = leadInfoDetailEntityList.stream()
					.map(listIndoDetail -> listIndoDetail.getPhoneNumber()).collect(Collectors.toList());
			LOGGER.info("Started for updating updateRcsNoAfterBulkApiResponse() got Size " + noCodeList.size()
					+ " AND Thread Name::" + Thread.currentThread().getName());

			// String noListJson = new Gson().toJson(noCodeList).toString();
			String rcsEnableContactsResponse = null;
			List<String> listNoWithIndiaCode = new ArrayList<>();
			for (String phoneNumber : noCodeList) {
				// System.out.println(phoneNumber);
				if (phoneNumber.length() == 10)
					listNoWithIndiaCode.add("+91" + phoneNumber.substring(phoneNumber.length() - 10));
			}
			rcsEnableContactsResponse = botTokenAPIService.checkBulkRcsNumber(pojo.getAccess_token(),
					listNoWithIndiaCode, userEntity.getBotId());

			if (rcsEnableContactsResponse.contains("rcsEnabledContacts")) {
				return new Gson().fromJson(rcsEnableContactsResponse, BulkTokenResponse.class);

			}
			// LOGGER.info("Ended for updating updateRcsNoAfterBulkApiResponse()");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
