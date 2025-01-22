package com.messaging.rcs.repository;

import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.LeadInfoEntity;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface LeadInfoRepository extends JpaRepository<LeadInfoEntity, Long> {
	@Query(value = "select lead_id  from lead_info  where lead_name= ?1", nativeQuery = true)
	Integer getLeadId(String paramString);

	@Query("select l  from LeadInfoEntity l where l.leadName = ?1 and l.leadCompletionStatus <> 'Deleted' ")
	List<LeadInfoEntity> getLeadByLeadName(String paramString);

	List<LeadInfoEntity> getLeadInfoEntityByLeadNameContains(String paramString);

	LeadInfoEntity findFirstByLeadName(String paramString);

	@Query("select li from LeadInfoEntity li where li.leadId = ?1 and li.userId in (?2)")
	LeadInfoEntity getLeadInfoEntityByLeadIdAndLeadUsAndUserIds(Long paramLong, List<Long> paramList);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 and lower(l.leadCompletionStatus) in ( 'created', 'running','temp pause') and (lower(l.leadStatus) = 'active' or (lower(l.leadCompletionStatus) = 'scheduled later' )) ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignId(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.leadStatus = 'Active' ")
	List<LeadInfoEntity> getAllActiveLeads();

	@Query("select l.leadId from LeadInfoEntity l where l.campaignId = ?1 order by l.createdDate asc")
	List<Long> getLeadIdsByCampaignId(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 and lower(l.leadCompletionStatus) in ( 'created', 'running', 'temp pause', 'scheduled later', 'run manually') and lower(l.leadStatus) = 'active' ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignId2(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummary(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.campaignId in (?1) ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryPageable(List<Long> paramList,
			Pageable paramPageable);

	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdOrderByLeadIdDesc(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 and lower(l.leadStatus) = ?2 ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndStatusForSummary(Long paramLong, String paramString);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 and lower(l.leadCompletionStatus) in ('running', 'pause','temp pause', 'scheduled later','isb') ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndStatusForSummaryScheduled(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.campaignId in (?1) and lower(l.leadCompletionStatus) in ('running', 'pause','temp pause', 'scheduled later','isb') ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndStatusForSummaryScheduledPageable(List<Long> paramList,
			Pageable paramPageable);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 and lower(l.leadCompletionStatus) in ('stopped', 'stop' , 'completed' , 'run manually','schedule expired') ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndStatusForSummaryNonScheduled(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.campaignId in (?1) and lower(l.leadCompletionStatus) in ('stopped', 'stop' , 'completed' , 'run manually','schedule expired') ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndStatusForSummaryNonScheduledPageable(List<Long> paramList,
			Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si \n where li.lead_schedule_id = si.schedule_id and li.lead_campaign_id in (?1) \n and  (((?2 <= si.SCHEDULE_START_DTM) or (?3 <= si.SCHEDULE_START_DTM))   \n OR  ((?4 <= si.SCHEDULE_END_DTM) or (?5 <= si.SCHEDULE_END_DTM)))", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryWithStartAndEndDateFilterPageable(
			List<Long> paramList, Date paramDate1, Date paramDate2, Date paramDate3, Date paramDate4,
			Pageable paramPageable);

	@Query("select l from LeadInfoEntity l where l.campaignId in (?1) and l.createdDate >= ?2 and l.createdDate <= ?3 and lower(l.leadCompletionStatus) in ('running', 'pause','temp pause', 'scheduled later','isb') ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryWithStartEndDateStatusFilterScheduledPageable(
			List<Long> paramList, Date paramDate1, Date paramDate2, Pageable paramPageable);

	@Query("select l from LeadInfoEntity l where l.campaignId in (?1) and l.createdDate >= ?2 and l.createdDate <= ?3 and lower(l.leadCompletionStatus) in ('stopped', 'stop' , 'completed' , 'run manually','schedule expired') ")
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryWithStartEndDateStatusFilterNonScheduledPageable(
			List<Long> paramList, Date paramDate1, Date paramDate2, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si \n where li.lead_schedule_id = si.schedule_id and li.lead_campaign_id = ?1 \n and  (((?2 <= si.SCHEDULE_START_DTM) or (?3 <= si.SCHEDULE_START_DTM))   \n OR  ((?3 <= si.SCHEDULE_END_DTM) or (?2 <= si.SCHEDULE_END_DTM)))", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryWithStartDateFilter(Long paramLong, Date paramDate1,
			Date paramDate2);

	@Query(value = "select li.* from lead_info li, schedule_info si \n where li.lead_schedule_id = si.schedule_id and li.lead_campaign_id in (?1) \n and  (((?2 <= si.SCHEDULE_START_DTM) or (?3 <= si.SCHEDULE_START_DTM))   \n OR  ((?3 <= si.SCHEDULE_END_DTM) or (?2 <= si.SCHEDULE_END_DTM)))", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryWithStartDateFilterPageable(List<Long> paramList,
			Date paramDate1, Date paramDate2, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.lead_campaign_id in (?1) and si.schedule_start_dtm >= ?2 and lower(li.lead_completion_status) in ('running', 'pause','temp pause', 'scheduled later','isb')  ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryWithStartDateStatusFilterScheduledPageable(
			List<Long> paramList, Date paramDate, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.lead_campaign_id in (?1) and si.schedule_start_dtm >= ?2 and lower(li.lead_completion_status) in ('stopped', 'stop' , 'completed' , 'run manually')  ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdForSummaryWithStartDateStatusFilterNonScheduledPageable(
			List<Long> paramList, Date paramDate, Pageable paramPageable);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 ")
	List<LeadInfoEntity> getAllLeadInfoEntitiesByCampaignId(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 ")
	List<LeadInfoEntity> getAllLeadInfoEntitiesByCampaignIdPageable(Long paramLong, Pageable paramPageable);

	@Query("select l from LeadInfoEntity l where l.leadId in (?1) ")
	List<LeadInfoEntity> getAllLeadInfoEntitiesByLeadIdsPageable(List<Long> paramList);

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 and lower(l.leadCompletionStatus) = ('running') ")
	List<LeadInfoEntity> getAllRunningLeadInfoEntitiesByCampaignId(Long paramLong);

	@Query(value = "select *  from lead_info  where lead_id= ?1", nativeQuery = true)
	LeadInfoEntity getLeadInfoEntityByLeadId(long paramLong);

	@Modifying
	@Transactional
	@Query(value = " Update lead_info l set l.lead_completion_status = ?2 where l.lead_id = ?1 limit 1", nativeQuery = true)
	void updateLeadCompletionStatus(long paramLong, String paramString);

	@Modifying
	@Transactional
	@Query(value = " Update lead_info l set l.lead_status = ?2 where l.lead_id = ?1 limit 1", nativeQuery = true)
	void updateLeadStatus(long paramLong, String paramString);

	@Modifying
	@Transactional
	@Query(value = " Update lead_info l set l.lead_completion_status = ?2, l.process_end_dtm = ?3 where l.lead_id = ?1 limit 1", nativeQuery = true)
	void updateLeadCompletionStatusAsPartialCompleted(long paramLong, String paramString, Date paramDate);

	@Modifying
	@Transactional
	@Query(value = "update lead_info li set li.lead_completion_status = 'Completed', li.process_end_dtm = ?2 where li.LEAD_ID = ?1 ", nativeQuery = true)
	void updateLeadCompletionStatusToCompleted2(Long paramLong, Date paramDate);

	@Modifying
	@Transactional
	@Query(value = "update lead_info li set li.lead_completion_status = 'Completed', li.process_end_dtm = ?2 where li.LEAD_RETRY_ID = ?1 ", nativeQuery = true)
	void updateLeadCompletionStatusToCompleted(Long paramLong, Date paramDate);

	@Query(value = "select count(li.lead_id) from lead_info li where li.LEAD_CAMPAIGN_ID = ?1", nativeQuery = true)
	int getLeadCountByCampaignId(long paramLong);

	List<LeadInfoEntity> findByCampaignId(Long paramLong);

	List<LeadInfoEntity> getLeadInfoEntityByCampaignId(Long paramLong);

	@Query(value = " select li.* from lead_info li where li.LEAD_COMPLETION_STATUS  in ('Running','Created')", nativeQuery = true)
	List<LeadInfoEntity> getActiveLeads();

	@Query(value = " select li.* from lead_info li where li.LEAD_COMPLETION_STATUS  in ('Completed', 'Stop', 'Stopped')", nativeQuery = true)
	List<LeadInfoEntity> getCompletedLeads();

	@Query("select li from LeadInfoEntity li where li.leadCompletionStatus = 'Kafka Error'")
	List<LeadInfoEntity> getErroneousLeads();

	@Query(value = " select li.* from lead_info li where li.LEAD_COMPLETION_STATUS  in ('Running')", nativeQuery = true)
	List<LeadInfoEntity> getRunningLeads();

	@Query(value = " select li.* from lead_info li where li.LEAD_COMPLETION_STATUS  in ('Running') and lower(li.LEAD_STATUS) = 'active' and li.LEAD_CAMPAIGN_ID = ?1 ", nativeQuery = true)
	List<LeadInfoEntity> getRunningLeadsForCampaign(Long paramLong);

	@Query("select li from LeadInfoEntity li where li.campaignId = ?1 and li.leadCompletionStatus in ('Running','Pause','Temp Pause','Paused','Stop','Stopped','Completed', 'ISB', 'Schedule Expired')")
	List<LeadInfoEntity> getApplicableLeadsForBalanceUpdate(Long paramLong);

	@Query(value = "select li.* from lead_info li, schedule_info si \n where li.lead_schedule_id = si.schedule_id and li.lead_user_id = ?1 \n and  (((?2 <= si.SCHEDULE_START_DTM) or (?3 <= si.SCHEDULE_START_DTM))   \n OR  ((?4 <= si.SCHEDULE_END_DTM) or (?5 <= si.SCHEDULE_END_DTM)))", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdForSummaryWithStartAndEndDateFilter(Long paramLong, Date paramDate1,
			Date paramDate2, Date paramDate3, Date paramDate4);

	@Query(value = "select li.* from lead_info li, schedule_info si \n where li.lead_schedule_id = si.schedule_id and li.lead_user_id in (?1) \n and  (((?2 <= si.SCHEDULE_START_DTM) or (?3 <= si.SCHEDULE_START_DTM))   \n OR  ((?4 <= si.SCHEDULE_END_DTM) or (?5 <= si.SCHEDULE_END_DTM)))", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdForSummaryWithStartAndEndDateFilterPageable(List<Long> paramList,
			Date paramDate1, Date paramDate2, Date paramDate3, Date paramDate4, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.LEAD_USER_ID = ?1 and si.schedule_start_dtm >= ?2 and si.schedule_start_dtm <= ?3 ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdAndCreatedByBetween(Long paramLong, Date paramDate1,
			Date paramDate2);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 and cd.call_start_date <= ?3) and li.LEAD_USER_ID = ?1", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdAndCreatedByBetweenExists(Long paramLong, Date paramDate1,
			Date paramDate2);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 and cd.call_start_date <= ?3) and li.LEAD_USER_ID in (?1)", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdAndCreatedByBetweenExistsPageable(List<Long> paramList,
			Date paramDate1, Date paramDate2, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si \n where li.lead_schedule_id = si.schedule_id and li.lead_user_id = ?1 \n and  (((?2 <= si.SCHEDULE_START_DTM) or (?3 <= si.SCHEDULE_START_DTM))   \n OR  ((?3 <= si.SCHEDULE_END_DTM) or (?2 <= si.SCHEDULE_END_DTM)))", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdAndCreatedByGreaterThanEqual(Long paramLong, Date paramDate1,
			Date paramDate2);

	@Query("select l from LeadInfoEntity l where l.userId in (?1) and l.createdDate >= ?2")
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdAndCreatedByGreaterThanEqualPageable(List<Long> paramList,
			Date paramDate, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2) and li.LEAD_USER_ID = ?1", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdAndCreatedByGreaterThanEqualExists(Long paramLong, Date paramDate);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2) and li.LEAD_USER_ID in (?1)", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdAndCreatedByGreaterThanEqualExistsPageable(List<Long> paramList,
			Date paramDate, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.LEAD_CAMPAIGN_ID = ?1 and si.schedule_start_dtm >= ?2 and si.schedule_start_dtm <= ?3 ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByBetween(Long paramLong, Date paramDate1,
			Date paramDate2);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.LEAD_CAMPAIGN_ID = ?1 and si.schedule_start_dtm >= ?2 and si.schedule_start_dtm <= ?3 ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByBetweenPageable(Long paramLong, Date paramDate1,
			Date paramDate2, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 and cd.call_start_date <= ?3) and li.LEAD_ID in (?1) ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByLeadIdsAndCreatedByBetweenExistsPageable(List<Long> paramList,
			Date paramDate1, Date paramDate2);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.LEAD_ID in  (?1 ) and (si.schedule_day like CONCAT(CONCAT('%', ?3), '%') or si.schedule_day = 0)and ( ?2 between cast(si.schedule_start_dtm as date ) and  si.schedule_end_dtm ) ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByLeadIdsAndCreatedByBetweenPageable(List<Long> paramList, Date paramDate,
			String paramString);

	@Query(value = "select li.* from lead_info li , schedule_info si where  li.lead_schedule_id = si.schedule_id and si.schedule_start_dtm >= ?2 and li.LEAD_ID in (?1) ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByLeadIdsAndCreatedByGreaterThanEqualPageable(List<Long> paramList,
			Date paramDate);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 ) and li.LEAD_ID in (?1)", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByLeadIdsAndCreatedByGreaterThanEqualExistsPageable(List<Long> paramList,
			Date paramDate);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 and cd.call_start_date <= ?3) and li.LEAD_CAMPAIGN_ID = ?1", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByBetweenExists(Long paramLong, Date paramDate1,
			Date paramDate2);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 and cd.call_start_date <= ?3) and li.LEAD_CAMPAIGN_ID = ?1", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByBetweenExistsPageable(Long paramLong,
			Date paramDate1, Date paramDate2, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.LEAD_CAMPAIGN_ID = ?1 and si.schedule_start_dtm >= ?2 ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByGreaterThanEqual(Long paramLong, Date paramDate);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.LEAD_CAMPAIGN_ID = ?1 and si.schedule_start_dtm >= ?2 ", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByGreaterThanEqualPageable(Long paramLong,
			Date paramDate, Pageable paramPageable);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 ) and li.LEAD_CAMPAIGN_ID = ?1", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByGreaterThanEqualExists(Long paramLong,
			Date paramDate);

	@Query(value = "select li.* from lead_info li where exists (select cd.* from call_detail_summary cd where li.lead_id = cd.lead_id and cd.call_start_date >= ?2 ) and li.LEAD_CAMPAIGN_ID = ?1", nativeQuery = true)
	List<LeadInfoEntity> getLeadInfoEntitiesByCampaignIdAndCreatedByGreaterThanEqualExistsPageable(Long paramLong,
			Date paramDate, Pageable paramPageable);

	List<LeadInfoEntity> getLeadInfoEntitiesByUserId(Long paramLong);

	@Query("select l from LeadInfoEntity l where l.userId in (?1)")
	List<LeadInfoEntity> getLeadInfoEntitiesByUserIdPageable(List<Long> paramList, Pageable paramPageable);

	@Modifying
	@Transactional
	@Query(value = "update lead_info li set li.lead_source_id = ?2, li.lead_status = 'Inactive' where li.lead_id = ?1 limit 1", nativeQuery = true)
	void markLeadInactiveAndUpdateLeadSourceId(Long paramLong, String paramString);

	List<LeadInfoEntity> getAllByUserIdAndCreatedDateBetween(Long paramLong, Date paramDate1, Date paramDate2);

	@Query(value = "select distinct li.LEAD_ID from lead_info li where li.LEAD_USER_ID in (?1)", nativeQuery = true)
	List<BigInteger> getLeadIdsByUserIds(List<Long> paramList);

	@Query(value = "select distinct li.LEAD_ID from lead_info li where li.LEAD_USER_ID in (?1) and li.lead_campaign_id = ?2 ", nativeQuery = true)
	List<BigInteger> getLeadIdsByUserIdsAndCampaignId(List<Long> paramList, Long paramLong);

	@Query("select distinct l.leadId from LeadInfoEntity l where l.userId in (?1) order by l.createdDate asc ")
	List<Long> getAllLeadIdsByUserIds(List<Long> paramList);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.LEAD_USER_ID =  ?1 and (si.schedule_day like CONCAT(CONCAT('%', ?3), '%') or si.schedule_day = 0)and ( ?2 between cast(si.schedule_start_dtm as date ) and  si.schedule_end_dtm ) ", nativeQuery = true)
	List<LeadInfoEntity> getTodaysLeads(long paramLong, Date paramDate, String paramString);

	List<LeadInfoEntity> findByLeadId(Long paramLong);

	@Query(value = "select * from lead_info where lead_user_id=:userId and created_date >=:startDate AND created_date<=:endDate", nativeQuery = true)
	List<LeadInfoEntity> getAllLeadInfoByUserIdAndDateBetween(@Param("startDate") String paramString1,
			@Param("endDate") String paramString2, @Param("userId") Long paramLong, Pageable paramPageable)
			throws Exception;

	@Query(value = "select * from lead_info where lead_user_id=:userId and created_date >=:startDate AND created_date<=:endDate and lead_campaign_id=:campaignId", nativeQuery = true)
	List<LeadInfoEntity> getAllLeadInfoByCampaignBetweenDate(@Param("startDate") String paramString1,
			@Param("endDate") String paramString2, @Param("userId") Long paramLong, Pageable paramPageable,
			@Param("campaignId") String paramString3) throws Exception;

	List<LeadInfoEntity> getAllByUserId(Long paramLong);

	@Query(value = "select count(*) from lead_info where lead_user_id=:userId and created_date >=:startDate AND created_date<=:endDate", nativeQuery = true)
	long countByUserIdAndCreatedDateBetween(@Param("startDate") String paramString1,
			@Param("endDate") String paramString2, @Param("userId") Long paramLong);

	@Query(value = "select count(*) from lead_info where lead_user_id=:userId and created_date >=:startDate AND created_date<=:endDate and lead_campaign_id=:campaignId", nativeQuery = true)
	long countByCampaignIdBetweenDate(@Param("startDate") String paramString1, @Param("endDate") String paramString2,
			@Param("userId") Long paramLong, @Param("campaignId") String paramString3);

	@Query(value = "select li.* from lead_info li, schedule_info si where li.lead_schedule_id = si.schedule_id and li.lead_status = 'Active'  and si.schedule_start_dtm between now()  and  si.schedule_end_dtm", nativeQuery = true)
	List<LeadInfoEntity> getAllActiveLeadsByCampaignIdAndUserId();

	@Query(value = "select * from lead_info where lead_user_id=:userId and created_date >=:startDate AND created_date<=:endDate", nativeQuery = true)
	List<LeadInfoEntity> findAllLeadInfoByUserIdAndDateBetween(@Param("startDate") String paramString1,
			@Param("endDate") String paramString2, @Param("userId") Long paramLong) throws Exception;

	@Query("select l from LeadInfoEntity l where l.campaignId = ?1 and l.userId = ?2 order by l.createdDate asc")
	List<LeadInfoEntity> getLeadIdAndNameByCampaignId(Long paramLong1, Long paramLong2);

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "SELECT lf.lead_name,lf.lead_id,cp.campaign_name,cp.campaign_id FROM rcsmessaging.lead_info lf INNER JOIN  rcsmessaging.campaign cp ON lf.lead_campaign_id = cp.campaign_id where lf.lead_user_id=:userId and lf.created_date >=:startDate AND lf.created_date<=:endDate", nativeQuery = true)
	List<Object[]> findAllLeadAndCampaignNameWithIds(@Param("startDate") String paramString1,
			@Param("endDate") String paramString2, @Param("userId") Long paramLong) throws Exception;

	@Query(value = "select count(*) from lead_info where lead_user_id=:userId and created_date >=:startDate AND created_date<=:endDate and lead_campaign_id=:campaignId and lead_id=:leadId", nativeQuery = true)
	long countByCampaignIdAndLeadIdBetweenDate(@Param("startDate") String paramString1,
			@Param("endDate") String paramString2, @Param("userId") Long paramLong,
			@Param("campaignId") String paramString3, @Param("leadId") String paramString4);

	@Query(value = "select * from lead_info where lead_user_id=:userId and created_date >=:startDate AND created_date<=:endDate and lead_campaign_id=:campaignId and lead_id=:leadId", nativeQuery = true)
	List<LeadInfoEntity> getAllLeadInfoByCampaignAndLeadIdBetweenDate(@Param("startDate") String paramString1,
			@Param("endDate") String paramString2, @Param("userId") Long paramLong, Pageable paramPageable,
			@Param("campaignId") String paramString3, @Param("leadId") String paramString4) throws Exception;

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "SELECT lfd.created_by,tmp.bot_id,tmp.template_code,li.lead_name,cp.campaign_name,cp.campaign_type,cp.data_source_name FROM rcsmessaging.lead_info_detail lfd INNER JOIN rcsmessaging.lead_info li ON li.lead_id=lfd.lead_id INNER JOIN  rcsmessaging.campaign cp ON cp.campaign_id = li.lead_campaign_id INNER JOIN rcsmessaging.template tmp ON tmp.id = cp.template_id where lfd.phone_number=:phoneNumber and lfd.status !='Created' order by lfd.lead_info_detail_id desc limit 1", nativeQuery = true)
	List<Object[]> getUserAndBotIdByMnoForDemoRcsService(@Param("phoneNumber") String paramString) throws Exception;

	@Query(value = "select *  from lead_info_detail where send_to_queue = 1 and lead_id=:leadId and lower(status) in ('created') and phone_number_status = 'OK'", nativeQuery = true)
	List<LeadInfoDetailEntity> findByLeadIdAndUserId(@Param("leadId") long paramLong);

	@Query(value = "select lead_id from lead_info where  created_date >=:startDate AND created_date<=:endDate", nativeQuery = true)
	List<Long> getAllLeadByCreatedDate(@Param("startDate") String paramString1, @Param("endDate") String paramString2);

	@Modifying
	@Transactional
	@Query(value = " Update lead_info l set l.lead_completion_status = ?2,l.lead_status= ?2 where l.lead_id = ?1 limit 1", nativeQuery = true)
	void updateLeadCompletionStatusAndStatus(long paramLong, String paramString);
}
