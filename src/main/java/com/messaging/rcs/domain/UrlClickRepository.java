package com.messaging.rcs.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UrlClickRepository extends JpaRepository<urlClickDetails, Integer> {

	urlClickDetails getByLongUrl(String longUrl);

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "select * from url_click_details where created_date>= :start_date and created_date <= :end_date and clientid= :clientid order by id desc", nativeQuery = true)
	public List<urlClickDetails> findAllByStartToEndDateWithPagenation(@Param("start_date") String start_date,
			@Param("end_date") String end_date, @Param("clientid") int clientid, Pageable pageable);// , PageRequest
																									// page);

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "select click.id,click.clientid,click.created_date,click.short_url,click.long_url,click.device,click.ua,msg.biz_msg_id,msg.biz_num as senderId,msg.content,msg.dlttemplateid,msg.inserttime as pb_date from  enterprise.url_click_details as click  inner JOIN enterprise.yulore_message_request as msg   ON  click.transaction_id =msg.biz_msg_id where click.click_api_status=0 and click.clientid in (36,37) group by click.id limit 500", nativeQuery = true)
	public List<Object[]> getListByClickApiStatuIsZero();

	@Modifying
	@Transactional
	@Query(value = "update url_click_details set click_api_status=1 where id=:id", nativeQuery = true)
	public void updateClickApiStatus(@Param("id") Integer id);

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "select count(*) from url_click_details where created_date>= :start_date and created_date <= :end_date and clientid= :clientid order by id desc", nativeQuery = true)
	public Integer getCountAllByStartToEndDate(@Param("start_date") String start_date,
			@Param("end_date") String end_date, @Param("clientid") int clientid);// , PageRequest page);

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Query(value = "select * from url_click_details where created_date>= :start_date and created_date <= :end_date and clientid= :clientid order by id desc", nativeQuery = true)
	public List<urlClickDetails> findAllByStartToEndDate(@Param("start_date") String start_date,
			@Param("end_date") String end_date, @Param("clientid") int clientid);// , PageRequest
																									// page);

}
