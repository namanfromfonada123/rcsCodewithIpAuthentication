package com.messaging.rcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.LeadFileStorage;

@Repository
@Transactional
public interface LeadFileStorageRepository extends JpaRepository<LeadFileStorage, Long> {

	List<LeadFileStorage> findByIsSchedule(Integer isSchedule);

	@Modifying
	@Query(value = "update lead_file_storage set is_schedule=1 where lead_file_id =:id", nativeQuery = true)
	int updateById(@Param("id")Long id);
}
