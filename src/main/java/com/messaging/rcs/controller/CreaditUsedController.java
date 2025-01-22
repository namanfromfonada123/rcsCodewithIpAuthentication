package com.messaging.rcs.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.messaging.rcs.domain.CreditUsedEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.repository.CreaditUsedRepository;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.util.DateUtils;

@RestController
@RequestMapping(value = { "/api/v1/rcsmessaging/credit" }, produces = APPLICATION_JSON_VALUE)
public class CreaditUsedController {
	@Autowired
	private CreaditUsedRepository creaditUsedRepository;

	@Autowired
	private UserRepository userRepository;

	@PostMapping(value = { "/createCredit" })
	public ResponseEntity<?> createCredit(@RequestBody CreditUsedEntity creditUsedEntity) {
		HashMap<String, Object> result = new HashMap<>();
		try {

			if (creditUsedEntity.getAcctCredit() == 0) {
				result.put("status", HttpStatus.NOT_FOUND);
				result.put("msg", "Credit Cannot Be Empty");
				return new ResponseEntity<>(result, HttpStatus.OK);

			}
			creditUsedEntity.setCreatedDate(DateUtils.getStringDateInTimeZone(new Date()));
			creditUsedEntity.setUpdateDate(DateUtils.getStringDateInTimeZone(new Date()));

			creditUsedEntity = creaditUsedRepository.save(creditUsedEntity);
			if (creditUsedEntity.getCrdId() != 0) {
				UserEntity userEntity = null;
				userEntity = userRepository.findByUserId(Long.valueOf(creditUsedEntity.getUserId()));
				if (Objects.nonNull(userEntity)) {
					if (creditUsedEntity.getMsgType().equalsIgnoreCase("RCS")) {
						Integer rcsCredit = userRepository.findSumRcsCredit(userEntity.getParentUserId());
						if (rcsCredit != 0) {
							long parentBalanceCredit=Long.valueOf(rcsCredit)-creditUsedEntity.getAcctCredit();
							System.out.println("*** parentBalanceCredit ***** "+parentBalanceCredit);
							userRepository.updateRcsCredit(userEntity.getParentUserId(),parentBalanceCredit);
							userEntity.setCreditBalance(creditUsedEntity.getAcctCredit());
							userEntity.setTotalRcsCredit(creditUsedEntity.getAcctCredit());
						} else {
							userEntity.setCreditBalance(creditUsedEntity.getAcctCredit());
							userEntity.setTotalRcsCredit(creditUsedEntity.getAcctCredit());
						}
					} else if (creditUsedEntity.getMsgType().equalsIgnoreCase("SMS")) {

						Integer smsCredit = userRepository.findSumSMSCredit(userEntity.getParentUserId());
						if (smsCredit != 0) {
							long parentSMSBalanceCredit=Long.valueOf(smsCredit)-creditUsedEntity.getAcctCredit();
							System.out.println("*** parentBalanceCredit ***** "+parentSMSBalanceCredit);
							
							userRepository.updateSMSCredit(userEntity.getParentUserId(),parentSMSBalanceCredit);

							userEntity.setSmsCreditBalance(creditUsedEntity.getAcctCredit());
							userEntity.setTotalSMSCredit(creditUsedEntity.getAcctCredit());
						} else {
							userEntity.setSmsCreditBalance(creditUsedEntity.getAcctCredit());
							userEntity.setTotalSMSCredit(creditUsedEntity.getAcctCredit());
						}
					} else {
						Integer whatsCredit = userRepository.findSumWhatsAppCredit(userEntity.getParentUserId());
						if (whatsCredit != 0) {
							long parentWhatsAppBalanceCredit=Long.valueOf(whatsCredit)-creditUsedEntity.getAcctCredit();
							System.out.println("*** parentBalanceCredit ***** "+parentWhatsAppBalanceCredit);
							userRepository.updateWhatsAppCredit(userEntity.getParentUserId(),whatsCredit-parentWhatsAppBalanceCredit);
							userEntity.setWhatsAppCreditBalance(creditUsedEntity.getAcctCredit());
							userEntity.setTotalWhatsAppCredit(creditUsedEntity.getAcctCredit());
						} else {
							userEntity.setWhatsAppCreditBalance(creditUsedEntity.getAcctCredit());
							userEntity.setTotalWhatsAppCredit(creditUsedEntity.getAcctCredit());
						}
					}
				}
				userRepository.save(userEntity);
				result.put("status", HttpStatus.OK);
				result.put("msg", "Success");
			} else {
				result.put("status", HttpStatus.NOT_FOUND);
				result.put("msg", "Not Found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", "Internal Server Error ::" + e.getMessage());
		}
		return new ResponseEntity<>(result, HttpStatus.OK);

	}

	@GetMapping(value = { "/findAllCredit" })
	public ResponseEntity<?> findAllCredit(@RequestParam("userId") String userId) {
		HashMap<String, Object> result = new HashMap<>();
		try {

			List<CreditUsedEntity> creditUsedEntity = null;
			creditUsedEntity = creaditUsedRepository.findAllByUserId(userId);
			if (Objects.nonNull(creditUsedEntity)) {
				result.put("status", HttpStatus.OK);
				result.put("msg", "Success");
				result.put("data", creditUsedEntity);
			} else {
				result.put("status", HttpStatus.NOT_FOUND);
				result.put("msg", "Not Found");
			}
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", "Internal Server Error ::" + e.getMessage());
		}
		return new ResponseEntity<>(result, HttpStatus.OK);

	}

	@GetMapping(value = { "/findAllCreditForAdmin" })
	public ResponseEntity<?> findAllCreditForAdmin() {
		HashMap<String, Object> result = new HashMap<>();
		try {

			List<CreditUsedEntity> creditUsedEntity = null;
			creditUsedEntity = creaditUsedRepository.findAll();
			if (Objects.nonNull(creditUsedEntity)) {
				for (CreditUsedEntity crd : creditUsedEntity) {
					crd.setUserId(userRepository.findById(Long.valueOf(crd.getUserId())).get().getUserName());
				}
				result.put("status", HttpStatus.OK);
				result.put("msg", "Success");
				result.put("data", creditUsedEntity);
			} else {
				result.put("status", HttpStatus.NOT_FOUND);
				result.put("msg", "Not Found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			result.put("msg", "Internal Server Error ::" + e.getMessage());
		}
		return new ResponseEntity<>(result, HttpStatus.OK);

	}
}
