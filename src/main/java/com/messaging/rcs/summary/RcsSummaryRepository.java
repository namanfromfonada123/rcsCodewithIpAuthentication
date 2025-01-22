package com.messaging.rcs.summary;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RcsSummaryRepository extends JpaRepository<RcsSummaryEntity, Long> {
	@Transactional
	@Query(value = "select * from rcs_summary where created_date between :startDate and :endDate and user_name=:userName", nativeQuery = true)
	public List<RcsSummaryEntity> getSummaryByBetweenDate(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("userName") String userName);

	@Query(value = "select * from rcs_summary where lead_id=:leadId", nativeQuery = true)
	public RcsSummaryEntity findByLeadName(@Param("leadId") String leadId);

}
