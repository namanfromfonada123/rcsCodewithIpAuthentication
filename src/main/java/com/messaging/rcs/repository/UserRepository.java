package com.messaging.rcs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.services.rcsbusinessmessaging.v1.RCSBusinessMessaging.Users;
import com.messaging.rcs.domain.UserEntity;

/**
 * 
 * @author RahulRajput 2023-06-13
 *
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByUserName(String username);

	@Query("select u.apiKey from UserEntity u where u.apiKey = ?1")
	String findbyApiKey(String apiKey);

	UserEntity findByUserId(Long userId);

	@Query(value = "select user_id from users  where role_id =:roleId", nativeQuery = true)
	List<Long> findUsersByRoleId(@Param("roleId") Long roleId);

	UserEntity findByUserNameAndEmail(String username, String email);

	@Query("select u from UserEntity u where u.userName = ?1 and u.userPassword =?2 and u.isDeleted = 0")
	UserEntity findByUserNameAndUserPassword(String userName, String userPassword);

	UserEntity getUserEntityByUserId(Long userId);

	@Query("select u from UserEntity u where u.parentUserId = ?1")
	List<UserEntity> getUserByUserParentId(Long parentId);

	@Query("select u from UserEntity u where u.parentUserId = 0")
	List<UserEntity> getAdminUser();

	@Transactional
	@Modifying
	@Query(value = "update users set sms_dlt_principle_id=:smsDltPrincipleId where user_id=:user_id", nativeQuery = true)
	Integer updateSmsDltPrincipleId(@Param("smsDltPrincipleId") String smsDltPrincipleId,
			@Param("user_id") String user_id);

	UserEntity getByUserName(String username);

	@Query(value = "select * from users  where role_id in (:roleId)", nativeQuery = true)
	List<UserEntity> findAllUsersByRoleId(@Param("roleId") Integer roleId);
	
	@Query(value = "select * from users  where user_id in (:userId)", nativeQuery = true)
	List<UserEntity> findAllUsersByUserId(@Param("userId") Long userId);

	
	@Query(value = "select user_name from users where user_id in (select parent_user_id from users  where user_id=:userId)", nativeQuery = true)
	List<String> findParentUserNameByUserId(@Param("userId") Long userId);
	
	@Transactional
	@Query(value = "select * from users  where parent_user_id=:userId", nativeQuery = true)
	List<UserEntity> findAllSubUserOfSubAdminOfAdminByParentUserId(@Param("userId") Long userId);
	
	@Query(value = "select credit_balance from users  where user_id=:userId", nativeQuery = true)
	Integer findSumRcsCredit(@Param("userId") Long userId);
	
	@Query(value = "select sms_credit_balance from users  where user_id=:userId", nativeQuery = true)
	Integer findSumSMSCredit(@Param("userId") Long userId);
	
	@Query(value = "select whats_app_credit_balance from users  where user_id=:userId", nativeQuery = true)	
	Integer findSumWhatsAppCredit(@Param("userId") Long userId);
	
	@Transactional
	@Modifying
	@Query(value = "update users set credit_balance=:creditBalance where user_id=:userId", nativeQuery = true)
	Integer updateRcsCredit(@Param("userId") Long userId,@Param("creditBalance")Long creditBalance);
	
	@Transactional
	@Modifying
	@Query(value = "update users set sms_credit_balance=:smsBalance  where user_id=:userId", nativeQuery = true)
	Integer updateSMSCredit(@Param("userId") Long userId,@Param("smsBalance")Long smsBalance);
	
	@Transactional
	@Modifying
	@Query(value = "update users set whats_app_credit_balance=:whatsAppBalance from users  where user_id=:userId", nativeQuery = true)	
	Integer updateWhatsAppCredit(@Param("userId") Long userId,@Param("whatsAppBalance")Long whatsAppBalance);
	
	@Query(value = "select * from rcsmessaging.users group by bot_id;", nativeQuery = true)	
	List<UserEntity> findAllBotToken();
}
