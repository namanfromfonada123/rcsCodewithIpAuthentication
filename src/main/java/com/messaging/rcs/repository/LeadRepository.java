package com.messaging.rcs.repository;

import com.messaging.rcs.domain.LeadInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by sbsingh on Oct/30/2021.
 */
@Repository
public interface LeadRepository extends JpaRepository<LeadInfoEntity, Long> {
}
