package com.messaging.rcs.email.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.TemplateFileDB;

@Repository
@Transactional
public interface TemplateFileDbRepository extends JpaRepository<TemplateFileDB, Long> {

	@Modifying
	@Transactional
	@Query(value = "update template_files set template_id=:templateId where id=:Id", nativeQuery = true)
	public Integer updateFileStorageTempalteId(@Param("templateId") Long templateId, @Param("Id") Long Id);

}
