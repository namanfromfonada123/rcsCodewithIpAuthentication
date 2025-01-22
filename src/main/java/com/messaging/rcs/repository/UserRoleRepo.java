package com.messaging.rcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.UsersRole;

@Repository
@Transactional
public interface UserRoleRepo extends JpaRepository<UsersRole, Integer> {

}
