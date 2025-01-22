/*
 * package com.messaging.rcs.service;
 * 
 * import java.text.DateFormat; import java.text.SimpleDateFormat; import
 * java.util.ArrayList; import java.util.Collections; import java.util.Date;
 * import java.util.List; import java.util.stream.Collectors;
 * 
 * import org.apache.log4j.Logger; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.jdbc.core.JdbcTemplate; import
 * org.springframework.stereotype.Service; import
 * org.springframework.util.StringUtils;
 * 
 * import com.messaging.rcs.domain.LeadInfoDetailEntity; import
 * com.messaging.rcs.domain.UserEntity; import com.messaging.rcs.model.LeadInfo;
 * import com.messaging.rcs.repository.BlackListRepository; import
 * com.messaging.rcs.repository.LeadInfoDetailRepository; import
 * com.messaging.rcs.repository.UserRepository; import
 * com.messaging.rcs.schedular.BotTokenAPIService;
 * 
 *//**
	 * 
	 * @author RahulRajput
	 *
	 */
/*
 * @Service public class FlieReadLeadInofDetails { private static final Logger
 * LOGGER = Logger.getLogger(FlieReadLeadInofDetails.class); private static
 * final String DUPLICATE_NUMBER = "DUPLICATE_NUMBER"; private static final
 * String INVALID_NUMBER = "INVALID_NUMBER"; public static final String
 * NONRCS_NUMBER = "NONRCS_NUMBER"; private static final String BLACKLIST_NUMBER
 * = "BLACKLIST_NUMBER"; private static final String CREATED = "Created";
 * private static final String OK = "OK";
 * 
 * @Autowired private LeadInfoDetailRepository leadInfoDetailRepository;
 * 
 * @Autowired private BlackListRepository blackListRepository;
 * 
 * @Autowired private UserRepository userRepository;
 * 
 * @Autowired private JdbcTemplate jdbcTemplate;
 * 
 * private boolean markDuplicate = false;
 * 
 * @Autowired private BotTokenAPIService botTokenAPIService;
 * 
 * public void saveLeadInfoDetails(LeadInfo leadInfo, List<List<String>>
 * dataList) { LeadInfoDetailEntity leadInfoDetailEntity = null;
 * List<UserEntity> userList = userRepository.findAll();
 * List<LeadInfoDetailEntity> leadInfoDetailEntityList = new
 * ArrayList<LeadInfoDetailEntity>(); int rowNumber = 0; long totaltime = 0;
 * long starttime = System.currentTimeMillis(); List<UserEntity> userEntity =
 * null; try { for (List<String> row : dataList) { if (rowNumber == 0) {
 * 
 * row.removeAll(Collections.singleton("")); rowNumber++; } else { userEntity =
 * userList.stream().filter(user -> user.getUserId() == leadInfo.getLeadId())
 * .collect(Collectors.toList());
 * 
 * leadInfoDetailEntity = new LeadInfoDetailEntity();
 * leadInfoDetailEntity.setPhoneNumber(row.get(0).trim());
 * leadInfoDetailEntity.setLeadId(leadInfo.getLeadId());
 * 
 * DateFormat df = new SimpleDateFormat("dd-MM-yyyy"); //
 * LOGGER.info("LeadInfo-> " + leadInfo); Date now = new Date();
 * 
 * // LOGGER.info("Print "+line + "ThreadId:"+Thread.currentThread().getId());
 * leadInfoDetailEntity.setLeadId(leadInfo.getLeadId());
 * leadInfoDetailEntity.setStatus(CREATED);
 * leadInfoDetailEntity.setNextCallDtm(now); String phoneNumber = null;
 * 
 * leadInfoDetailEntity.setPhoneNumber(phoneNumber);
 * 
 * leadInfoDetailEntity.setCreatedDate(new Date());
 * leadInfoDetailEntity.setLastModifiedDate(new Date());
 * leadInfoDetailEntity.setLastModifiedBy(leadInfo.getLastModifiedBy());
 * leadInfoDetailEntity.setCreatedBy(leadInfo.getCreatedBy());
 * 
 * leadInfoDetailEntity.setAdditonalDataInfoDate("");
 * leadInfoDetailEntity.setAdditonalDataInfoNumeric("");
 * leadInfoDetailEntity.setAdditonalDataInfoText2("");
 * leadInfoDetailEntity.setPlayWords("");
 * leadInfoDetailEntity.setStatus(CREATED);
 * leadInfoDetailEntity.setAttemptNum(0); if (markDuplicate) {
 * leadInfoDetailEntity.setPhoneNumberStatus(DUPLICATE_NUMBER); } else if
 * (!validatePhoneNumber(phoneNumber)) {
 * leadInfoDetailEntity.setPhoneNumberStatus(INVALID_NUMBER); } else if
 * (isBlackListed(phoneNumber, leadInfo.getUserId(),
 * userEntity.get(0).getParentUserId())) {
 * leadInfoDetailEntity.setPhoneNumberStatus(BLACKLIST_NUMBER); } else if
 * (isNonRcs(phoneNumber, userEntity.get(0).getBotId(),
 * userEntity.get(0).getBotToken())) {
 * leadInfoDetailEntity.setPhoneNumberStatus(NONRCS_NUMBER); } else {
 * leadInfoDetailEntity.setPhoneNumberStatus(OK); }
 * leadInfoDetailEntityList.add(leadInfoDetailEntity); } }
 * 
 * totaltime = System.currentTimeMillis() - starttime;
 * LOGGER.info("Total time in making [" + leadInfoDetailEntityList.size() +
 * "] records is [" + totaltime + "] Ms   LeadId:" + leadInfo.getLeadId() +
 * " ThreadId:" + Thread.currentThread().getId()); starttime =
 * System.currentTimeMillis();
 * 
 * jdbcTemplate.batchUpdate(
 * "insert into lead_info_detail (status, lead_id, phone_number, attempt_num, ADDITIONAL_DATA_INFO_NUMERIC,"
 * +
 * " ADDITIONAL_DATA_INFO_DATE, ADDITIONAL_DATA_INFO_TEXT2, ADDITIONAL_DATA_INFO_TEXT, cli, language, "
 * +
 * " phone_number_status, next_call_dtm, retry_attempt, retry_duration, retry_type, play_words ,"
 * +
 * " created_by, created_date, last_modified_by, last_modified_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
 * , leadInfoDetailEntityList, 10001, (ps, argument) ->
 * 
 * { ps.setString(1, argument.getStatus()); ps.setLong(2, argument.getLeadId());
 * ps.setString(3, argument.getPhoneNumber()); ps.setInt(4,
 * argument.getAttemptNum() != null ? argument.getAttemptNum() : 0);
 * ps.setString(5, argument.getAdditonalDataInfoNumeric()); ps.setString(6,
 * argument.getAdditonalDataInfoDate()); ps.setString(7,
 * argument.getAdditonalDataInfoText2()); ps.setString(8,
 * argument.getAdditonalDataInfoText()); ps.setString(9, argument.getCli());
 * ps.setString(10, argument.getLanguage()); ps.setString(11,
 * argument.getPhoneNumberStatus()); ps.setTimestamp(12,
 * convertJavaDateToSqlDate(argument.getNextCallDtm())); ps.setInt(13,
 * argument.getRetryAttempt() != null ? argument.getRetryAttempt() : 0);
 * ps.setInt(14, argument.getRetryDuration() != null ?
 * argument.getRetryDuration() : 0); ps.setString(15, argument.getRetryType());
 * ps.setString(16, argument.getPlayWords()); ps.setString(17,
 * argument.getCreatedBy()); ps.setTimestamp(18,
 * convertJavaDateToSqlDate(argument.getCreatedDate())); ps.setString(19,
 * argument.getLastModifiedBy()); ps.setTimestamp(20,
 * convertJavaDateToSqlDate(argument.getLastModifiedDate())); }); //
 * leadInfoDetailRepository.saveAll(leadInfoDetailEntityList);
 * 
 * totaltime = System.currentTimeMillis() - starttime;
 * LOGGER.info("Total time in saving [" + leadInfoDetailEntityList.size() +
 * "] records is [" + totaltime + "] Ms  ThreadId:" +
 * Thread.currentThread().getId());
 * 
 * } catch (
 * 
 * Exception e) { LOGGER.error("Got Exception ", e); }
 * 
 * }
 * 
 *//**
	 * 
	 * @param authorization
	 * @param phoneNumber
	 * @param botId
	 * @return
	 *//*
		 * private boolean isNonRcs(String authorization, String phoneNumber, String
		 * botId) { boolean isNonRcs = false; if (!phoneNumber.startsWith("+91")) {
		 * phoneNumber = "+91" + phoneNumber; } List<String> numbers = new
		 * ArrayList<>(); numbers.add(phoneNumber); try { String response =
		 * botTokenAPIService.checkRcsNumber(authorization, phoneNumber, botId); if
		 * (response != null) { isNonRcs = true; } else { LOGGER.info("Number " +
		 * phoneNumber + " is RCS compatible"); } } catch (Exception e) { isNonRcs =
		 * true; } return isNonRcs; }
		 * 
		 * public java.sql.Timestamp convertJavaDateToSqlDate(Date date) { return new
		 * java.sql.Timestamp(date.getTime()); }
		 * 
		 * private boolean validatePhoneNumber(String phoneNumber) { boolean isValid =
		 * true; if (phoneNumber == null) { return false; }
		 * 
		 * if (phoneNumber.length() != 10) return false; // if
		 * (phoneNumber.startsWith("+")) { // return true; // } // if
		 * (phoneNumber.length() != 10 ) { // return false; // } try { long l =
		 * Long.parseLong(phoneNumber); if (l < 0) { isValid = false; } } catch
		 * (Exception e) { isValid = false; } return isValid;
		 * 
		 * }
		 * 
		 * private boolean isBlackListed(String phoneNumber, long userId, long
		 * parentUserId) { boolean isBlackList = false; long starttime =
		 * System.currentTimeMillis(); String prefix = phoneNumber.substring(0, 3);
		 * String phNumber = blackListRepository.isBlackList(phoneNumber, userId,
		 * Integer.valueOf(prefix).intValue());// , // parentUserId); // long totaltime
		 * = System.currentTimeMillis() - starttime; //
		 * LOGGER.info("Total time in checking blackList for phonenumber:"+phoneNumber+"
		 * // is ["+totaltime+"] Ms ThreadId:"+Thread.currentThread().getId()); if
		 * (StringUtils.hasLength(phNumber)) { LOGGER.info("Phone Number[" + phoneNumber
		 * + "] is BlackListed"); isBlackList = true; } return isBlackList; } }
		 */