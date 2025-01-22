package com.messaging.rcs.email.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.rcs.domain.BlackListUploadFile;

@Repository
@Transactional
public interface BlackListUploadFileRepository extends JpaRepository<BlackListUploadFile, Long> {

	List<BlackListUploadFile> findByIsExecute(int i);

	@Modifying
	@Query(value = "update black_list_upload_file set is_execute=1 where id=:id", nativeQuery = true)
	void updateIsExecuteById(@Param("id") Long id);
}
