package com.messaging.rcs.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messaging.rcs.domain.UserBotMappingEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.domain.UserRolesRepository;
import com.messaging.rcs.domain.UsersRole;
import com.messaging.rcs.model.ResetPasswordModel;
import com.messaging.rcs.model.User;
import com.messaging.rcs.repository.TemplateRepository;
import com.messaging.rcs.repository.UserBotMappingRepository;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.service.UserService;
import com.messaging.rcs.util.EncryptionUtil;
import com.messaging.rcs.util.PasswordUtil;
import com.messaging.rcs.util.SendEmail;

@RestController
@CrossOrigin
@RequestMapping(value = { "/api/v1/rcsmessaging/user" }, produces = { "application/json" })
public class UserController {
	private static final Logger LOGGER = Logger.getLogger(com.messaging.rcs.controller.UserController.class.getName());

	private static final String STATUS = "Status";

	private static final String MESSAGE = "message";

	public static final String USER = "USER";

	private EncryptionUtil encUtil = new EncryptionUtil();

	private PasswordUtil pwdUtil = new PasswordUtil();

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	private BeanUtilsBean beanUtils = new BeanUtilsBean();

	@Autowired
	private SendEmail sendEmail;

	@Autowired
	private UserRolesRepository userRolesRepository;

	@Autowired
	private UserBotMappingRepository userBotMappingRepository;
	@Autowired
	private TemplateRepository tempalteRepo;

