package com.messaging.rcs.repository;

import com.messaging.rcs.domain.MessageSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by sbsingh on Oct/31/2021.
 */
@Repository
public interface MessageSummaryRepository extends JpaRepository<MessageSummaryEntity, Long> {
}
