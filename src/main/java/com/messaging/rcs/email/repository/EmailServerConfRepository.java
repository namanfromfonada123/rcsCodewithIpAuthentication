package com.messaging.rcs.email.repository;

import com.messaging.rcs.email.model.EmailServerConf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailServerConfRepository extends JpaRepository<EmailServerConf, Long> {
	
	@Query("select c from EmailServerConf c where c.name = ?1")
	EmailServerConf findByEventKey(String name);
}
