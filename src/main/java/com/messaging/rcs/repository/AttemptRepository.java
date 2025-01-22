package com.messaging.rcs.repository;

import com.messaging.rcs.domain.AttemptInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by sbsingh on Oct/31/2021.
 */
@Repository
public interface AttemptRepository extends JpaRepository<AttemptInfoEntity, Long> {
    @Query(value ="select status from attempt_info where retry_id = ?1 and round = ?2 limit 1", nativeQuery = true)
    String getRoundStatus(Long retry_id, int attemp_num);

    @Query(" select ai from AttemptInfoEntity ai where ai.round = 1 and ai.retryId = ?1 ")
    AttemptInfoEntity getRoundOneEntity(Long retryId);

    @Query(" select ai.processedNumCount from AttemptInfoEntity ai where ai.round = ?2 and ai.retryId = ?1 ")
    Integer getRoundProcessCount(Long retryId, int attemptNum);

    @Query(" select min(ai.round) from AttemptInfoEntity ai where  ai.retryId = ?1 and ai.status = 'Pending' ")
    Integer getCurrentAttemptNum(Long retryId);

    @Query(" select ai from AttemptInfoEntity ai where ai.retryId = ?1 order by ai.attempId asc ")
    List<AttemptInfoEntity> findByRetryId(Long retryId);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query(value = "update attempt_info ai set ai.PROCESSED_NUM_COUNT = ai.PROCESSED_NUM_COUNT + 1 where ai.retry_id = ?1 and ai.ROUND = ?2 limit 1", nativeQuery = true)
    void updateProcessedNumCount(Long retryId, int attemptNum);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query(value = "update attempt_info ai set ai.QUALIFIED_COUNT = ai.QUALIFIED_COUNT +  1 where ai.retry_id = ?1 and ai.round = ?2 and ai.round <= ?2  limit 1", nativeQuery = true)
    void updateQualifiedCount(long retryId, int nextAttemptNum, Integer nextNoOfRetry, Integer retryAttemptCount);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query(value = "update attempt_info ai set ai.STATUS = 'Completed' where ai.retry_id = ?1 and ai.QUALIFIED_COUNT = ai.PROCESSED_NUM_COUNT and ai.round = ?2 limit 1", nativeQuery = true)
    void markRoundCompletedIfApplicable(Long retryId, int attemptNum);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query(value = "update attempt_info ai set ai.STATUS = 'Completed' where ai.retry_id = ?1  and ai.round = ?2 limit 1", nativeQuery = true)
    void markRoundCompleted(Long retryId, int attemptNum);

    @Query(value = "select ai.STATUS from attempt_info ai where ai.round = ?1 and ai.retry_id = ?2  limit 1",nativeQuery = true)
    String getPreviousRoundStatus(int attemptNum, Long retryId);

    @Query(value = "select count(ai.ATTEMPT_ID) from attempt_info ai where ai.retry_id = ?1 and ai.STATUS in ('Pending') and ai.QUALIFIED_COUNT > 0", nativeQuery = true)
    Integer getPendingRoundCount(Long retryId);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query(value = "update attempt_info ai set ai.STATUS = 'Not Required' where ai.retry_id = ?1 and ai.round > ?2 ",nativeQuery = true)
    void markAttemptsAsNotRequired(Long retryId, int attemptNum);

    @Query(value = "select count(ai.ATTEMPT_ID) from attempt_info ai where ai.retry_id = ?1 and ai.STATUS in ('Completed', 'Not Required')", nativeQuery = true)
    Integer getCompletedRetryCount(Long retryId);

    @Query(value = "select ai.* from attempt_info ai where ai.ROUND = ?1 and ai.retry_id = ?2", nativeQuery = true)
    AttemptInfoEntity findbyRoundAndRetryId(int attemptNum, Long retryId);
}
