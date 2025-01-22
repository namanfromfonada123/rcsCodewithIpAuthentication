package com.messaging.rcs.rbm.controller;

import com.messaging.rcs.domain.MessageTypeEntity;
import com.messaging.rcs.rbm.service.RbmMessageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.messaging.rcs.util.SystemConstants.BASE_URL;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * 
 * @author RahulRajput 2023-06-16
 *
 */
@CrossOrigin
@RestController
@RequestMapping(value = BASE_URL, produces = APPLICATION_JSON_VALUE)
public class MessageController {

	private static final Logger LOGGER = Logger.getLogger(MessageController.class.getName());
	private static final String STATUS = "Status";
	private static final String MESSAGE = "message";

	@Autowired
	private RbmMessageService rbmMessageService;

	@GetMapping(value = "/messageType", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAllMessageTypes() {
		Map<String, Object> result = new HashMap<>();
		List<MessageTypeEntity> allMessageTypes = rbmMessageService.getAllMessageTypes();

		result.put(STATUS, "Success");
		result.put(MESSAGE, allMessageTypes);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping(value = "/addMessage/{campaignId}/{messageId}")
	public ResponseEntity<?> addMessageToCampaign(@PathVariable(name = "campaignId") Long campaignId,
			@PathVariable(name = "messageId") Long messageId, @RequestParam(name = "messageBody") String messageBody) {
		Map<String, Object> result = new HashMap<>();
		try {
			rbmMessageService.addMessageToCampaign(campaignId, messageId, messageBody);
			result.put(STATUS, "Success");
			result.put(MESSAGE, "Done");
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put(STATUS, "Failure");
			result.put(MESSAGE, e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
	}
}
