package com.messaging.rcs.summary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.messaging.rcs.repository.LeadInfoDetailBackUpRepository;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.repository.LeadInfoRepository;
import com.messaging.rcs.util.DateUtils;

@Service
public class RcsSummaryService {

	@Autowired
	private LeadInfoDetailRepository leadInfoDetailRepository;
	@Autowired
	private RcsSummaryRepository rcsSummaryRepository;

	@Autowired
	private LeadInfoRepository leadInfoRepository;

	@Autowired
	private LeadInfoDetailBackUpRepository leadInfoDetailBackUpRepository;

	// @Scheduled(cron = "0 13 10 * * *")
	// @Scheduled(cron = "${lead.callback.schedular.cron}")
//	@Scheduled(fixedDelay = 50000)
	public void getSummary() {
		try {
			List<RcsSummaryEntity> rcsSummaryEntities = null;
			System.out.println("Inside GetSummary");
			List<Long> leadId = leadInfoRepository.getAllLeadByCreatedDate(
					DateUtils.getYesterdayDateString() + " 00:00:00", DateUtils.getYesterdayDateString() + " 23:59:59");
			System.out.println("Lead Got Size ::" + leadId.size());
			if (leadId.size() > 0) {
				rcsSummaryEntities = insertSummaryByLeadId(leadId);
				if (rcsSummaryEntities.size() > 0) {
					System.out.println("Today Total Lead Inserted In Rcs_summary Table:: " + rcsSummaryEntities.size());
				}

			} else {
				System.out.println("Today No Lead Found For Inserting In Rcs_summary Table:: " + leadId.size());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<RcsSummaryEntity> insertSummaryByLeadId(List<Long> leadId) {
		System.out.println("***** Inside updateSummaryByLeadId *****");
		RcsSummaryEntity rcsSummaryEntity = null;
		List<RcsSummaryEntity> rcsSummaryEntities = new ArrayList<>();

		for (Long lead : leadId) {
			List<Object[]> summary = leadInfoDetailRepository.getRcsSummary(lead);
			if (summary.size() > 0) {
				for (Object[] sum : summary) {
					System.out.println("Ready To Processing For Lead_Id::" + sum[1]);

					rcsSummaryEntity = new RcsSummaryEntity();

					rcsSummaryEntity.setCreated_date(String.valueOf(sum[0]));
					rcsSummaryEntity.setLead_name(String.valueOf(sum[1]));
					rcsSummaryEntity.setLast_modified_date(String.valueOf(sum[2]));
					rcsSummaryEntity.setStatus(String.valueOf(sum[3]));
					rcsSummaryEntity.setTOTAL(String.valueOf(sum[4]));
					rcsSummaryEntity.setCampaing_name(String.valueOf(sum[5]));
					rcsSummaryEntity.setNonRCS_FAILED(String.valueOf(sum[6]));
					rcsSummaryEntity.setSubmitted(String.valueOf(sum[7]));
					rcsSummaryEntity.setSent(String.valueOf(sum[8]));
					rcsSummaryEntity.setDelivered(String.valueOf(sum[9]));
					rcsSummaryEntity.setRead(String.valueOf(sum[10]));
					rcsSummaryEntity.setFailed(String.valueOf(sum[11]));
					rcsSummaryEntity.setInvalid(String.valueOf(sum[12]));
					rcsSummaryEntity.setUser_name(String.valueOf(sum[13]));
					rcsSummaryEntity.setLeadId(String.valueOf(sum[14]));
					rcsSummaryEntity.setRcs_msg_type_id(String.valueOf(sum[15]));
					rcsSummaryEntity.setNonrcs_fallback_sms_submit(String.valueOf(sum[16]));
					rcsSummaryEntity.setNonrcs_sms_submit(String.valueOf(sum[17]));
					rcsSummaryEntity.setDelliveryFailed(String.valueOf(sum[18]));
					rcsSummaryEntity.setSubmitFailed(String.valueOf(sum[19]));
					rcsSummaryEntity.setDeliverySuccess(String.valueOf(sum[20]));

					rcsSummaryEntities.add(rcsSummaryEntity);

					System.out.println("Processing Done For This Lead_Id::" + sum[1]);

				}
				rcsSummaryRepository.saveAll(rcsSummaryEntities);
			}
		}
		return rcsSummaryEntities;
	}

	public List<RcsSummaryEntity> insertSummaryByApi(String startDate, String endDate) {
		List<RcsSummaryEntity> rcsSummaryEntities = null;
		try {

			System.out.println("Inside GetSummary");
			List<Long> leadId = leadInfoRepository.getAllLeadByCreatedDate(startDate + " 00:00:00",
					endDate + " 23:59:59");
			System.out.println("Lead Got Size ::" + leadId.size());
			if (leadId.size() > 0) {
				rcsSummaryEntities = insertSummaryByLeadId(leadId);
				if (rcsSummaryEntities.size() > 0) {
					System.out.println("Today Total Lead Inserted In Rcs_summary Table:: " + rcsSummaryEntities.size());
				}

			} else {
				System.out.println("Today No Lead Found For Inserting In Rcs_summary Table:: " + leadId.size());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rcsSummaryEntities;
	}

	public RcsSummaryEntity updateSummaryByLeadId(String leadId) {

		System.out.println("Update Process Done For This Lead_Id::" + leadId);

		RcsSummaryEntity rcsSummaryEntity = null;
		List<Object[]> summary = null;
		rcsSummaryEntity = rcsSummaryRepository.findByLeadName(leadId);

		if (Objects.nonNull(rcsSummaryEntity)) {
			summary = leadInfoDetailRepository.getRcsSummary(Long.valueOf(leadId));
			if (summary.size() > 0) {
				for (Object[] sum : summary) {
					System.out.println("Ready To Processing For Lead_Id::" + sum[1]);

					rcsSummaryEntity.setCreated_date(String.valueOf(sum[0]));
					rcsSummaryEntity.setLead_name(String.valueOf(sum[1]));
					rcsSummaryEntity.setLast_modified_date(String.valueOf(sum[2]));
					rcsSummaryEntity.setStatus(String.valueOf(sum[3]));
					rcsSummaryEntity.setTOTAL(String.valueOf(sum[4]));
					rcsSummaryEntity.setCampaing_name(String.valueOf(sum[5]));
					rcsSummaryEntity.setNonRCS_FAILED(String.valueOf(sum[6]));
					rcsSummaryEntity.setSubmitted(String.valueOf(sum[7]));
					rcsSummaryEntity.setSent(String.valueOf(sum[8]));
					rcsSummaryEntity.setDelivered(String.valueOf(sum[9]));
					rcsSummaryEntity.setRead(String.valueOf(sum[10]));
					rcsSummaryEntity.setFailed(String.valueOf(sum[11]));
					rcsSummaryEntity.setInvalid(String.valueOf(sum[12]));
					rcsSummaryEntity.setUser_name(String.valueOf(sum[13]));
					rcsSummaryEntity.setLeadId(String.valueOf(sum[14]));
					rcsSummaryEntity.setRcs_msg_type_id(String.valueOf(sum[15]));
					rcsSummaryEntity = rcsSummaryRepository.save(rcsSummaryEntity);

					System.out.println("Update Process Done For This Lead_Id::" + leadId);

				}
			}
		}
		return rcsSummaryEntity;
	}
}
