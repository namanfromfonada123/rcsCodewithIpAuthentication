package com.messaging.rcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.LeadInfoDetailEntityBackUp;

/**
 * 2023-06-01
 * 
 * @author RahulRajput
 *
 */
@Repository
public interface LeadInfoDetailBackUpRepository extends JpaRepository<LeadInfoDetailEntityBackUp, Long> {
	@Query(value = "select  date(lid.created_date)created_date,info.lead_name,date(lid.last_modified_date)last_modified_date,info.lead_status,COUNT(*)TOTAL,(select cmp.campaign_name from rcsmessaging.campaign cmp where cmp.campaign_id=info.lead_campaign_id limit 1)campaing_name,\r\n"
			+ "SUM(CASE WHEN lid.phone_number_status='NONRCS_NUMBER'  THEN 1 ELSE 0 END) NonRCS_FAILED,\r\n"
			+ "SUM(CASE WHEN lid.phone_number_status='OK'  THEN 1 ELSE 0 END) SUBMITTED ,\r\n"
			+ "SUM(CASE WHEN lid.status='Delivered' or lid.status='sent' or lid.status like 'Displayed%'  or lid.status='read' THEN 1 ELSE 0 END) sent, \r\n"
			+ "SUM(CASE WHEN lid.status='Delivered'  or lid.status like 'Displayed%'  or lid.status='read' THEN 1 ELSE 0 END) Delivered, \r\n"
			+ "SUM(CASE WHEN lid.status like 'Displayed%' or lid.status='read' THEN 1 ELSE 0 END) `Read`, \r\n"
			+ "SUM(CASE WHEN lid.status='failed'  THEN 1 ELSE 0 END) failed, \r\n"
			+ "SUM(CASE WHEN lid.phone_number_status='INVALID_NUMBER'  THEN 1 ELSE 0 END) Invalid, info.created_by, info.lead_id\r\n"
			+ " from  rcsmessaging.lead_info_detail_feb  lid INNER JOIN  rcsmessaging.lead_info info ON info.lead_id = lid.lead_id \r\n"
			+ "where lid.lead_id=:leadId  GROUP BY lid.lead_id; ", nativeQuery = true)
	public List<Object[]> getRcsSummary(@Param("leadId") Long leadId);

}
