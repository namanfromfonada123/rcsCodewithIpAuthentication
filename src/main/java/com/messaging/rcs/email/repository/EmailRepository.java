package com.messaging.rcs.email.repository;


import com.messaging.rcs.email.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<EmailTemplate, Long> {
	
	@Query("select e from EmailTemplate e where e.eventKey = ?1")
	EmailTemplate findByEventKey(String eventKey);
}
