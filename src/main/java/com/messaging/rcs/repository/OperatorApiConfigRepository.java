package com.messaging.rcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.OperatorApiConfigEntity;

@Repository
public interface OperatorApiConfigRepository extends JpaRepository<OperatorApiConfigEntity, Long> {

	@Query("SELECT o FROM OperatorApiConfigEntity o WHERE o.apiType = :apiType AND o.operatorId = :operatorId")
	OperatorApiConfigEntity findByApiTypeAndTemplateTypeAndOperatorId(@Param("apiType") String apiType,
			@Param("operatorId") Long operatorId);
}
