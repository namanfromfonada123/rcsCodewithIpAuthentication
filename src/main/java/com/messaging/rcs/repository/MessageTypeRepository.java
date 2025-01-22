package com.messaging.rcs.repository;

import com.messaging.rcs.domain.MessageTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by sbsingh on Nov/15/2021.
 */
@Repository
public interface MessageTypeRepository extends JpaRepository<MessageTypeEntity, Long> {

    MessageTypeEntity findByMessageId(Long messageId);
}
