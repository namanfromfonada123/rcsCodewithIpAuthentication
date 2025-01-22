package com.messaging.rcs.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserRolesRepository extends JpaRepository<UsersRole, Long> {

	Optional<UsersRole> findById(Integer valueOf);

	@Query(value = "select * from users_roles where name=:role", nativeQuery = true)
	UsersRole findByName(@Param("role") String role);

}
