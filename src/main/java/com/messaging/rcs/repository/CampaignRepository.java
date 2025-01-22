package com.messaging.rcs.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.CampaignEntity;

/**
 * Created by Rahul Kumar on 2023/06/09.
 */
@Repository
public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {

	@Query(value = "select c from CampaignEntity c where c.campaignName = ?1 and c.isDeleted = 0")
	CampaignEntity getCampaignByCampaignName(String name);

	@Query("select c from CampaignEntity c where c.userId = ?1 ")
	public List<CampaignEntity> getCampaignByUserId(Long userId);

	@Query("select c from CampaignEntity c where c.userId in ( ?1 ) order by c.campaignId desc ")
	public List<CampaignEntity> getCampaignByUserIdPageable(List<Long> userIds, Pageable pageable);

	@Query("select c from CampaignEntity c where c.userId in ( ?1 ) ")
	public List<CampaignEntity> getCampaignByUserIds(List<Long> userIds);

	@Query("select c from CampaignEntity c where c.userId = ?1 and lower(c.campaignStatus) = 'active' ")
	public List<CampaignEntity> getActiveCampaignsByUserId(Long userId);

	@Query("select c from CampaignEntity c where lower(c.campaignName) = lower(?1) and c.userId = ?2 and c.isDeleted=0 ")
	public CampaignEntity getCampaignByNameAndUserId(String campaignName, long userId);

	CampaignEntity getFirstByCampaignName(String campaignName);

	@Query("select c from CampaignEntity c where c.campaignName = ?1")
	public List<CampaignEntity> getListOfCampaignByCampaignName(String campaignName);

	@Modifying
	@Transactional
	@Query("update CampaignEntity c set c.campaignStatus = ?2 where c.campaignId =  ?1")
	void updateCampaignStatus(long campaignId, String status);

	@Modifying
	@Transactional
	@Query("update CampaignEntity c set c.campaignStatus = ?2 , c.campaignEndTime = ?3 where c.campaignId =  ?1")
	void updateCampaignStatusToComplete(long campaignId, String completed, Date endDtm);

	@Query("select c from CampaignEntity c where lower(c.campaignName) = lower(?1)")
	public CampaignEntity getCampaignByName(String campaignName);

	CampaignEntity getByCampaignId(Long campaignId);

	CampaignEntity getCampaignEntityByCampaignId(Long campaignId);

	@Query(value = "select * from campaign where user_id =:userId and created_date BETWEEN :startDate AND :endDate", nativeQuery = true)
	List<CampaignEntity> getAllByUserIdAndStartDate(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId, Pageable pageable);

	@Query(value = "select * from campaign where user_id =:userId and created_date BETWEEN :startDate AND :endDate and template_id=:templateId", nativeQuery = true)
	List<CampaignEntity> getAllByTemplateIdAndBetweenStartDate(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId, @Param("templateId") String templateId,
			Pageable pageable);

	@Query(value = "select * from campaign where user_id =:userId and created_date BETWEEN :startDate AND :endDate and campaign_id=:campaignId", nativeQuery = true)
	List<CampaignEntity> getAllByCampaignIdAndBetweenStartDate(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId, @Param("campaignId") String campaignId,
			Pageable pageable);

	@Query(value = "select * from campaign where user_id =:userId and created_date BETWEEN :startDate AND :endDate and campaign_id=:campaignId and template_id=:templateId", nativeQuery = true)
	List<CampaignEntity> getAllByCampaignIdAndTemplateIdBetweenStartDate(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId, @Param("campaignId") String campaignId,
			@Param("templateId") String templateId, Pageable pageable);

	@Query(value = "select * from campaign where user_id =:userId and created_date BETWEEN :startDate AND :endDate", nativeQuery = true)
	List<CampaignEntity> getAllCampaignByStartAndDate(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId);

	@Query("select c from CampaignEntity c where  lower(c.campaignStatus) = 'active'  and c.isSendToRmq=0 ")
	public List<CampaignEntity> getActiveCampaignsByUserId();

	@Modifying
	@Transactional
	@Query("update CampaignEntity c set c.isSendToRmq = ?2 where c.campaignId =  ?1")
	public void updateCampaignIsSendToRmq(long campaignId, Integer isSendToRmq);

	@Query(value = "select count(*) from campaign where user_id =:userId and created_date >=:startDate AND created_date<=:endDate and campaign_id=:campaignId", nativeQuery = true)
	public Long countByCampaignId(@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("userId") Long userId, @Param("campaignId") String campaignId);

	@Query(value = "select count(*) from campaign where user_id =:userId and created_date >=:startDate AND created_date<=:endDate and template_id=:templateId", nativeQuery = true)
	public Long countByTemplateId(@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("userId") Long userId, @Param("templateId") String templateId);

	@Query(value = "select count(*) from campaign where user_id =:userId and created_date >=:startDate AND created_date<=:endDate and campaign_id=:campaignId and template_id=:templateId", nativeQuery = true)
	public Long countByTemplateIdAndCampaingId(@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("userId") Long userId, @Param("campaignId") String campaignId,
			@Param("templateId") String templateId);

	@Query(value = "select count(*) from campaign where user_id =:userId and created_date >=:startDate AND created_date<=:endDate", nativeQuery = true)
	public Long countByUserId(@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("userId") Long userId);

	@Query(value = "select template_id from campaign where campaign_id =:campaignId", nativeQuery = true)
	Long getTemplateIdByCampaignId(Long campaignId);

	@Query("select c from CampaignEntity c where c.userId = ?1 and c.templateId = ?2")
	public List<CampaignEntity> getCampaignByUserIdAndTemplateId(Long userId, Long templateId);

	@Query("select c from CampaignEntity c where c.campaignId = ?1 and c.userId = ?2 ")
	public Optional<CampaignEntity> getActiveCampaignsByUserIdAndCampaignId(Long campaignId, Long userId);

	/**
	 * 2024-06-25 with bot id query
	 * 
	 * @param startDate
	 * @param endDate
	 * @param userId
	 * @param campaignId
	 * @param templateId
	 * @return
	 */
	@Query(value = "select count(*) from campaign where user_id =:userId and created_date >=:startDate AND created_date<=:endDate and campaign_id=:campaignId and template_id in (:templateId)", nativeQuery = true)
	public Long countByTemplateIdAndCampaingIdAndBotId(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId, @Param("campaignId") String campaignId,
			@Param("templateId") List<Long> templateId);

	@Query(value = "select * from campaign where user_id =:userId and created_date BETWEEN :startDate AND :endDate and campaign_id=:campaignId and template_id in (:templateId)", nativeQuery = true)
	List<CampaignEntity> getAllByCampaignIdAndTemplateIdAndBotMappingId(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId, @Param("campaignId") String campaignId,
			@Param("templateId") List<Long> templateId, Pageable pageable);

	@Query(value = "select * from campaign where user_id =:userId and created_date BETWEEN :startDate AND :endDate and template_id=:templateId", nativeQuery = true)
	List<CampaignEntity> getAllByCampaignIdByBotMappingId(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userId") Long userId, @Param("templateId") List<Long> templateId,
			Pageable pageable);

	@Query(value = "select count(*) from campaign where user_id =:userId and created_date >=:startDate AND created_date<=:endDate and template_id in (:templateId)", nativeQuery = true)
	public Long countByBotMappingId(@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("userId") Long userId, @Param("templateId") List<Long> templateId);

}
