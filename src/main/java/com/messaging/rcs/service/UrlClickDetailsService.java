package com.messaging.rcs.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UrlClickRepository;
import com.messaging.rcs.domain.urlClickDetails;
import com.messaging.rcs.model.CustomPagenationReportBean;
import com.messaging.rcs.model.DataContainer;

@Service
public class UrlClickDetailsService {

	@Autowired
	private UrlClickRepository urlClickRepository;

	public DataContainer getReportDetailbyStartAndEndDate(CustomPagenationReportBean customPagenationReportBean,
			org.springframework.data.domain.Pageable pageable) throws ParseException {
		DataContainer data = new DataContainer();
		List<urlClickDetails> sMSUrlEntity = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date todayDate = new Date();
		String todayDateString = sdf.format(todayDate);
		todayDate = sdf.parse(todayDateString);
		Integer totalCount = 0;
	
		sMSUrlEntity = urlClickRepository.findAllByStartToEndDateWithPagenation(
				customPagenationReportBean.getStartDate(), customPagenationReportBean.getEndDate(),
				customPagenationReportBean.getClientId(), pageable);
		// PageRequest.of(customCdrReportBean.getPage() - 1,
		// customCdrReportBean.getSize()));
		if (sMSUrlEntity.size() > 0) {
			totalCount = urlClickRepository.getCountAllByStartToEndDate(customPagenationReportBean.getStartDate(),
					customPagenationReportBean.getEndDate(), customPagenationReportBean.getClientId());

			data.setStatus(200);
			data.setMsg("Data Found Successfully.");
			data.setData(sMSUrlEntity);
			data.setTotal(totalCount);
		} else {
			data.setStatus(400);
			data.setMsg("Record Does Not Exist");
		}

		return data;
	}

	@Cacheable(cacheNames = "smsTrackingCache", key = "#sms_longUrl")
	public urlClickDetails getByLongUrl(String longUrl) {
		return urlClickRepository.getByLongUrl(longUrl);

	}

}
