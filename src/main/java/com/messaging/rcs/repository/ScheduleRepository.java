package com.messaging.rcs.repository;

import com.messaging.rcs.domain.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Created by sbsingh on Oct/31/2021.
 */
@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    ScheduleEntity getByScheduleId(Long scheduleId);
    @Modifying
    @Transactional
    @Query("update ScheduleEntity s set s.scheduleDay = ?2 where s.scheduleId = ?1")
    void updateScheduleDay(long scheduleId, String days);
}
