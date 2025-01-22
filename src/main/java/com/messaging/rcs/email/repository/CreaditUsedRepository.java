package com.messaging.rcs.email.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.CreditUsedEntity;

@Repository
@Transactional
public interface CreaditUsedRepository extends JpaRepository<CreditUsedEntity, Long> {

	@Transactional
	@Query(value = "select * from credit_used where user_id=:userId order by crd_id desc",nativeQuery = true)
	List<CreditUsedEntity> findAllByUserId(@Param("userId") String userId);
	
	
	
	@Transactional
	@Query(value = "select * from credit_used where user_id=:userId ;",nativeQuery = true)
	List<CreditUsedEntity> getAllUserId(@Param("userId") String userId);

}
