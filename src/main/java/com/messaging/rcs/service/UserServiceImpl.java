package com.messaging.rcs.service;

import static com.messaging.rcs.util.SystemConstants.USER_ADMIN;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.messaging.rcs.configuration.HashMaps;
import com.messaging.rcs.configuration.RabbitQueueService;
import com.messaging.rcs.consumer.QueueName;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.model.User;
import com.messaging.rcs.repository.UserRepository;

/**
 * Created by Rahul Kumar on 2023:12:06
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
	private BeanUtilsBean beanUtils = new BeanUtilsBean();
	@Autowired
	private RabbitQueueService rabbitQueueService;
	@Autowired
	private QueueName queueName;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserEntity createUser(User user, MultipartFile companyBanner, MultipartFile companyLogo) throws Exception {
		LOGGER.info("Service createUser:" + user);
		UserEntity userEntity = new UserEntity();
		try {
			userEntity.setUserPassword(bCryptPasswordEncoder.encode(user.getUserPassword()));
			userEntity.setActive("Y");
			userEntity.setAcctType(user.getAcctType());
			userEntity.setBotId(user.getBotId());
			userEntity.setBotToken(user.getBotToken());
			userEntity.setCompanyName(user.getCompanyName());
			userEntity.setDailyUsageLimit(user.getDailyUsageLimit());
			userEntity.setEmail(user.getEmail());
			userEntity.setFirstName(user.getFirstName());
			userEntity.setIsDeleted(0);
			userEntity.setLastModifiedBy(user.getFirstName());
			userEntity.setLastModifiedDate(new Date());
			userEntity.setLastName(user.getLastName());
			userEntity.setParentUserId(user.getParentUserId());
			// userEntity.setParentUserId(user.getRole_id());
			userEntity.setRole_id(user.getRole_id());
			userEntity.setCopyRight(user.getCopyRight());
			userEntity.setMenuPermission(user.getMenuPermission());
			userEntity.setPhone(user.getPhone());
			userEntity.setPwdResetDate(new Date());
			userEntity.setUserName(user.getUserName());
			if (Objects.nonNull(companyLogo)) {
				userEntity.setCompanyLogo(companyLogo.getBytes());// (ImageUtil.compressImage(companyLogo.getBytes()));
				userEntity.setCompanyLogoFileName(companyLogo.getOriginalFilename());
				userEntity.setCompanyLogoFileType(companyLogo.getContentType());
			}
			if (Objects.nonNull(companyBanner)) {
				userEntity.setCompanyBanner(companyBanner.getBytes());// (ImageUtil.compressImage(companyBanner.getBytes()));
				userEntity.setCompanyBannerFileName(companyBanner.getOriginalFilename());
				userEntity.setCompanyBannerFileType(companyBanner.getContentType());
			}
			// beanUtils.copyProperties(userEntity, user);
			userEntity.setCreatedBy(user.getUserName());
			userEntity.setCreatedDate(new Date());
			userEntity.setAiVideo(user.getAiVideo());
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Got Exception: ", e);
			throw e;
		}
		userEntity = userRepository.save(userEntity);
		try {
			// HashMaps.userEntityHashmap.put(userEntity.getApiKey(), userEntity);
			rabbitQueueService.addNewQueue("telco." + userEntity.getUserName() + ".queue",
					"telco." + userEntity.getUserName() + ".exchange",
					"telco." + userEntity.getUserName() + ".routing");

			rabbitQueueService.addNewQueue(("sms." + userEntity.getUserName() + ".queue"),
					"sms." + userEntity.getUserName() + ".exchange", "sms." + userEntity.getUserName() + ".routing");
		} catch (Exception e) {
			e.printStackTrace();
		}
		userEntity.setUserPassword("*****");
		LOGGER.info("Service user Created Successfully:" + userEntity);
		return userEntity;
	}

	public static String generateBasicAuthToken(String botId, String secretKey) {

		String authString = botId + ":" + secretKey;
		System.out.println("generateBasicAuthToken ::" + authString);
		byte[] encodedAuth = Base64.getEncoder().encode(authString.getBytes(StandardCharsets.UTF_8));
		System.out.println("generateBasicAuthToken :: Basic " + authString);

		return "Basic " + new String(encodedAuth);
	}

	@Override
	public UserEntity updateUser(User user, MultipartFile companyBanner, MultipartFile companyLogo) {
		if (user.getUserId() == 0) {
			throw new RuntimeException("User id not provided !!");
		}

		UserEntity userEntity = userRepository.findByUserId(user.getUserId());
		try {
			if (userEntity == null) {
				throw new RuntimeException("User not found !!");
			}

			/*
			 * if (StringUtils.hasLength(user.getUserName())) { Optional<UserEntity>
			 * optional = userRepository.findByUserName(user.getUserName()); if
			 * (optional.isPresent()) { throw new
			 * RuntimeException("User with this name already in system !!"); }
			 * userEntity.setUserName(user.getUserName()); }
			 */
			if (StringUtils.hasLength(user.getFirstName())) {
				userEntity.setFirstName(user.getFirstName());
			}

			if (StringUtils.hasLength(user.getLastName())) {
				userEntity.setLastName(user.getLastName());
			}

			if (StringUtils.hasLength(user.getEmail())) {
				userEntity.setEmail(user.getEmail());
			}

			if (StringUtils.hasLength(user.getPhone())) {
				userEntity.setPhone(user.getPhone());
			}

			if (StringUtils.hasLength(user.getCompanyName())) {
				userEntity.setCompanyName(user.getCompanyName());
			}

			if (Objects.nonNull(companyLogo)) {
				userEntity.setCompanyLogo(companyLogo.getBytes());// (ImageUtil.compressImage(companyLogo.getBytes()));
				userEntity.setCompanyLogoFileName(companyLogo.getOriginalFilename());
				userEntity.setCompanyLogoFileType(companyLogo.getContentType());
			}
			if (Objects.nonNull(companyBanner)) {
				userEntity.setCompanyBanner(companyBanner.getBytes());// (ImageUtil.compressImage(companyBanner.getBytes()));
				userEntity.setCompanyBannerFileName(companyBanner.getOriginalFilename());
				userEntity.setCompanyBannerFileType(companyBanner.getContentType());
			}
			userEntity.setAcctType(user.getAcctType());

			/*
			 * if (StringUtils.hasLength(user.getActive())) { String loggedInUserName =
			 * "";// getLoggedInUserName(); if (loggedInUserName != null &&
			 * !USER_ADMIN.equalsIgnoreCase(loggedInUserName)) { throw new
			 * RuntimeException("Only Admin can make a user active !!"); }
			 * userEntity.setActive(user.getActive()); }
			 */

			userEntity.setLastModifiedBy(user.getUserName());
			userEntity.setLastModifiedDate(new Date());
			userEntity = userRepository.save(userEntity);
			userEntity.setUserPassword("*****");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userEntity;
	}

	@Override
	public void deleteUser(Long id) throws Exception {
		LOGGER.info("Delete User: " + id);
		// if (loggedInUser != null && USER_ADMIN.equalsIgnoreCase(loggedInUser)) {
		UserEntity userEntity = userRepository.findByUserId(id);
		if (userEntity != null) {
			userRepository.deleteById(id);
			LOGGER.info("User deleted Successfully");
		} else {
			throw new RuntimeException("User not found !!");
		}
		/*
		 * } else { throw new RuntimeException("Unauthorized for this action !!"); }
		 */
	}

	@Override
	public UserEntity getUser(Long userId) {
		UserEntity byUserId = userRepository.findByUserId(userId);
		if (byUserId != null && byUserId.getIsDeleted() == 0) {
			return byUserId;
		} else {
			throw new RuntimeException("User not found !!");
		}
	}

	@Override
	public List<UserEntity> getAdminUser() throws Exception {
		return userRepository.getAdminUser();
	}

	@Override
	public void deactivateUser(Long id, String active) {
		LOGGER.info("Delete User: " + id);
		UserEntity userEntity = userRepository.findByUserId(id);
		if (userEntity != null) {
			userEntity.setActive(active);
			userRepository.save(userEntity);
			LOGGER.info("User Deactivated Successfully");
		} else {
			throw new RuntimeException("User not found !!");
		}
	}

	@Override
	public List<User> getAllUsers() throws InvocationTargetException, IllegalAccessException {
		List<UserEntity> userEntities = userRepository.findAll();
		List<User> users = new ArrayList<>();
		if (!CollectionUtils.isEmpty(userEntities)) {
			for (UserEntity userEntity : userEntities) {
				User user = new User();
				beanUtils.copyProperties(user, userEntity);
				user.setUserPassword("****");
				users.add(user);
			}
		}
		return users;
	}

	@Override
	public UserEntity login(User user) {
		UserEntity userEntity = userRepository.findByUserNameAndUserPassword(user.getUserName(),
				user.getUserPassword());
		userEntity.setUserPassword("****");
		return userEntity;
	}

	/*
	 * @Override public String getLoggedInUserName() { Authentication auth =
	 * SecurityContextHolder.getContext().getAuthentication(); if (auth != null) {
	 * return ((CustomUserDetails) auth.getPrincipal()).getUsername(); } return
	 * null; }
	 * 
	 * @Override public Long getLoggedInUserId() { Authentication auth =
	 * SecurityContextHolder.getContext().getAuthentication(); if (auth != null) {
	 * return ((CustomUserDetails) auth.getPrincipal()).getUserId(); } return null;
	 * }
	 */

	@Override
	public boolean isUserIdFromBodyChildOfLoggedInUser(long possibleParentId, long userIdFromBody) {
		boolean isUserIdFromBodyChildOfLoggedInUser = false;
		List<User> childUsers = new ArrayList<>();
		try {
			getAllDeepChilds(possibleParentId, childUsers, false);
			if (!childUsers.isEmpty()) {
				for (User childUser : childUsers) {
					if (userIdFromBody == childUser.getUserId()) {
						isUserIdFromBodyChildOfLoggedInUser = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Got Exception: ", e);
		}
		return isUserIdFromBodyChildOfLoggedInUser;
	}

	private void getAllDeepChilds(long firstParentId, List<User> allChildUsers, boolean forPresentation)
			throws Exception {
		LOGGER.info("getAllDeepChilds ->" + firstParentId);
		List<UserEntity> userEntities = userRepository.getUserByUserParentId(firstParentId);
		if (userEntities.isEmpty()) {
			return;
		}
		List<User> users = new ArrayList<>();
		for (UserEntity userEntity : userEntities) {
			User user = new User();
			beanUtils.copyProperties(user, userEntity);
			users.add(user);
		}

		allChildUsers.addAll(users);
		users.forEach(t -> {
			try {
				getAllDeepChilds(t.getUserId(), allChildUsers, forPresentation);
			} catch (Exception e) {
				LOGGER.error("Got Exception ", e);
			}
		});
	}

}
