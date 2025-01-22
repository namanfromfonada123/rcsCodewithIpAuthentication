package com.messaging.rcs.controller;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.messaging.rcs.domain.OperatorApiConfigEntity;
import com.messaging.rcs.domain.OperatorEntity;
import com.messaging.rcs.repository.OperatorApiConfigRepository;
import com.messaging.rcs.repository.OperatorRepository;

/**
 * Created By Rahul 2024-11-13 Wednesday
 */
@RestController
@RequestMapping(value = { "/api/v1/rcsmessaging/operator" }, produces = { "application/json" })
public class OperatorController {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private OperatorRepository operatorRepository;

	@Autowired
	private OperatorApiConfigRepository operatorApiConfigRepository;

	@GetMapping("/getOperator")
	public ResponseEntity<?> getOperatorList() {
		HashMap<String, Object> result = new HashMap<>();
		List<OperatorEntity> operator = null;
		operator = operatorRepository.findAll();
		// .stream().map(OperatorEntity::getOperatorName).collect(Collectors.toList());
		try {
			if (operator.size() > 0) {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Data Founded.");
				result.put("data", operator);
			} else {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Data Not Found.");
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.OK);
			result.put("msg", e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/createApi")
	public ResponseEntity<?> createApi(@RequestBody OperatorApiConfigEntity operatorApiConfigEntity) {
		System.out.println("operatorApiConfigEntity :: " + operatorApiConfigEntity.toString());
		HashMap<String, Object> result = new HashMap<>();
		OperatorApiConfigEntity operatorApiConfig = null;
		operatorApiConfigEntity.setCreatedDate(sdf.format(new Date()));
		operatorApiConfigEntity.setUpdatedDate(sdf.format(new Date()));

		operatorApiConfig = operatorApiConfigRepository.save(operatorApiConfigEntity);
		// .stream().map(OperatorEntity::getOperatorName).collect(Collectors.toList());
		try {
			if (operatorApiConfig.getApiConfigId() > 0) {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Success");
				result.put("data", operatorApiConfig);
			} else {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Failed");
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/findAllApi")
	public ResponseEntity<?> findAllApi() {
		HashMap<String, Object> result = new HashMap<>();
		List<OperatorApiConfigEntity> operatorApiConfig = null;
		operatorApiConfig = operatorApiConfigRepository.findAll();
		// .stream().map(OperatorEntity::getOperatorName).collect(Collectors.toList());
		try {
			if (operatorApiConfig.size() > 0) {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Success");
				result.put("data", operatorApiConfig);
			} else {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Data Not Exist.");
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/deleteApiById")
	public ResponseEntity<?> deleteApiById(@RequestParam("id") Long id) {
		HashMap<String, Object> result = new HashMap<>();
		Optional<OperatorApiConfigEntity> operatorApiConfig = null;
		operatorApiConfig = operatorApiConfigRepository.findById(id);
		// .stream().map(OperatorEntity::getOperatorName).collect(Collectors.toList());
		try {
			if (Objects.nonNull(operatorApiConfig)) {
				operatorApiConfigRepository.deleteById(id);
				result.put("status", HttpStatus.OK);
				result.put("msg", "Delete Successfully");
			} else {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Data Not Exist.");
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/findApiById")
	public ResponseEntity<?> findApiById(@RequestParam("id") Long id) {
		HashMap<String, Object> result = new HashMap<>();
		Optional<OperatorApiConfigEntity> operatorApiConfig = null;
		operatorApiConfig = operatorApiConfigRepository.findById(id);
		// .stream().map(OperatorEntity::getOperatorName).collect(Collectors.toList());
		try {
			if (Objects.nonNull(operatorApiConfig)) {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Record Founded.");
				result.put("data", operatorApiConfig);

			} else {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Record Not Exist.");
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/updateApiById")
	public ResponseEntity<?> updateApiById(@RequestBody OperatorApiConfigEntity operatorApiConfigEntity) {
		System.out.println("operatorApiConfigEntity :: " + operatorApiConfigEntity.toString());
		HashMap<String, Object> result = new HashMap<>();
		OperatorApiConfigEntity operatorApiConfig = null;
		operatorApiConfig = operatorApiConfigRepository.findById(operatorApiConfigEntity.getApiConfigId()).get();
		// .stream().map(OperatorEntity::getOperatorName).collect(Collectors.toList());
		try {
			if (Objects.nonNull(operatorApiConfig)) {
				if (!operatorApiConfig.getApiUrl().equals(operatorApiConfigEntity.getApiUrl())) {
					operatorApiConfig.setApiUrl(operatorApiConfigEntity.getApiUrl());
				}

				operatorApiConfig.setUpdatedDate(sdf.format(new Date()));

				operatorApiConfigRepository.save(operatorApiConfig);
				result.put("status", HttpStatus.OK);
				result.put("msg", "Updated Successfully");
			} else {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Data Not Exist.");
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	public static String generateBasicAuthToken(String botId, String secretKey) {

		String authString = botId + ":" + secretKey;
		System.out.println("generateBasicAuthToken ::" + authString);
		byte[] encodedAuth = Base64.getEncoder().encode(authString.getBytes(StandardCharsets.UTF_8));
		System.out.println("generateBasicAuthToken :: Basic " + authString);

		return "Basic " + new String(encodedAuth);
	}
}
