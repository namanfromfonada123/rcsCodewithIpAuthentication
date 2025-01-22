package com.messaging.rcs.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.messaging.rcs.domain.SMSUrlEntity;
import com.messaging.rcs.model.DataContainer;
import com.messaging.rcs.model.SmsUrlDto;
import com.messaging.rcs.service.SMSUrlService;

@RestController
@RequestMapping(value = { "/api/v1/rcsmessaging/sms/url" })
public class SMSUrlController {
	@Autowired
	private SMSUrlService smsUrlService;

	@PostMapping({"/createSmsUrl"})
	public String createUrl(@RequestBody SmsUrlDto smsUrlDto) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		DataContainer data = smsUrlService.createUrl(smsUrlDto);
		return gson.toJson(data).toString();

	}

	@PostMapping({"/findByTrackingIdFromRedisCache"})
	public String findByTrackingIdFromRedisCache(@RequestParam("trackingId") String trackingId) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		DataContainer data = smsUrlService.getTrackingIdFromRedisCache(trackingId);
		return gson.toJson(data).toString();

	}

	@PostMapping({"/findByTrackingId"})
	public String findByTrackingId(@RequestParam("trackingId") String trackingId) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		DataContainer data = smsUrlService.findByTrackingId(trackingId);
		return gson.toJson(data).toString();

	}

	@GetMapping({"/findAllTrackingList"})
	public String findAllTrackingList(@RequestParam("clientId") Integer clientId) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		DataContainer data = null;
		if (clientId == 0) {
			data = new DataContainer();
			data.setMsg("Client Id Cannot Be Empty.");
			data.setStatus(404);
			return gson.toJson(data).toString();
		}
		data = smsUrlService.findAllTrackingList(clientId);// (customPagenationReportBean);
		return gson.toJson(data).toString();

	}

	@PostMapping({"/deleteByTrackingId"})
	public String deleteByTrackingId(@RequestParam("trackingId") String trackingId) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		DataContainer data = smsUrlService.deleteByTrackingId(trackingId);
		return gson.toJson(data).toString();

	}

	@PostMapping({"/updateByTrackingId"})
	public String updateByTrackingId(@RequestBody SMSUrlEntity smsUrlEntity) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		DataContainer data = smsUrlService.updateByTrackingId(smsUrlEntity);
		return gson.toJson(data).toString();

	}
}
