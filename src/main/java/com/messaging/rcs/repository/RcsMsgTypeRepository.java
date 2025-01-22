package com.messaging.rcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.RcsMsgTypeEntity;

@Repository
public interface RcsMsgTypeRepository extends JpaRepository<RcsMsgTypeEntity, Long> {

}
