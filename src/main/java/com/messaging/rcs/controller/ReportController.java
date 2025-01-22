package com.messaging.rcs.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.model.CommonReportPojo;
import com.messaging.rcs.model.DataContainer;
import com.messaging.rcs.model.RcsSummarySmsData;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.service.ReportService;
import com.messaging.rcs.summary.RcsSummaryEntity;
import com.messaging.rcs.summary.RcsSummaryRepository;
import com.messaging.rcs.summary.RcsSummaryService;
import com.messaging.rcs.util.FileReadThread;

@RestController
@CrossOrigin(origins = { "*" }, maxAge = 4800L)
@RequestMapping(value = { "/api/v1/rcsmessaging/report" }, produces = { "application/json" })
public class ReportController {
	private static final Logger LOGGER = Logger.getLogger(FileReadThread.class);

	@Autowired
	private LeadInfoDetailRepository leadInfoDetailRepository;

	@Autowired
	private ReportService reportService;

	@Autowired
	private RcsSummaryRepository rcsSummaryRepository;

	@Autowired
	private RcsSummaryService rcsSummaryService;

	@SuppressWarnings("unchecked")
	@PostMapping({ "/getRcsSummarySmsReport" })
	public DataContainer getRcsSummarySmsData(@RequestBody CommonReportPojo commonReportPojo) {
		DataContainer data = new DataContainer();
		List<RcsSummarySmsData> summaryDetails = null;
		LOGGER.info("get_Summary_Report_and_Detailed_Report:: User=" + commonReportPojo.getClientId() + "||Role="
				+ commonReportPojo.getAction());
		try {
			Map<String, Object> ResData = this.reportService.getRcsSummarySmsData(commonReportPojo);
			if (ResData != null) {
				summaryDetails = (List<RcsSummarySmsData>) ResData.get("#result-set-1");
				if (summaryDetails != null && summaryDetails.size() > 0) {
					data.setData(summaryDetails);
					data.setMsg("Success");
					data.setStatus(Integer.valueOf(200));
					data.setRequest_status(String.valueOf(ResData.get("count")));
					LOGGER.info("DataSize=" + summaryDetails.size());
					// return data;
				} else {
					data.setData(summaryDetails);
					data.setMsg("Data Not Found");
					data.setStatus(Integer.valueOf(404));
					LOGGER.info("Error Response  =" + data.toString());
					// return data;
				}
			}
		} catch (Exception e) {
			data.setMsg("Got Exception " + e.getLocalizedMessage());
			data.setStatus(500);
			LOGGER.info("Error Response  =" + data.toString());
			return data;
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	@PostMapping({ "/getRcsDetailedSmsReport" })
	public DataContainer getRcsDetailedSmsData(@RequestBody CommonReportPojo commonReportPojo) {
		DataContainer data = new DataContainer();
		List<LeadInfoDetailEntity> summaryDetails = null;
		LOGGER.info("get_Summary_Report_and_Detailed_Report:: User=" + commonReportPojo.getClientId() + "||Role="
				+ commonReportPojo.getAction());
		try {
			Map<String, Object> ResData = this.reportService.getRcsDetailedSmsData(commonReportPojo);
			if (ResData != null) {
				summaryDetails = (List<LeadInfoDetailEntity>) ResData.get("#result-set-1");
				if (summaryDetails != null && summaryDetails.size() > 0) {
					data.setData(summaryDetails);
					data.setMsg("Success");
					data.setStatus(Integer.valueOf(200));
					data.setRequest_status(String.valueOf(ResData.get("count")));
					LOGGER.info("DataSize=" + summaryDetails.size());
					// return data;
				} else {
					data.setData(summaryDetails);
					data.setMsg("Data Not Found");
					data.setStatus(Integer.valueOf(404));
					LOGGER.info("Error Response=" + data.toString());
					// return data;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			data.setMsg("Got Exception " + e.getLocalizedMessage());
			data.setStatus(500);
			LOGGER.info("Error Response  =" + data.toString());
			return data;
		}
		return data;
	}

	@PostMapping({ "/findSummaryReport" })
	public DataContainer getRcsSummaryFromRcsSummaryTransactionTable(@RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate, @RequestParam("userName") String userName) {
		DataContainer data = new DataContainer();
		LOGGER.info("get_Summary_Report_and_Detailed_Report:: StartDate=" + startDate + "||EndDate=" + endDate
				+ " User Name ::" + userName);
		try {
			List<Map<String, Object>> rcsSummary = new ArrayList<>();

			List<RcsSummaryEntity> summary = this.rcsSummaryRepository.getSummaryByBetweenDate(startDate, endDate,
					userName);
			if (summary.size() > 0) {
				Map<String, Object> rcsMapSummary = null;
				for (RcsSummaryEntity rcsSum : summary) {
					rcsMapSummary = new HashMap<String, Object>();
					// rcsMapSummary.put("datehour", rcsSum.getd);
					rcsMapSummary.put("created_date", rcsSum.getCreated_date());
					rcsMapSummary.put("lead_name", rcsSum.getLead_name());
					rcsMapSummary.put("last_modified_date", rcsSum.getLast_modified_date());
					rcsMapSummary.put("status", rcsSum.getStatus());
					rcsMapSummary.put("TOTAL", rcsSum.getTOTAL());
					rcsMapSummary.put("campaing_name", rcsSum.getCampaing_name());
					rcsMapSummary.put("NonRCS_FAILED",
							rcsSum.getNonRCS_FAILED() != null ? rcsSum.getNonRCS_FAILED() : "0");
					rcsMapSummary.put("SUBMITTED", rcsSum.getSubmitted() != null ? rcsSum.getSubmitted() : "0");
					rcsMapSummary.put("sent", rcsSum.getSent() != null ? rcsSum.getSent() : "0");
					rcsMapSummary.put("Delivered", rcsSum.getDelivered() != null ? rcsSum.getDelivered() : "0");
					rcsMapSummary.put("Read", rcsSum.getRead() != null ? rcsSum.getRead() : "0");
					rcsMapSummary.put("failed", rcsSum.getFailed() != null ? rcsSum.getFailed() : "0");
					rcsMapSummary.put("Invalid", rcsSum.getInvalid() != null ? rcsSum.getInvalid() : "0");
					rcsMapSummary.put("rcs_msg_type_id",
							rcsSum.getInvalid() != null ? rcsSum.getRcs_msg_type_id() : "0");
					rcsMapSummary.put("nonrcs_fallback_sms_submit",
							rcsSum.getInvalid() != null ? rcsSum.getNonrcs_fallback_sms_submit() : "0");
					rcsMapSummary.put("nonrcs_sms_submit",
							rcsSum.getInvalid() != null ? rcsSum.getNonrcs_sms_submit() : "0");
					rcsMapSummary.put("DELIVERY_FAILED",
							rcsSum.getDelliveryFailed() != null ? rcsSum.getDelliveryFailed() : "0");
					rcsMapSummary.put("DELIVERY_SUCCESS",
							rcsSum.getDeliverySuccess() != null ? rcsSum.getDeliverySuccess() : "0");
					rcsMapSummary.put("SUBMIT_FAILED",
							rcsSum.getSubmitFailed() != null ? rcsSum.getSubmitFailed() : "0");
					rcsSummary.add(rcsMapSummary);
				}
				data.setData(rcsSummary);
				data.setMsg("Success");
				data.setStatus(Integer.valueOf(200));
				data.setRequest_status(String.valueOf(rcsSummary.size()));
				LOGGER.info("DataSize=" + rcsSummary.size());
				// return data;
			} else {
				data.setData(rcsSummary);
				data.setMsg("Data Not Found");
				data.setStatus(Integer.valueOf(404));
				LOGGER.info("findSummaryReport Error Response  =" + data.toString());
				// return data;
			}

		} catch (Exception e) {
			e.printStackTrace();
			data.setMsg("findSummaryReport Got Exception " + e.getLocalizedMessage());
			data.setStatus(500);
			LOGGER.info("findSummaryReport Error Response  =" + data.toString());
			return data;
		}
		return data;
	}

	@PostMapping({ "/insertRcsSummaryReportByLeadId" })
	public DataContainer insertRcsSummaryReportByLeadId(@RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate) {
		DataContainer data = new DataContainer();
		LOGGER.info("updateRcsSummaryReportByLeadId::");
		try {
			List<RcsSummaryEntity> rcsSummary = this.rcsSummaryService.insertSummaryByApi(startDate, endDate);
			if (rcsSummary.size() > 0) {
				data.setData(rcsSummary);
				data.setMsg("Success");
				data.setStatus(Integer.valueOf(200));
				data.setRequest_status(String.valueOf(rcsSummary.size()));
				LOGGER.info("DataSize=" + rcsSummary.size());
				// return data;
			} else {
				data.setData(rcsSummary);
				data.setMsg("Data Not Inserted.");
				data.setStatus(Integer.valueOf(404));
				LOGGER.info("insertRcsSummaryReportByLeadId Error Response  =" + data.toString());
				// return data;
			}

		} catch (Exception e) {
			e.printStackTrace();
			data.setMsg("insertRcsSummaryReportByLeadId Got Exception " + e.getLocalizedMessage());
			data.setStatus(500);
			LOGGER.info("insertRcsSummaryReportByLeadId Error Response  =" + data.toString());
			return data;
		}
		return data;
	}

	@PostMapping({ "/updateSummaryByLeadId" })
	public DataContainer updateSummaryByLeadId(@RequestParam("leadId") String leadId) {
		DataContainer data = new DataContainer();
		RcsSummaryEntity rcsSummary = null;
		LOGGER.info("updateRcsSummaryReportByLeadId::" + leadId.toString());
		try {
			rcsSummary = this.rcsSummaryService.updateSummaryByLeadId(leadId);
			if (Objects.nonNull(rcsSummary)) {
				data.setData(rcsSummary);
				data.setMsg("Success");
				data.setStatus(Integer.valueOf(200));
				LOGGER.info("updateSummaryByLeadId=" + data.toString());
				// return data;
			} else {
				data.setData(rcsSummary);
				data.setMsg("Summary Not Updated.");
				data.setStatus(Integer.valueOf(404));
				LOGGER.info("updateSummaryByLeadId Error Response  =" + data.toString());
				// return data;
			}

		} catch (Exception e) {
			data.setMsg("updateSummaryByLeadId Got Exception " + e.getLocalizedMessage());
			data.setStatus(500);
			LOGGER.info("updateSummaryByLeadId Error Response  =" + data.toString());
			e.printStackTrace();
			return data;
		}
		return data;
	}
}
