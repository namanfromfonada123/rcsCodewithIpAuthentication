package com.messaging.rcs.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.LeadInfoDetailEntity;

/**
 * 2023-06-01
 * 
 * @author RahulRajput
 *
 */
@Repository
public interface LeadInfoDetailRepository extends JpaRepository<LeadInfoDetailEntity, Long> {

	@Query(value = "select phone_number  from lead_info_detail partition(p?1) where lead_id= ?1", nativeQuery = true)
	List<String> getPhoneNumbers(Integer leadId);

	@Query(value = "select l.* from lead_info_detail partition(p?1) l where l.lead_id = ?1", nativeQuery = true)
	public List<LeadInfoDetailEntity> getLeadInfoDetailsByLeadId(Long leadId);

	@Query(value = "select l.* from lead_info_detail partition(p?1) l where l.lead_id = ?1 and l.phone_number_status = ?2", nativeQuery = true)
	public List<LeadInfoDetailEntity> getLeadInfoDetailsByLeadIdAndPhoneNumberStatus(Long leadId,
			String phoneNumberStatus);

	@Query(value = "select l.phone_number from lead_info_detail partition(p?1) l where l.lead_id = ?1 and l.phone_number_status = ?2", nativeQuery = true)
	public List<String> getPhoneNumberByLeadIdAndPhoneNumberStatus(Long leadId, String phoneNumberStatus);

	List<LeadInfoDetailEntity> getLeadInfoDetailEntityByLeadIdAndPhoneNumberStatusIn(Long leadId,
			List<String> phoneNumberStatus);

	List<LeadInfoDetailEntity> getLeadInfoDetailEntityByLeadIdAndStatusIn(Long leadId, List<String> status);

	@Query(value = "select u.* from lead_info_detail  partition(p?1) u where u.LEAD_ID = ?1 and u.LEAD_INFO_DETAIL_ID = ?2 ", nativeQuery = true)
	public LeadInfoDetailEntity getLeadInfoDetailEntityByLeadIdAndLeadInfoDetailId(Long leadId, Long leadInfoDetailId);

	LeadInfoDetailEntity getLeadInfoDetailEntityByLeadInfoDetailId(Long leadInfoDetailId);

	@Query(value = "select lid.* from lead_info_detail partition(p?1) lid where lid.lead_id =?1 and lid.phone_number_status = 'OK' limit 1", nativeQuery = true)
	List<LeadInfoDetailEntity> getLeadInfoDetailEntityByLeadId(Long leadId);

	@Query(value = "select lid.* from lead_info_detail partition(p?1) lid where lid.lead_id =?1 and lid.PHONE_NUMBER = ?2 and lid.phone_number_status = 'OK' limit 1 ", nativeQuery = true)
	LeadInfoDetailEntity getByLeadIdAndPhoneNumber(Long leadId, String phoneNumber);

	@Query(value = "select lid.* from lead_info_detail partition(p?1) lid where lid.lead_id =?1 and li.status = 'Dialed' or  lid.phone_number_status != 'OK' ", nativeQuery = true)
	List<LeadInfoDetailEntity> getDialedLeadInfoDetailIdsByLeadId(Long leadId);

	@Query(value = "select lid.* from lead_info_detail partition(p?1) lid and lid.status = 'Dialed' limit ?2 ", nativeQuery = true)
	List<LeadInfoDetailEntity> getAllDialedRecordsByLeadId(Long leadId, int limit);

	LeadInfoDetailEntity getFirstByLeadIdAndPhoneNumberAndAdditonalDataInfoText2Containing(Long leadId,
			String phoneNumber, String orderId);

	@Query(value = "select count(1)  from lead_info_detail partition(p?1)  u  where u.LEAD_ID= ?1 and lower(u.STATUS) not in ('created') and PHONE_NUMBER_STATUS = 'OK' ", nativeQuery = true)
	public Integer getInProcessLeadCountByLeadId(Long leadId);

	@Query(value = "select count(1)  from lead_info_detail partition(p?1) u  where u.LEAD_ID= ?1 and lower(u.STATUS) in ('created') and PHONE_NUMBER_STATUS = 'OK' limit 1", nativeQuery = true)
	public Integer getCreatedLeadCountByLeadId(Long leadId);

	@Query(value = "select count(1)  from lead_info_detail partition(p?1) u  where u.LEAD_ID= ?1 and lower(u.STATUS) in ('created', 'dialfail', 'tbredial', 'published', 'hold') and PHONE_NUMBER_STATUS = 'OK' ", nativeQuery = true)
	public Integer getUnDialedLeadCountByLeadId(Long leadId);

