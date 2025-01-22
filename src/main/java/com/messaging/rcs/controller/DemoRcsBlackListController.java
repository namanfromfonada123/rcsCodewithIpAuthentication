package com.messaging.rcs.controller;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.messaging.rcs.domain.CallBackMsgStatusEntity;
import com.messaging.rcs.repository.CallBackMsgStatusRepository;

@RestController
@RequestMapping(value = "/api/v1/rcsmessaging/demoRcs/callBackReport")
public class DemoRcsBlackListController {
	Logger log = LoggerFactory.getLogger(DemoRcsBlackListController.class);
	
	@Autowired
	private CallBackMsgStatusRepository callBackMsgStatusRepository;

	@PostMapping("/findRcsCallBackMsgStatusFromDemoRcs")
	public ResponseEntity<?> findCallBackMsgStatusBasedTextMessageIsNotNull(@RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate, @RequestParam("userName") String userName) {
		HashMap<String, Object> map = new HashMap<>();
		log.info("Request From demoRcs/findListBasedTextMessageIsNotNull Api:: " + userName);

		List<CallBackMsgStatusEntity> blackList = null;
		try {
			String start = startDate + " 00:00:00";
			String end = endDate + " 23:59:59";
			blackList = callBackMsgStatusRepository.findByTextMessageNotNullAndIsCompleteIsOne(userName, start, end);
			if (blackList.size() > 0) {
				map.put("blacklist", blackList);
				map.put("status", 200);
				map.put("msg", "Record Founded.");
				return new ResponseEntity<Object>(map, HttpStatus.OK);
			} else {
				map.put("blacklist", blackList);
				map.put("status", 204);
				map.put("msg", "Record Not Found.");
				return new ResponseEntity<Object>(map, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			map.put("status", 500);
			map.put("msg", e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<Object>(map, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}
}
