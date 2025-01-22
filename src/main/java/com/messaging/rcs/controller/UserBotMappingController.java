package com.messaging.rcs.controller;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.messaging.rcs.domain.UserBotMappingEntity;
import com.messaging.rcs.domain.UsersRole;
import com.messaging.rcs.model.DataContainer;
import com.messaging.rcs.repository.UserBotMappingRepository;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.repository.UserRoleRepo;

@RestController
@RequestMapping("/api/v1/rcsmessaging/multipleUserBot")
public class UserBotMappingController {
	private static final Logger LOGGER = Logger
			.getLogger(com.messaging.rcs.controller.UserBotMappingController.class.getName());

	@Autowired
	private UserBotMappingRepository userBotMappingRepository;

	@Autowired
	private UserRoleRepo userRoleRepo;
	@Autowired
	private UserRepository userRepository;

	@PostMapping("/createBot")
	public String createBot(@RequestBody UserBotMappingEntity userBotMappingEntity) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		DataContainer data = new DataContainer();
		UserBotMappingEntity userBotMappingList = null;
		try {
			if (Objects.isNull(userBotMappingEntity.getBotId())) {
				data.setStatus(400);
				data.setMsg("Bot Id Is Empty.");
				return gson.toJson(data).toString();

			} else if (Objects.isNull(userBotMappingEntity.getBotType())) {
				data.setStatus(400);
				data.setMsg("Bot Type Is Empty.");
				return gson.toJson(data).toString();

			} else if (Objects.isNull(userBotMappingEntity.getBotName())) {
				data.setStatus(400);
				data.setMsg("Bot Name Is Empty.");
				return gson.toJson(data).toString();

			} else if (Objects.isNull(userBotMappingEntity.getSecretKey())) {
				data.setStatus(400);
				data.setMsg("SecretKey Is Empty.");
				return gson.toJson(data).toString();

			} else if (Objects.isNull(userBotMappingEntity.getBrand())) {
				data.setStatus(400);
				data.setMsg("Bot Brand Is Empty.");
				return gson.toJson(data).toString();

			} else {

				userBotMappingList = userBotMappingRepository
						.findBotsListByUserIdAndBotId(userBotMappingEntity.getBotId());
				if (Objects.isNull(userBotMappingList)) {
					userBotMappingEntity.setBasicToken(generateBasicAuthToken(userBotMappingEntity.getBotId(),
							userBotMappingEntity.getSecretKey()));
					userBotMappingRepository.save(userBotMappingEntity);
					data.setStatus(200);
					data.setMsg("Data Saved Successfully.");
					data.setData(userBotMappingList);
					LOGGER.info("***** Inside createBot ::" + data.getMsg());
					return gson.toJson(data).toString();
				} else {
					data.setStatus(202);
					data.setMsg("Record Already Exist");
					LOGGER.info("***** Inside createBot ::" + data.getMsg());
					return gson.toJson(data).toString();

				}
			}
		} catch (

		Exception e) {
			data.setStatus(500);
			data.setMsg(e.getMessage());
			e.printStackTrace();
			return gson.toJson(data).toString();

		}
	}

	/**
	 * 
	 * @param roleId
	 * @return
	 * @throws ParseException
	 */
	@GetMapping("/findBotsByRoleId")
	public String findBotsByRoleId(@RequestParam("userId") String roleId) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		DataContainer data = new DataContainer();
		List<UserBotMappingEntity> userBotMappingList = null;
		Optional<UsersRole> userRole = null;
		List<Long> userIds = null;

		try {
			userRole = userRoleRepo.findById(Integer.valueOf(roleId));
			if (userRole.isPresent()) {
				userIds = userRepository.findUsersByRoleId(Long.valueOf(userRole.get().getId()));
				userBotMappingList = userBotMappingRepository.findBotsListByUserId(userIds);

			}
			if (Objects.nonNull(userBotMappingList)) {
				data.setStatus(200);
				data.setMsg("Data Found Successfully.");
				data.setData(userBotMappingList);
				LOGGER.info("***** Inside findBotsByRoleId ::" + data.getMsg());
				return gson.toJson(data).toString();
			} else {
				data.setStatus(400);
				data.setMsg("Record Does Not Exist");
				LOGGER.info("***** Inside findBotsByRoleId ::" + data.getMsg());
				return gson.toJson(data).toString();

			}

		} catch (Exception e) {
			data.setStatus(500);
			data.setMsg(e.getMessage());
			e.printStackTrace();
			return gson.toJson(data).toString();

		}
	}

	@GetMapping("/findBotsByUserId")
	public String findBotsByUserId(@RequestParam("userId") String userId) throws ParseException {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		DataContainer data = new DataContainer();
		List<UserBotMappingEntity> userBotMappingList = null;
		Optional<UsersRole> userRole = null;
		List<Long> userIds = null;

		try {

			userBotMappingList = userBotMappingRepository.findBotsListByUserId(Arrays.asList(Long.valueOf(userId)));

			if (Objects.nonNull(userBotMappingList)) {
				data.setStatus(200);
				data.setMsg("Data Found Successfully.");
				data.setData(userBotMappingList);
				LOGGER.info("***** Inside findBotsByRoleId ::" + data.getMsg());
				return gson.toJson(data).toString();
			} else {
				data.setStatus(400);
				data.setMsg("Record Does Not Exist");
				LOGGER.info("***** Inside findBotsByRoleId ::" + data.getMsg());
				return gson.toJson(data).toString();

			}
		} catch (Exception e) {
			data.setStatus(500);
			data.setMsg(e.getMessage());
			e.printStackTrace();
			return gson.toJson(data).toString();

		}
	}

	public static String generateBasicAuthToken(String botId, String secretKey) {

		String authString = botId + ":" + secretKey;
		System.out.println("generateBasicAuthToken ::" + authString);
		byte[] encodedAuth = Base64.getEncoder().encode(authString.getBytes(StandardCharsets.UTF_8));
		System.out.println("generateBasicAuthToken :: Basic " + authString);

		return "Basic " + new String(encodedAuth);
	}
}
