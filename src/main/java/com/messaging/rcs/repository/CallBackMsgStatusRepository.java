package com.messaging.rcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.CallBackMsgStatusEntity;

@Repository
public interface CallBackMsgStatusRepository extends JpaRepository<CallBackMsgStatusEntity, Long> {
	@org.springframework.transaction.annotation.Transactional
	@Query(value = "select * from demo_rcs.call_back_msg_status where user_name=:userName and (text_message is not null or display_text is not null) and created_date between :startDate and :endDate", nativeQuery = true)
	public List<CallBackMsgStatusEntity> findByTextMessageNotNullAndIsCompleteIsOne(@Param("userName") String userName,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

}
