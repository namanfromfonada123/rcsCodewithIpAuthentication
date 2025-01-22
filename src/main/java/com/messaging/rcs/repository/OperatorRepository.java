package com.messaging.rcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.OperatorEntity;

@Repository
public interface OperatorRepository extends JpaRepository<OperatorEntity, Long> {

	@Query(value = "select * from operator where operator_name=:operator", nativeQuery = true)
	OperatorEntity findByOperatorName(@Param("operator")String operator);

}