	@Query(value = "select count(1)  from lead_info_detail partition(p?1) u  where u.STATUS = 'Published' and PHONE_NUMBER_STATUS = 'OK' ", nativeQuery = true)
	public Integer getPublishedCountByLeadId(Long leadId);

	@Query(value = "select u.lead_info_detail_id  from lead_info_detail partition(p?1) u  where u.LEAD_ID= ?1 and u.ATTEMPT_NUM = ?2 and  lower(u.STATUS) in ('published','dialfail','hold') and PHONE_NUMBER_STATUS = 'OK' limit 1", nativeQuery = true)
	public Long getInProcessLeadCountByLeadIdByattemptNum(Long leadId, int attemptNum);

	@Query(value = "select u.lead_info_detail_id  from lead_info_detail partition(p?1) u  where u.LEAD_ID= ?1 and u.ATTEMPT_NUM < ?2 and  lower(u.STATUS) in ('tbredial') and PHONE_NUMBER_STATUS = 'OK' limit 1", nativeQuery = true)
	public Long getTBRedialLeadCountByLeadIdByRedialattemptNum(Long leadId, int attemptNum);

	@Query(value = "select u.lead_info_detail_id  from lead_info_detail partition(p?1) u  where u.LEAD_ID= ?1 and  (u.STATUS)  not in ('Dialed','FAILED', 'NO ANSWER', 'BUSY') and PHONE_NUMBER_STATUS = 'OK' limit 1", nativeQuery = true)
	public Long checkTBRedialNumberLeadId(Long leadId);

	@Query(value = "select *  from lead_info_detail partition(p?1)  where lead_id= ?1 and lower(status) in ( 'created', 'dialfail' ) and PHONE_NUMBER_STATUS = 'OK' limit ?2", nativeQuery = true)
	List<LeadInfoDetailEntity> getNewPhoneNumbersBasedOnFreeChannels(Long leadId, Integer limit);

	@Query(value = "select *  from lead_info_detail partition(p?1)  where lead_id= ?1 and lower(status) in ( 'created', 'dialfail', 'tbredial' ) and PHONE_NUMBER_STATUS = 'OK' "
			+ " and next_call_dtm <= ?3 limit ?2", nativeQuery = true)
	List<LeadInfoDetailEntity> getPhoneNumbersForRorNTypeRetry(Long leadId, Integer limit, Date now);

	@Query(value = "select lid.*  from lead_info_detail partition(p?1) lid  " + " where  lid.lead_id = ?1 "
			+ " and lid.status in ( 'TBRedial' ) and lid.attempt_num = ?3 " + " limit ?2", nativeQuery = true)
	List<LeadInfoDetailEntity> getTBRedialPhoneNumbersBasedOnFreeChannels(Long leadId, Integer limit,
			Integer attempt_num);

	@Query(value = "select lid.* from lead_info_detail partition(p?1) lid, lead_info li " + "where lid.lead_id = ?1 "
			+ "and lid.lead_id = li.lead_id "
			+ "and lower(li.lead_completion_status) != 'completed' limit ?2", nativeQuery = true)
	List<LeadInfoDetailEntity> getPhoneNumbersBasedOnNotCompleteStatus(Long leadId, Integer limit);

	@Query(value = "select * from lead_info_detail partition(p?1)  where lead_id= ?1 and PHONE_NUMBER in (select PHONE_NUMBER from lead_info_detail partition(p?1)  where lead_id= ?1 and phone_number_status = 'OK' group by PHONE_NUMBER HAVING COUNT(PHONE_NUMBER) > 1) ", nativeQuery = true)
	List<LeadInfoDetailEntity> getDuplicateNoInLead(Long leadId);

	@Query(value = "select min(attempt_num) from lead_info_detail  where lead_id= ?1 limit 1", nativeQuery = true)
	Integer getAttemptNum(Long leadId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?3) lid set lid.status = ?2 where lid.lead_id = ?3 and lid.LEAD_INFO_DETAIL_ID = ?1 limit 1", nativeQuery = true)
	void updateStatus(Long leadInfoDetailId, String status, long leadId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?3) lid set lid.play_words = ?2 where lid.lead_id = ?3  and lid.LEAD_INFO_DETAIL_ID = ?1 limit 1", nativeQuery = true)
	void updatePlayWord(Long leadInfoDetailId, String playWords, long leadId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?3) lid set lid.phone_number_status = ?2 where lid.LEAD_INFO_DETAIL_ID in (?1) and lid.lead_id = ?3", nativeQuery = true)
	void updateStatusBulk(List<Long> leadInfoDetailIds, String status, long leadId);

