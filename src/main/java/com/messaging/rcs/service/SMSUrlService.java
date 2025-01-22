package com.messaging.rcs.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.SMSUrlEntity;
import com.messaging.rcs.model.DataContainer;
import com.messaging.rcs.model.SmsUrlDto;
import com.messaging.rcs.repository.SMSUrlRepository;

@Service
public class SMSUrlService {
	@Autowired
	RedisTemplate<String, SMSUrlEntity> redisTemplate;

	@Autowired
	private SMSUrlRepository smsUrlRepository;

	@Transactional
	public DataContainer createUrl(SmsUrlDto smsUrlDto) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date todayDate = new Date();
		String todayDateString = sdf.format(todayDate);
		todayDate = sdf.parse(todayDateString);

		DataContainer data = new DataContainer();
		SMSUrlEntity smsUrlEntity = new SMSUrlEntity();
		try {
			if (Objects.isNull(smsUrlDto.getTitle()) || smsUrlDto.getTitle().equals("")) {
				data.setStatus(404);
				data.setMsg("Title Cannot Be Empty.");
				return data;
			}
			if (Objects.isNull(smsUrlDto.getUrl()) || smsUrlDto.getUrl().equals("")) {
				data.setStatus(404);
				data.setMsg("URL Cannot Be Empty.");
				return data;
			}
			if (Objects.isNull(smsUrlDto.getClientId()) || smsUrlDto.getUrl().equals("")) {
				data.setStatus(404);
				data.setMsg("Client Id Cannot Be Empty.");
				return data;
			}
			smsUrlEntity.setUrl(smsUrlDto.getUrl());
			smsUrlEntity.setTitle(smsUrlDto.getTitle());
			smsUrlEntity.setTrackingId(generateTrackingId());
			smsUrlEntity.setCreatedDate(todayDateString);
			smsUrlEntity.setCreatedBy("Admin");
			smsUrlEntity.setIsActive("1");
			smsUrlEntity.setClientId(smsUrlDto.getClientId());
			smsUrlEntity.setMessageType(smsUrlDto.getMessageType());
			SMSUrlEntity saveSmsUrlEntity = smsUrlRepository.save(smsUrlEntity);
			if (saveSmsUrlEntity.getSmsUrlId() != null) {
				try {
					redisTemplate.opsForValue().set(saveSmsUrlEntity.getTrackingId(), saveSmsUrlEntity);
					// , 36000L,TimeUnit.SECONDS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				data.setStatus(200);
				data.setMsg("Data Saved Successfully.");
				data.setData(saveSmsUrlEntity);
				return data;
			}
		} catch (Exception e) {
			data.setMsg("Got Exceptions::" + e.getMessage());
			data.setStatus(500);
		}
		return null;

	}

	public DataContainer findByTrackingId(String trackingId) {
		DataContainer data = new DataContainer();
		SMSUrlEntity sMSUrlEntity = null;
		if (Objects.isNull(trackingId) || trackingId.equals("")) {
			data.setStatus(404);
			data.setMsg("Tracking Id Is Empty.");
			return data;
		} else {
			sMSUrlEntity = smsUrlRepository.findByTrackingId(trackingId);
			if (Objects.nonNull(sMSUrlEntity)) {
				data.setStatus(200);
				data.setMsg("Data Found Successfully.");
				data.setData(sMSUrlEntity);
			} else {
				data.setStatus(404);
				data.setMsg("Record Does Not Exist");
			}
		}
		return data;
	}

	public DataContainer findAllTrackingList(Integer clientId) {
		DataContainer data = new DataContainer();
		List<SMSUrlEntity> sMSUrlEntity = null;

		sMSUrlEntity = smsUrlRepository.findAllByClientId(clientId);
		// (customPagenationReportBean.getStartDate(),
		// customPagenationReportBean.getEndDate());
		// PageRequest.of(customCdrReportBean.getPage() - 1,
		// customCdrReportBean.getSize()));
		if (Objects.nonNull(sMSUrlEntity) && sMSUrlEntity.size() > 0) {
			data.setStatus(200);
			data.setMsg("Data Found Successfully.");
			data.setData(sMSUrlEntity);
		} else {
			data.setStatus(400);
			data.setMsg("Record Does Not Exist");
		}

		return data;
	}

	public DataContainer updateByTrackingId(SMSUrlEntity updateSmsUrl) {
		DataContainer data = new DataContainer();
		SMSUrlEntity sMSUrlEntity = null;

		sMSUrlEntity = smsUrlRepository.findByTrackingId(updateSmsUrl.getTrackingId());
		if (Objects.nonNull(sMSUrlEntity)) {
			sMSUrlEntity.setTitle(updateSmsUrl.getTitle());
			sMSUrlEntity.setUrl(updateSmsUrl.getUrl());
			smsUrlRepository.save(sMSUrlEntity);
			data.setStatus(200);
			data.setMsg("Data Updated Successfully.");
			data.setData(sMSUrlEntity);
		} else {
			data.setStatus(400);
			data.setMsg("Record Doesn't Exists.");
		}

		return data;
	}

	public DataContainer deleteByTrackingId(String trackingId) {
		DataContainer data = new DataContainer();
		SMSUrlEntity sMSUrlEntity = null;
		if (Objects.isNull(trackingId) || trackingId.equals("")) {
			data.setStatus(404);
			data.setMsg("Tracking Id Is Empty.");
			return data;
		} else {
			sMSUrlEntity = smsUrlRepository.findByTrackingId(trackingId);
			if (Objects.nonNull(sMSUrlEntity)) {
				Integer deleted = smsUrlRepository.deleteByTrackingId(trackingId);
				if (deleted.equals(1)) {
					data.setStatus(200);
					data.setMsg("TrackingId Deleted Successfully.");
				}
			} else {
				data.setStatus(404);
				data.setMsg("Record Does Not Exist");
			}
		}
		return data;
	}

	public synchronized static String generateTrackingId() {

		String tripId = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
		Date date = new Date(System.currentTimeMillis());
		Random random = new Random();
		tripId = dateFormat.format(date) + String.format(String.format("%02d", (random.nextInt(99) + 1)));
		return tripId;
	}

	public DataContainer getTrackingIdFromRedisCache(String trackingId) {
		DataContainer data = new DataContainer();
		data.setData(redisTemplate.opsForValue().get(trackingId));
		data.setMsg("Success");
		data.setStatus(200);
		return data;
	}
}
