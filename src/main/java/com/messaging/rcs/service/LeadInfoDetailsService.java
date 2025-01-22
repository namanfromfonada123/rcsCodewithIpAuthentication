package com.messaging.rcs.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.api.services.rcsbusinessmessaging.v1.model.BatchGetUsersResponse;
import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.repository.BlackListRepository;

@Service
public class LeadInfoDetailsService {
	private static final Logger LOGGER = Logger.getLogger(LeadInfoDetailsService.class.getName());
	private static final String DUPLICATE_NUMBER = "DUPLICATE_NUMBER";
	private static final String INVALID_NUMBER = "INVALID_NUMBER";
	public static final String NONRCS_NUMBER = "NONRCS_NUMBER";
	private static final String BLACKLIST_NUMBER = "BLACKLIST_NUMBER";
	private static final String CREATED = "Created";
	private static final String OK = "OK";

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private BlackListRepository blackListRepository;

	public void process(List<List<String>> dataList, LeadInfoEntity leadInfo, boolean markDuplicate, UserEntity userEntity) {
		long starttime = System.currentTimeMillis();
		try {
			List<String> list=null;
			List<Integer> dateIndexes = new ArrayList<>();
			List<Integer> numIndexes = new ArrayList<>();
			List<Integer> textIndexes = new ArrayList<>();
			List<Integer> langIndexes = new ArrayList<>();
			List<Integer> playIndexes = new ArrayList<>();
			int phoneNumberIdx = -1;
			int phoneIdx = -1;
			List<LeadInfoDetailEntity> leadInfoDetailEntityList = new ArrayList<>();
			// LOGGER.info("LeadInfo-> " + leadInfo);
			Date now = new Date();
			long totaltime = 0;
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
						String phoneNumber = null;
						if (phoneIdx == -1) {
							phoneNumber = rowColumns[0];
						} else {
							phoneNumber = rowColumns[phoneIdx];
						}
						leadInfoDetailEntity.setPhoneNumber(phoneNumber);

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

							if (dateIndexes.contains(i)) {
								if (d_fr == 0) {
									additonalDataInfoDate.append(rowColumns[i]);
									d_fr++;
								} else {
									additonalDataInfoDate.append("~").append(rowColumns[i]);
									d_fr++;
								}
							} else if (numIndexes.contains(i)) {
								if (n_fr == 0) {
									additonalDataInfoNumeric.append(rowColumns[i]);
									n_fr++;
								} else {
									additonalDataInfoNumeric.append("~").append(rowColumns[i]);
									n_fr++;
								}
							} else if (playIndexes.contains(i)) {
								if (p_fr == 0) {
									playWords.append(rowColumns[i]);
									p_fr++;
								} else {
									playWords.append("^").append(rowColumns[i]);
									p_fr++;
								}
							} else if (textIndexes.contains(i)) {
								if (t_fr == 0) {
									additonalDataInfoText2.append(rowColumns[i]);
									t_fr++;
								} else {
									additonalDataInfoText2.append("~").append(rowColumns[i]);
									t_fr++;
								}

							} else if (langIndexes.contains(i)) {
								leadInfoDetailEntity.setLanguage(rowColumns[i]);
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
						} else if (!validatePhoneNumber(phoneNumber)) {
							leadInfoDetailEntity.setPhoneNumberStatus(INVALID_NUMBER);
						} else if (isBlackListed(phoneNumber, leadInfo.getUserId(), userEntity.getParentUserId())) {
							leadInfoDetailEntity.setPhoneNumberStatus(BLACKLIST_NUMBER);
						} else if (isNonRcs(phoneNumber)) {
							leadInfoDetailEntity.setPhoneNumberStatus(NONRCS_NUMBER);
						} else {
							leadInfoDetailEntity.setPhoneNumberStatus(OK);
						}
						leadInfoDetailEntityList.add(leadInfoDetailEntity);
					}
				}
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
					leadInfoDetailEntityList, 10001, (ps, argument) -> {
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
						ps.setDate(12, convertJavaDateToSqlDate(argument.getNextCallDtm()));
						ps.setInt(13, argument.getRetryAttempt() != null ? argument.getRetryAttempt() : 0);
						ps.setInt(14, argument.getRetryDuration() != null ? argument.getRetryDuration() : 0);
						ps.setString(15, argument.getRetryType());
						ps.setString(16, argument.getPlayWords());
						ps.setString(17, argument.getCreatedBy());
						ps.setDate(18, convertJavaDateToSqlDate(argument.getCreatedDate()));
						ps.setString(19, argument.getLastModifiedBy());
						ps.setDate(20, convertJavaDateToSqlDate(argument.getLastModifiedDate()));
					});

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

	private boolean isBlackListed(String phoneNumber, long userId, long parentUserId) {
		boolean isBlackList = false;
		// long starttime = System.currentTimeMillis();
	//	String prefix = phoneNumber.substring(0, 3);
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
		if (!phoneNumber.startsWith("+91")) {
			phoneNumber = "+91" + phoneNumber;
		}
		List<String> numbers = new ArrayList<>();
		numbers.add(phoneNumber);
		try {
			BatchGetUsersResponse users = null;// rbmApiHelper.getUsers(numbers);
			if (users.getReachableRandomSampleUserCount() == 0) {
				isNonRcs = true;
			}
		} catch (Exception e) {
			isNonRcs = true;
		}
		return isNonRcs;
	}
}