	@Query(value = "select phone_number_status,count(*) from lead_info_detail partition(p?1) l where lead_id = ?1 group by phone_number_status ", nativeQuery = true)
	List<Object> getLeadStats(Long leadid);

	@Query(value = "select count(*) from lead_info_detail partition(p?1) l where lead_id = ?1 and  phone_number_status in ('RCS_ERROR')", nativeQuery = true)
	Integer getLeadStatsForRCSError(Long leadid);

	@Query(value = "select count(*)  from lead_info_detail where lead_id=:leadId and lower(status) in ('created') and phone_number_status = 'NONRCS_NUMBER'", nativeQuery = true)
	long findMnoListCountByLeadId(@Param("leadId") long leadId);

	@Query(value = "select phone_number  from lead_info_detail where lead_id=:leadId and lower(status) in ('created') and phone_number_status = 'NONRCS_NUMBER' LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<String> findPhoneNumberBasedOnLimitAndOffset(@Param("leadId") long leadId, @Param("limit") long limit,
			@Param("offset") long offset);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail   lid set lid.status = ?2 where lid.LEAD_INFO_DETAIL_ID = ?1 and lid.lead_id = ?3 and lid.LAST_MODIFIED_DATE = ?4  limit 1", nativeQuery = true)
	void updateStatus2(Long leadInfoDetailId, String status, long leadId, Date now);

	@Query(value = "select min(lid.attempt_num) from lead_info_detail partition(p?1) lid where lid.lead_id = ?1 and lid.STATUS = 'TBRedial' ", nativeQuery = true)
	Integer getCurrentRediallingAttemptNumber(long leadId);

