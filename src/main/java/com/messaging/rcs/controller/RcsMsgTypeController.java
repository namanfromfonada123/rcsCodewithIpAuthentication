package com.messaging.rcs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.messaging.rcs.domain.RcsMsgTypeEntity;
import com.messaging.rcs.repository.RcsMsgTypeRepository;

@RestController
@RequestMapping(value = { "/api/v1/rcsmessaging/rcsMsgType" }, produces = { "application/json" })
public class RcsMsgTypeController {

	@Autowired
	private RcsMsgTypeRepository rcsMsgTypeRepository;

	@GetMapping({ "/findAllRcsMsgType" })
	public ResponseEntity<?> findAllRcsMsgType() {
		HashMap<String, Object> result = new HashMap<String, Object>();
		List<RcsMsgTypeEntity> rcsMsgTypeList = null;
		try {
			rcsMsgTypeList = this.rcsMsgTypeRepository.findAll();
			if (Objects.nonNull(rcsMsgTypeList)) {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Data Founded.");
				result.put("data", rcsMsgTypeList);
				return new ResponseEntity<>(result, HttpStatus.OK);

			} else {
				result.put("status", HttpStatus.NOT_FOUND);
				result.put("msg", "Data Not Found.");
				return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);

			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", "Internal Server Error");
			e.printStackTrace();
			return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
}