	@PostMapping({ "/createUser" })
	public ResponseEntity createUser(@RequestParam("userJson") String userJson,
			@RequestPart(value = "companyLogo", required = false) MultipartFile companyLogo,
			@RequestPart(value = "companyBanner", required = false) MultipartFile companyBanner) throws Exception {
		LOGGER.info("Service create user");
		Map<String, Object> result = new HashMap<>();
		UserEntity returnedUser = null;
		try {
			User user = (User) (new ObjectMapper()).readValue(userJson, User.class);
			if (Objects.isNull(user.getUserName())) {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "User Name Cannot be NULL.");
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			/*
			 * if (Objects.isNull(user.getBotId())) { result.put("Status",
			 * HttpStatus.NOT_FOUND.toString()); result.put("message",
			 * "Bot ID Cannot Be Empty."); return new ResponseEntity(result,
			 * HttpStatus.NOT_FOUND); }
			 */
			/*
			 * if (!user.getBotToken().contains("Basic ")) { result.put("Status",
			 * HttpStatus.NOT_FOUND.toString()); result.put("message",
			 * "Bot Token Should Be Started Basic Token"); return new ResponseEntity(result,
			 * HttpStatus.NOT_FOUND); }
			 */
			/*
			 * if (Objects.isNull(user.getBotToken())) { result.put("Status",
			 * HttpStatus.NOT_FOUND.toString()); result.put("message",
			 * "Bot Token Cannot Be Empty."); return new ResponseEntity(result,
			 * HttpStatus.NOT_FOUND); }
			 */
			if (this.userRepository.findByUserName(user.getUserName()).isPresent()) {
				result.put("Status", HttpStatus.CREATED.toString());
				result.put("message", "User Already Exist.");
				return new ResponseEntity(result, HttpStatus.CREATED);
			}
			returnedUser = this.userService.createUser(user, companyBanner, companyLogo);
			if (returnedUser == null) {
				result.put("Status", "Error");
				result.put("message", "Unable to create user");
				return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
			}
			result.put("Status", HttpStatus.OK.toString());
			User user2 = new User();
			this.beanUtils.copyProperties(user2, returnedUser);
			result.put("USER", user2);
			return new ResponseEntity(result, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("Status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = { "/login" }, produces = { "application/json" }, consumes = { "application/json" })
	public final ResponseEntity<?> login(@RequestBody User user) throws Exception {
		LOGGER.info("Fetch User Controller");
		HashMap<String, Object> result = new HashMap<>();
		try {
			UserEntity returnedUser = this.userService.login(user);
			if (returnedUser == null) {
				LOGGER.error("Invalid Credentials");
				result.put("Status", "Error");
				result.put("message", "Invalid Credentials");
				return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
			}
			LOGGER.info("User retrieved successfully");
			returnedUser.setUserPassword("*****");
			result.put("USER", returnedUser);
			result.put("Status", HttpStatus.OK.toString());
			return new ResponseEntity(result, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Error:" + e);
			result.put("Status", "Error");
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping({ "/logout" })
	public ResponseEntity<?> logout() {
		SecurityContextHolder.clearContext();
		return ResponseEntity.ok("Success");
	}

	@PostMapping({ "/update" })
	public final ResponseEntity<?> updateInfo(@RequestParam("userJson") String userJson,
			@RequestPart(value = "companyLogo", required = false) MultipartFile companyLogo,
			@RequestPart(value = "companyBanner", required = false) MultipartFile companyBanner) throws Exception {
		LOGGER.info("Fetch User Controller");
		User user = (User) (new ObjectMapper()).readValue(userJson, User.class);
		UserEntity returnedUser = this.userService.updateUser(user, companyBanner, companyLogo);
		HashMap<String, Object> result = new HashMap<>();
		if (returnedUser == null) {
			result.put("Status", "404");
			result.put("message", "User Could Not Update.");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		returnedUser.setUserPassword("*****");
		result.put("USER", returnedUser);
		result.put("Status", HttpStatus.OK.toString());
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping(value = { "/findByUserId" }, produces = { "application/json" })
	public final ResponseEntity<?> getUser(@RequestParam("id") Long id) throws Exception {
		LOGGER.info("getUser->" + id);
		HashMap<String, Object> result = new HashMap<>();
		User user = new User();
		UserEntity returnedUser = this.userService.getUser(id);
		if (returnedUser == null) {
			result.put("Status", "Error");
			result.put("message", "User Not Found !!");
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
		result.put("Status", HttpStatus.OK.toString());
		result.put("USER", returnedUser);
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@GetMapping(value = { "/deleteByUserId" }, produces = { "application/json" })
	public final ResponseEntity<?> deleteUser(@RequestParam("id") Long id) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		try {
			this.userService.deleteUser(id);
			UserEntity userEntity = null;
			userEntity = userRepository.findByUserId(id);
			if (userEntity != null) {
				userRepository.deleteById(id);
				LOGGER.info("User deleted Successfully");
				result.put("Status", HttpStatus.OK.toString());
				result.put("message", "User deleted Successfully");
				return new ResponseEntity(result, HttpStatus.OK);
			} else {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "User not found !!");
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);

			}

		} catch (Exception e) {
			result.put("Status", "Failure");
			result.put("message", e.getLocalizedMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = { "/deactivateUserByUserId" }, produces = { "application/json" })
	public final ResponseEntity<?> deactivateUser(@RequestParam("id") Long id, @RequestParam("active") String active)
			throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		try {
			this.userService.deactivateUser(id, active);
			result.put("Status", HttpStatus.OK.toString());
			result.put("message", "User Deactivated Successfully");
			return new ResponseEntity(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put("Status", "Failure");
			result.put("message", e.getLocalizedMessage());
			return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = { "/findAllUser" }, produces = { "application/json" })
	public final ResponseEntity<List<User>> getUserAll() throws Exception {
		List<User> users = this.userService.getAllUsers();
		return new ResponseEntity(users, HttpStatus.OK);
	}

	public ResponseEntity<HashMap<String, Object>> forgotPassword(@RequestParam("userName") String userName,
			@RequestParam("mail") String mail) {
		UserEntity user = null;
		HashMap<String, Object> result = new HashMap<>();
		user = this.userRepository.findByUserNameAndEmail(userName, mail);
		if (Objects.isNull(user)) {
			result.put("Status", HttpStatus.NOT_FOUND.toString());
			result.put("message", "User Not Found");
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		}
		String pass = this.pwdUtil.generatePassword(8);
		System.out.println("System Generated Forgot Password Password::=>" + pass);
		user.setUserPassword(this.bCryptPasswordEncoder.encode(pass));
		this.sendEmail.sendEmail(user, pass);
		this.userRepository.save(user);
		result.put("Status", HttpStatus.OK.toString());
		result.put("message", "Password Sent To Given Mail.");
		return new ResponseEntity(result, HttpStatus.OK);
	}

	public boolean validatePassword(String password) {

		String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8}$";
		Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
		LOGGER.info("***** New Password ******" + password);

		if (password == null) {
			LOGGER.info(
					"***** Password is invalid! Ensure it has 8 characters with at least one uppercase letter, one lowercase letter, one digit, and one special character. ******"
							+ password);

			return false;
		} else {
			LOGGER.info("***** Password is invalid *****");

			return pattern.matcher(password).matches();
		}
	}

	@PostMapping(value = { "/reset-pwd" }, produces = { "application/json" })
	public ResponseEntity<HashMap<String, Object>> resetPassword(@RequestBody ResetPasswordModel resetPasswordModel) {
		LOGGER.info("***** Controller Inside resetPassword() ******");
		HashMap<String, Object> result = new HashMap<>();
		UserEntity user = null;
		boolean matchPass = true;

		try {
			boolean sss = validatePassword(resetPasswordModel.getNewPassword());
			if (validatePassword(resetPasswordModel.getNewPassword())) {
				user = this.userRepository.findByUserId(resetPasswordModel.getUserId());
				Calendar c1 = new GregorianCalendar();
				c1.add(5, 30);
				Date date = c1.getTime();
				if (Objects.nonNull(user)) {
					if (matchPass == this.bCryptPasswordEncoder.matches(resetPasswordModel.getOldPassword(),
							user.getUserPassword())) {
						LOGGER.info("***** Reset Password Successfully *****");
						user.setUserPassword(this.bCryptPasswordEncoder.encode(resetPasswordModel.getNewPassword()));
						user.setPwdResetDate(date);
						this.userRepository.save(user);
						result.put("Status", HttpStatus.OK.toString());
						result.put("message", "Password Successfully Reset.");
						return new ResponseEntity(result, HttpStatus.OK);
					}
					result.put("Status", HttpStatus.NOT_FOUND.toString());
					result.put("message", "Old Password Not Valid.");
					return new ResponseEntity(result, HttpStatus.NOT_FOUND);
				}
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message", "User Not Found.");
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			} else {
				result.put("Status", HttpStatus.NOT_FOUND.toString());
				result.put("message",
						"Password is invalid! Ensure it has 8 characters with at least one uppercase letter, one lowercase letter, one digit, and one special character");
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("Status", HttpStatus.NOT_FOUND.toString());
			result.put("message", e.getMessage());
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping(value = { "/getRoles" }, produces = { "application/json" })
	public final ResponseEntity<List<UsersRole>> getRoles() throws Exception {
		List<UsersRole> users = this.userRolesRepository.findAll();
		return new ResponseEntity(users, HttpStatus.OK);
	}

	@GetMapping(value = { "/findAllUsersByRolesId" }, produces = { "application/json" })
	public final ResponseEntity<?> findAllUsersByRolesIdOrUserId(@RequestParam("role") String role) throws Exception {
		LOGGER.info("getroleI->" + role);
		List<Long> userIds = null;
		Optional<UsersRole> userRole = null;
		List<UserEntity> userList = null;
		List<User> users = new ArrayList<>();
		HashMap<String, Object> result = new HashMap<>();
		UsersRole roles = null;
		try {
			LOGGER.info("***** GETTING USERS BASED ON ROLE_ID *****");
			roles = userRolesRepository.findByName(role);

			userList = userRepository.findAllUsersByRoleId(roles.getId());

			if (!userList.isEmpty()) {

				for (UserEntity userEntity : userList) {
					User user = new User();
					beanUtils.copyProperties(user, userEntity);
					user.setUserPassword("****");
					users.add(user);
				}
				result.put("Status", HttpStatus.OK.toString());
				result.put("USER", users);
				return new ResponseEntity(result, HttpStatus.OK);
			} else {
				result.put("Status", "404");
				result.put("message", "User Not Found !!");
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("Status", "500");
			result.put("message", "Internal Server Error");
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = { "/findAllUsersByuserId" }, produces = { "application/json" })
	public final ResponseEntity<?> findAllUsersByUserId(@RequestParam("userId") Long userId) throws Exception {
		LOGGER.info("getroleI->" + userId);
		List<Long> userIds = null;
		Optional<UsersRole> userRole = null;
		List<UserEntity> userList = null;
		List<User> users = new ArrayList<>();
		HashMap<String, Object> result = new HashMap<>();

		// userRole =
		// userRolesRepository.findById(Integer.valueOf(String.valueOf(userId)));
		LOGGER.info("***** GETTING USERS BASED ON ROLE_ID *****");

		// userIds =
		// userRepository.findUsersByRoleId(Long.valueOf(userRole.get().getId()));
		userList = userRepository.findAllUsersByUserId(userId);

		if (!userList.isEmpty()) {

			for (UserEntity userEntity : userList) {
				User user = new User();
				beanUtils.copyProperties(user, userEntity);
				user.setUserPassword("****");
				users.add(user);
			}
			result.put("Status", HttpStatus.OK.toString());
			result.put("USER", users);
			return new ResponseEntity(result, HttpStatus.OK);
		} else {
			result.put("Status", "404");
			result.put("message", "User Not Found !!");
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(value = { "/findAllUsersByParentUserId" }, produces = { "application/json" })
	public final ResponseEntity<?> findAllUsersByParentUserId(@RequestParam("parentUserId") Long parentUserId)
			throws Exception {

		List<UserEntity> userList = null;
		List<User> users = new ArrayList<>();
		HashMap<String, Object> result = new HashMap<>();
		LOGGER.info("***** GETTING USERS BASED ON parentUserId *****");

		userList = userRepository.getUserByUserParentId(parentUserId);

		if (!userList.isEmpty()) {

			for (UserEntity userEntity : userList) {
				User user = new User();
				beanUtils.copyProperties(user, userEntity);
				user.setUserPassword("****");
				users.add(user);
			}
			result.put("Status", HttpStatus.OK.toString());
			result.put("USER", users);
			return new ResponseEntity(result, HttpStatus.OK);
		} else {
			result.put("Status", "404");
			result.put("message", "User Not Found !!");
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(value = { "/createRole" }, produces = { "application/json" })
	public final ResponseEntity<?> createRoles(@RequestParam("roleName") String roleName) throws Exception {
		UsersRole role = null;
		HashMap<String, Object> result = new HashMap<>();

		role = this.userRolesRepository.findByName(roleName);
		try {
			if (Objects.isNull(role)) {
				UsersRole createRole = new UsersRole();
				createRole.setName(roleName);
				createRole = userRolesRepository.save(createRole);
				result.put("msg", "Submit Successfully.");
				result.put("stuats", 200);
				result.put("role", createRole);
				return new ResponseEntity(result, HttpStatus.OK);
			} else {
				result.put("msg", "Record Already Exits.");
				result.put("stuats", 201);
				return new ResponseEntity(result, HttpStatus.CREATED);
			}
		} catch (Exception e) {
			result.put("msg", e.getMessage());
			result.put("stuats", 500);
			e.printStackTrace();
			return new ResponseEntity(result, HttpStatus.CREATED);

		}
	}

	@GetMapping(value = { "/findParentUserNameByUserId" }, produces = { "application/json" })
	public final ResponseEntity<?> findParentUserNameByUserId(@RequestParam("userId") Long userId) throws Exception {

		List<String> userList = null;
		List<User> users = new ArrayList<>();
		HashMap<String, Object> result = new HashMap<>();
		LOGGER.info("*****  findParentUserNameByUserId *****");

		userList = userRepository.findParentUserNameByUserId(userId);

		if (Objects.nonNull(userList)) {

			result.put("Status", HttpStatus.OK.toString());
			result.put("USER", userList);
			return new ResponseEntity(result, HttpStatus.OK);
		} else {
			result.put("Status", "404");
			result.put("message", "User Not Found !!");
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(value = { "/findAllSubUserOfSubAdminOfAdminByParentUserId" }, produces = { "application/json" })
	public final ResponseEntity<?> findAllSubUserOfSubAdminOfAdminByParentUserId(
			@RequestParam("parentUserId") Long parentUserId) throws Exception {

		List<UserEntity> userList = null;
		UserEntity uL = null;

		List<User> users = new ArrayList<>();
		HashMap<String, Object> result = new HashMap<>();
		LOGGER.info("***** GETTING USERS BASED ON parentUserId *****");
		User user = null;

		userList = userRepository.findAllSubUserOfSubAdminOfAdminByParentUserId(parentUserId);
		if (userList.size() > 0) {
			/*
			 * if (Objects.nonNull(userList)) { for (int i = 0; i <= 10; i++) { user = new
			 * User(); if (parentUserId > -1L) { uL =
			 * userRepository.findAllSubUserOfSubAdminOfAdminByParentUserId(parentUserId);
			 * if (Objects.nonNull(uL)) { beanUtils.copyProperties(user, uL);
			 * user.setUserPassword("****"); users.add(user); parentUserId = uL.getUserId();
			 * } else { parentUserId = -1L; } } }
			 */
			result.put("Status", HttpStatus.OK.toString());
			result.put("USER", userList);
			return new ResponseEntity(result, HttpStatus.OK);

		} else {
			result.put("Status", "404");
			result.put("message", "User Not Found !!");
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(value = { "/insertBotFromUserBasicToken" }, produces = { "application/json" })
	public final ResponseEntity<List<UserBotMappingEntity>> insertBotFromUserBasicToken() throws Exception {
		List<UserEntity> basicToken = this.userRepository.findAllBotToken();
		for (UserEntity basic : basicToken) {
			UserBotMappingEntity userBot = new UserBotMappingEntity();
			System.out.println("User ::" + basic.getUserName());
			byte[] decodedBytes = Base64.getDecoder().decode(basic.getBotToken().replace("Basic ", ""));
			String decodedString = new String(decodedBytes);

			// Print the result
			System.out.println("Decoded String: " + decodedString);
			String[] ss = decodedString.split(":");
			userBot.setUser_id(basic.getUserId());
			userBot.setBotId(ss[0]);
			userBot.setBasicToken(basic.getBotToken());
			userBot.setBotName(ss[0]);
			userBot.setBotType("PRO");
			userBot.setBrand("FONADA");
			userBot.setOperator("VI");
			userBot.setSecretKey(ss[1]);
			userBotMappingRepository.save(userBot);
			tempalteRepo.updateBotTokenId(userBot.getId(), userBot.getBotId(), userBot.getUser_id());
		}
		return new ResponseEntity(userBotMappingRepository.findAll(), HttpStatus.OK);
	}
}