	@Modifying
	@Transactional
	@Query(value = "update lead_info_detail lid set  lid.cli = ?8, lid.status = ?7, lid.ATTEMPT_NUM = ?2, lid.ADDITIONAL_DATA_INFO_TEXT = ?3, lid.LAST_MODIFIED_DATE = ?4, lid.LAST_MODIFIED_BY = ?5"
			+ " where lid.LEAD_INFO_DETAIL_ID = ?1 and lid.lead_id = ?6 limit 1", nativeQuery = true)
	void updateLeadInfoDetailEntityObject(List<Long> leadInfoDetailIds, Integer attemptNum, String strIvrInfo,
			Date modifiedDate, String modifiedBy, long leadId, String status, String cli);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1)   lid set lid.STATUS = 'TBRedial' where lid.lead_id = ?1 and lid.LEAD_INFO_DETAIL_ID = ?2 limit 1", nativeQuery = true)
	void markNumberTBRedialIfNecessary(long leadId, long leadInfoDetailId, Integer retryAttemptCount);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1) lid set lid.STATUS = 'TBRedial', lid.next_call_dtm = ?3 where lid.lead_id = ?1 and lid.LEAD_INFO_DETAIL_ID = ?2  limit 1", nativeQuery = true)
	void markNumberTBRedialIfNecessaryWithNextCallTime(long leadId, long leadInfoDetailId, Date nextCallDtm);

	@Transactional
	@Modifying
	@Query(value = "delete from lead_info_detail  lid where lid.lead_id  = ?1 ", nativeQuery = true)
	void deleteByLeadId(Long leadId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail  partition(p?3)  lid set lid.status = ?2 where lid.LEAD_INFO_DETAIL_ID = ?1 and lid.lead_id = ?3 and lower(lid.status) = 'published' limit 1", nativeQuery = true)
	void updateStatusIfInPublishedStatus(long leadInfoDetailId, String dialFail, long leadId);

	LeadInfoDetailEntity getFirstByLeadIdAndStatus(Long leadId, String status);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1)  lid set lid.status = 'TBRedial' where  lid.lead_id = ?1 and lid.status = ?2 ", nativeQuery = true)
	void updateCdrsAsTBRedialedWithStatus(Long leadId, String status);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1)  lid set lid.status = 'Dialed' where  lid.phone_number = ?2 and lid.phone_number_status = 'OK' ", nativeQuery = true)
	void updateCdrsAsDialedWithStatus(Long leadId, String phoneNumber);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1)  lid set lid.attempt_num = ?3, lid.status = 'TBRedial' where  lid.lead_id = ?1 and lid.status = ?2 ", nativeQuery = true)
	void updateAttemptNumberForStatus(Long leadId, String status, int increment);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1)  lid set lid.attempt_num = ?3 where  lid.lead_id = ?1 and lid.status = ?2 ", nativeQuery = true)
	void updateAttemptNumberOnly(Long leadId, String status, int increment);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1)  lid set lid.status = 'Discarded' where lid.status in ('Created', 'TBRedial') and PHONE_NUMBER_STATUS = 'OK' ", nativeQuery = true)
	void discardRemainingRecords(long leadId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?1)  lid set lid.status = 'Hold' where lid.status in ('Published') and PHONE_NUMBER_STATUS = 'OK' ", nativeQuery = true)
	void holdPublishedRecords(long leadId);

	@Query(value = "select count(*)  from lead_info_detail partition(p?1)  where  status = 'Discarded' ", nativeQuery = true)
	int getDiscardedCount(Long leadId);

	@Query(value = "select *  from lead_info_detail where lead_id=:leadId and lower(status) in ('created') and phone_number_status = 'OK'", nativeQuery = true)
	List<LeadInfoDetailEntity> findByLeadIdAndUserId(@Param("leadId") long leadId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail partition(p?3) lid set lid.status = ?2 where lid.lead_id = ?3 and lid.LEAD_INFO_DETAIL_ID = ?1 limit 1", nativeQuery = true)
	void updateStatusByLeadInfoDetailIdAndStatusAndLeadId(Long leadInfoDetailId, String status, long leadId);

	@Query(value = "select count(*)  from lead_info_detail where created_by=:createdBy and created_date BETWEEN :startDate AND :endDate", nativeQuery = true)
	long countByCreatedByAndBetweenCreatedDate(@Param("createdBy") String createdBy,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update lead_info_detail lid set lid.phone_number_status = ?2 where lid.lead_id = ?3 and lid.phone_number = ?1", nativeQuery = true)
	void updatePhoneNumberStatusByLeadIdAndPhoneNumber(String phoneNumber, String status, long leadId);

	@Query(value = "select *  from lead_info_detail where lead_info_detail_id=:lead_info_detail_id and lower(status) in ('created') and phone_number_status = 'OK'", nativeQuery = true)
	List<LeadInfoDetailEntity> findByLeadInfoDetailIdForPaisabazzarPro(
			@Param("lead_info_detail_id") String lead_info_detail_id);

	@Query(value = "select SQL_CALC_FOUND_ROWS  date(lid.created_date)created_date,info.lead_name,date(lid.last_modified_date)last_modified_date,lid.status,COUNT(*)TOTAL,\r\n"
			+ "(select cmp.campaign_name from rcsmessaging.campaign cmp where cmp.campaign_id=info.lead_campaign_id limit 1)campaing_name,\r\n"
			+ "SUM(CASE WHEN lid.phone_number_status='NONRCS_NUMBER'  THEN 1 ELSE 0 END) NonRCS_FAILED,\r\n"
			+ "SUM(CASE WHEN lid.phone_number_status='OK'  THEN 1 ELSE 0 END) SUBMITTED ,\r\n"
			+ "SUM(CASE WHEN lid.status='Delivered' or lid.status='sent' or lid.status like 'Displayed%'  or lid.status='read'  THEN 1 ELSE 0 END) sent,\r\n"
			+ "SUM(CASE WHEN lid.status='Delivered'  or lid.status like 'Displayed%'  or lid.status='read' THEN 1 ELSE 0 END) Delivered, \r\n"
			+ "SUM(CASE WHEN lid.status like 'Displayed%' or lid.status='read' THEN 1 ELSE 0 END) `Read`,\r\n"
			+ "SUM(CASE WHEN lid.status='failed'  THEN 1 ELSE 0 END) failed,\r\n"
			+ "SUM(CASE WHEN lid.phone_number_status='INVALID_NUMBER'  THEN 1 ELSE 0 END) Invalid,lid.created_by,lid.lead_id,info.rcs_msg_type_id,\r\n"
			+ "SUM(CASE WHEN lid.send_to_queue='3'  THEN 1 ELSE 0 END) nonrcs_fallback_sms_submit,SUM(CASE WHEN lid.send_to_queue='2'  THEN 1 ELSE 0 END) ,\r\n"
			+ "SUM(CASE WHEN lid.status='DELIVERY_FAILED'  THEN 1 ELSE 0 END) DELIVERY_FAILED,\r\n"
			+ "SUM(CASE WHEN lid.status='SUBMIT_FAILED'  THEN 1 ELSE 0 END) SUBMIT_FAILED,\r\n"
			+ "SUM(CASE WHEN lid.status='DELIVERY_SUCCESS'  THEN 1 ELSE 0 END) DELIVERY_SUCCESS\r\n"
			+ "from  rcsmessaging.lead_info_detail   lid INNER JOIN  rcsmessaging.lead_info info ON info.lead_id = lid.lead_id\r\n"
			+ "where  lid.lead_id=:leadId GROUP BY 1,2,6", nativeQuery = true)
	public List<Object[]> getRcsSummary(@Param("leadId") Long leadId);

}
