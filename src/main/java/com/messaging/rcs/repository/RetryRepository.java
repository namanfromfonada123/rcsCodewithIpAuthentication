package com.messaging.rcs.repository;

import com.messaging.rcs.domain.RetryInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by sbsingh on Oct/31/2021.
 */
@Repository
public interface RetryRepository extends JpaRepository<RetryInfoEntity, Long> {
    RetryInfoEntity getRetryInfoEntityByRetryId(Long retryId);
    RetryInfoEntity getRetryInfoEntityByRetryType(String retryType);
}
