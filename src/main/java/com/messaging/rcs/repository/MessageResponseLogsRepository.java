package com.messaging.rcs.repository;

import com.messaging.rcs.domain.MessageResponseLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by sbsingh on Nov/21/2021.
 */
@Repository
public interface MessageResponseLogsRepository extends JpaRepository<MessageResponseLogs, Long> {
}
