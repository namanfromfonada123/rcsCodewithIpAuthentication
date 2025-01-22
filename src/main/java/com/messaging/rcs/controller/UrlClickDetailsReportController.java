package com.messaging.rcs.controller;

import java.text.ParseException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.messaging.rcs.domain.UrlClickRepository;
import com.messaging.rcs.domain.urlClickDetails;
import com.messaging.rcs.model.CustomPagenationReportBean;
import com.messaging.rcs.model.DataContainer;
import com.messaging.rcs.service.UrlClickDetailsService;

@RestController
@RequestMapping("/api/v1/rcsmessaging/sms/clickUrl")
public class UrlClickDetailsReportController {
	private static final Logger LOGGER = Logger
			.getLogger(com.messaging.rcs.controller.UrlClickDetailsReportController.class.getName());

	@Autowired
	private UrlClickDetailsService urlClickService;

	@Autowired
	private UrlClickRepository urlClickRepository;

	@RequestMapping(value = "/findAllClickUrlDeatils", method = RequestMethod.POST)
	public String findAllTrackingList(@RequestBody CustomPagenationReportBean customCdrReportBean)
			throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		DataContainer data = new DataContainer();

		try {

			if (customCdrReportBean.getFlag().equalsIgnoreCase("D")) {
				LOGGER.info("***** Inside Download Click Report *****");
				List<urlClickDetails> allClickDetailsList = null;
				allClickDetailsList = urlClickRepository.findAllByStartToEndDate(customCdrReportBean.getStartDate(),
						customCdrReportBean.getEndDate(), customCdrReportBean.getClientId());
				if (allClickDetailsList.size() > 0) {
					data.setStatus(200);
					data.setMsg("Data Found Successfully.");
					data.setData(allClickDetailsList);
					LOGGER.info("***** Inside Download Click Report *****" + data.getMsg());
					return gson.toJson(data).toString();
				} else {
					data.setStatus(400);
					data.setMsg("Record Does Not Exist");
					LOGGER.info("***** Inside Download Click Report *****" + data.getMsg());
					return gson.toJson(data).toString();

				}

			} else {
				Integer start = customCdrReportBean.getStart() - 1;
				int pageSize = customCdrReportBean.getSize();
				int pageNum = start != 0 ? start / pageSize : 0;
				Pageable pageable = PageRequest.of(pageNum, pageSize);
				data = urlClickService.getReportDetailbyStartAndEndDate(customCdrReportBean, pageable);
				LOGGER.info("***** Inside Download Click Report *****" + data.getMsg());

				return gson.toJson(data).toString();
			}
		} catch (Exception e) {
			data.setStatus(500);
			data.setMsg(e.getMessage());
			e.printStackTrace();
			return gson.toJson(data).toString();

		}
	}

}
