package com.messaging.rcs.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.model.Template;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and inserttime BETWEEN :from AND :to and template_user_id =:userId", nativeQuery = true)
	List<Template> findAllByUserId(@Param("from") String from, @Param("to") String to, Pageable pageable,
			@Param("userId") Long userId, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id =:userId", nativeQuery = true)
	List<Template> findAllByUserIdWithoutDateRange(Pageable pageable, @Param("userId") Long userId,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	/*
	 * @Transactional
	 * 
	 * @Query(value = "select * from template where  template_user_id =:userId",
	 * nativeQuery = true)
	 */
	List<Template> findAllByTemplateUserIdAndStatusAndRcsMsgTypeId(Long templateUserId, Integer status,
			String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where inserttime BETWEEN :from AND :to and template_user_id=:templateUserId and rcs_msg_type_id=:rcsMsgTypeId", nativeQuery = true)
	List<Template> findAllByTemplateUserIdWithDateRange(@Param("from") String from, @Param("to") String to,
			@Param("templateUserId") Long templateUserId, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	// old
	@Transactional
	@Query(value = "select * from template where inserttime BETWEEN :from AND :to and template_user_id=:templateUserId and rcs_msg_type_id=:rcsMsgTypeId", nativeQuery = true)
	List<Template> findAllByTemplateUserId(@Param("from") String from, @Param("to") String to,
			@Param("templateUserId") Long templateUserId, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	// new
	@Transactional
	@Query(value = "select * from template where  template_user_id=:templateUserId and rcs_msg_type_id=:rcsMsgTypeId", nativeQuery = true)
	List<Template> findAllByTemplateUserIdWithoutDateRange(@Param("templateUserId") Long templateUserId,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	Template findFirstByTemplateCodeOrderById(String templateCode);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and inserttime BETWEEN :from AND :to and template_user_id=:templateUserId", nativeQuery = true)
	long countByTemplateUserIdWithDateRange(@Param("from") String from, @Param("to") String to,
			@Param("templateUserId") Long templateUserId, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id=:templateUserId", nativeQuery = true)
	long countByTemplateUserId(@Param("templateUserId") Long templateUserId,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and  inserttime BETWEEN :from AND :to and template_user_id=:templateUserId and status=:status", nativeQuery = true)
	long countByTemplateUserIdAndStatusWithDateRange(@Param("from") String from, @Param("to") String to,
			@Param("templateUserId") Long templateUserId, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id=:templateUserId and status=:status", nativeQuery = true)
	long countByTemplateUserIdAndStatus(@Param("templateUserId") Long templateUserId, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and  inserttime BETWEEN :from AND :to and template_user_id=:templateUserId and template_code=:templateCode", nativeQuery = true)
	long countByTemplateUserIdAndTemplateCodeWithDateRange(@Param("from") String from, @Param("to") String to,
			@Param("templateUserId") Long templateUserId, @Param("templateCode") String templateCode,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id=:templateUserId and template_code=:templateCode", nativeQuery = true)
	long countByTemplateUserIdAndTemplateCode(@Param("templateUserId") Long templateUserId,
			@Param("templateCode") String templateCode, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and inserttime BETWEEN :from AND :to and template_user_id=:templateUserId and template_code=:templateCode and status=:status", nativeQuery = true)
	long countByTemplateUserIdAndStatusAndTemplateCodeWithDateRange(@Param("from") String from, @Param("to") String to,
			@Param("templateUserId") Long templateUserId, @Param("templateCode") String templateCode,
			@Param("status") String status, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId  and template_user_id=:templateUserId and template_code=:templateCode and status=:status", nativeQuery = true)
	long countByTemplateUserIdAndStatusAndTemplateCode(@Param("templateUserId") Long templateUserId,
			@Param("templateCode") String templateCode, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId  and template_user_id=:templateUserId and template_code=:templateCode and status=:status and bot_id=:botId", nativeQuery = true)
	long countByTemplateUserIdAndStatusAndTemplateCodeAndBotId(@Param("templateUserId") Long templateUserId,
			@Param("templateCode") String templateCode, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId, @Param("botId") String botId);

	long findByTemplateUserId(Long templateUserId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and inserttime BETWEEN :from AND :to and template_user_id =:userId and status=:status", nativeQuery = true)
	List<Template> findAllByUserIdAndStatusWithDateRange(@Param("from") String from, @Param("to") String to,
			Pageable pageable, @Param("userId") Long userId, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id =:userId and status=:status", nativeQuery = true)
	List<Template> findAllByUserIdAndStatus(Pageable pageable, @Param("userId") Long userId,
			@Param("status") String status, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and inserttime BETWEEN :from AND :to and template_user_id =:userId and template_code=:templateCode", nativeQuery = true)
	List<Template> findAllByUserIdAndTemplateCodeWithDateRange(@Param("from") String from, @Param("to") String to,
			Pageable pageable, @Param("userId") Long userId, @Param("templateCode") String templateCode,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id =:userId and template_code=:templateCode", nativeQuery = true)
	List<Template> findAllByUserIdAndTemplateCode(Pageable pageable, @Param("userId") Long userId,
			@Param("templateCode") String templateCode, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and inserttime BETWEEN :from AND :to and template_user_id =:userId and template_code=:templateCode and status=:status", nativeQuery = true)
	List<Template> findAllByUserIdAndTemplateCodeAndStatusWithDateRange(@Param("from") String from,
			@Param("to") String to, Pageable pageable, @Param("userId") Long userId,
			@Param("templateCode") String templateCode, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id =:userId and template_code=:templateCode and status=:status", nativeQuery = true)
	List<Template> findAllByUserIdAndTemplateCodeAndStatus(Pageable pageable, @Param("userId") Long userId,
			@Param("templateCode") String templateCode, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and inserttime BETWEEN :from AND :to and template_user_id =:userId and status=:status", nativeQuery = true)
	List<Template> findAllByUserIdAndStatusWithoutPageAbleAndWithDateRange(@Param("from") String from,
			@Param("to") String to, @Param("userId") Long userId, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id =:userId and status=:status", nativeQuery = true)
	List<Template> findAllByUserIdAndStatusWithoutPageAble(@Param("userId") Long userId, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where inserttime BETWEEN :from AND :to and template_user_id=:templateUserId", nativeQuery = true)
	List<Template> findListByTemplateUserIdWithOutRcsMsgTypeId(@Param("from") String from, @Param("to") String to,
			@Param("templateUserId") Long templateUserId);

	@Transactional
	@Query(value = "select * from template where  template_user_id=:templateUserId", nativeQuery = true)
	List<Template> findListByTemplateByUserId(@Param("templateUserId") Long templateUserId);

	@Transactional
	@Query(value = "select * from template where template_user_id=:templateUserId", nativeQuery = true)
	List<Template> findListByTemplateUserIdWithDateRange(@Param("templateUserId") Long templateUserId);

	
	@Transactional
	@Query(value = "select * from template where status=0 and (approve_response is null or approve_response!='rejected')", nativeQuery = true)
	List<Template> findPendingTemplateForApprovelFromOperator();

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update template set status=1 ,operator_response=:operatorResponse,approve_response=:templateApproveResponse where id=:templateId", nativeQuery = true)
	Integer updateStatusByTemplateId(@Param("operatorResponse") String operatorResponse,
			@Param("templateApproveResponse") String templateApproveResponse, @Param("templateId") Long templateId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update template set operator_response=1 where id=:templateId", nativeQuery = true)
	Integer updateTemplatePushedResponseByTemplateId(@Param("templateId") Long templateId);

	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update template set status=0,operator_response=:operatorResponse ,approve_response=:templateApproveResponse where id=:templateId", nativeQuery = true)
	Integer rejectStatusByTemplateId(@Param("operatorResponse") String operatorResponse,
			@Param("templateApproveResponse") String templateApproveResponse, @Param("templateId") Long templateId);

	/**
	 * 2024-06-24 Reseller
	 */
	@Transactional
	@Query(value = "select * from template where bot_id=:botId and  rcs_msg_type_id=:rcsMsgTypeId and template_user_id =:userId and template_code=:templateCode and status=:status", nativeQuery = true)
	List<Template> findAllByUserIdAndTemplateCodeAndStatusAndBotId(Pageable pageable, @Param("userId") Long userId,
			@Param("templateCode") String templateCode, @Param("status") String status,
			@Param("rcsMsgTypeId") String rcsMsgTypeId, @Param("botId") String botId);

	@Query(value = "select count(*) from template where rcs_msg_type_id=:rcsMsgTypeId and bot_id=:botId and template_user_id =:templateUserId", nativeQuery = true)
	long countByTemplateUserIdAndBotId(@Param("templateUserId") Long templateUserId, @Param("botId") String botId,
			@Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select * from template where rcs_msg_type_id=:rcsMsgTypeId and template_user_id =:userId and bot_id=:botId", nativeQuery = true)
	List<Template> findAllByUserIdAndBotId(Pageable pageable, @Param("userId") Long userId,
			@Param("botId") String botId, @Param("rcsMsgTypeId") String rcsMsgTypeId);

	@Transactional
	@Query(value = "select id from template where user_bot_mapping_id =:userBotMappingId", nativeQuery = true)
	List<Long> findAllByTemplateId(@Param("userBotMappingId") String userBotMappingId);
	
	@Modifying(flushAutomatically = true)
	@Transactional
	@Query(value = "update template set user_bot_mapping_id =:userBotMappingId where bot_id =:botId and template_user_id =:userId", nativeQuery = true)
	Integer updateBotTokenId(@Param("userBotMappingId") Long userBotMappingId,@Param("botId") String botId,@Param("userId") Long userId);

}
