package com.messaging.rcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.UserBotMappingEntity;

@Repository
@Transactional
public interface UserBotMappingRepository extends JpaRepository<UserBotMappingEntity, Integer> {

	@Query(value = "select * from user_bot_mapping where user_id in (:userId)", nativeQuery = true)
	public List<UserBotMappingEntity> findBotsListByUserId(@Param("userId") List<Long> userId);

	@Query(value = "select * from user_bot_mapping where bot_id=:botId", nativeQuery = true)
	public UserBotMappingEntity findBotsListByUserIdAndBotId(@Param("botId") String botId);
	
	
	@Query(value = "select * from user_bot_mapping where id =:id", nativeQuery = true)
	public UserBotMappingEntity findById(@Param("id") Long id);
	
	@Query(value = "select * from user_bot_mapping where bot_id =:botId", nativeQuery = true)
	public UserBotMappingEntity findByTemplateBotId(@Param("botId") String botId);

}
