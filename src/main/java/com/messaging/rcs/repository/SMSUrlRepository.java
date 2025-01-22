package com.messaging.rcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.SMSUrlEntity;


@Repository
public interface SMSUrlRepository extends JpaRepository<SMSUrlEntity, Long> {
	@Query(value = "select * from sms_url where tracking_id=:trackingId", nativeQuery = true)
	SMSUrlEntity findByTrackingId(@Param("trackingId") String trackingId);

	Integer deleteByTrackingId(String trackingId);

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "select * from sms_url where created_date>= :start_date and created_date <= :end_date", nativeQuery = true)
	public List<SMSUrlEntity> findAllByStartToEndDateWithPagenation(@Param("start_date") String start_date,
			@Param("end_date") String end_date);// , PageRequest page);
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "select * from sms_url where client_id= :client_id order by sms_url_id desc", nativeQuery = true)
	public List<SMSUrlEntity> findAllByClientId(@Param("client_id") Integer client_id);
}
