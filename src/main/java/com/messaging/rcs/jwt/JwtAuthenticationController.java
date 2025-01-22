package com.messaging.rcs.jwt;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.model.SendOTPResponsePojo;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.util.PasswordUtil;
import com.messaging.rcs.util.SendEmail;

@RestController
@RequestMapping("/api/v1/rcsmessaging/auth")
@CrossOrigin("*")
public class JwtAuthenticationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationController.class);

	
	@Value("${baseUrl.send.Otp.onMobile}")
	private String otpSendApi;
	@Value("${send.Otp.username}")
	private String otpUsername;
	@Value("${send.Otp.password}")
	private String otpPassword;
	@Value("${send.Otp.unicode}")
	private String otpUnicode;
	@Value("${send.Otp.from}")
	private String otpFrom;
	@Value("${send.Otp.dltContentId}")
	private String otpDltContentId;
	
	private static final String STATUS = "Status";
	private static final String MESSAGE = "message";
	public static final String USER = "USER";
	private PasswordUtil pwdUtil = new PasswordUtil();

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private SendEmail sendEmail;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private com.messaging.rcs.repository.UserRoleRepo userRoleRepo;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private CustomUserDetailService userDetailService;

	@PostMapping("/generateToken")
	public   ResponseEntity<?> generate_token(@RequestBody JwtRequest loginUser,@RequestParam("Otp") String otp) throws Exception {
		LOGGER.info("generate-token:=" + loginUser.getUsername());
		
		Map<String, Object>  LoginErrRes = new HashMap<>();


		Optional<UserEntity> user = userRepository.findByUserName(loginUser.getUsername());
		
		if (otp.equals(user.get().getOtp())) {
			LOGGER.info("generate-token:user=" + user.get().getUserName());
			final String token = jwtTokenUtil.generateToken(user.get());

			LOGGER.info("generate-token::Token=" + token);

			ApiResponse<AuthToken> response = new ApiResponse<>(user.get().getAiVideo(),200, userRoleRepo.findById(user.get().getRole_id()).get().getName(), user.get().getUserType(),
					user.get().getPhone(), user.get().getUserId(), user.get().getDailyUsageLimit(), user.get().getBotId(),user.get().getCompanyLogo(),user.get().getCopyRight(),
					new AuthToken(token, user.get().getUserName()));
			LOGGER.info("generate-token:: api response => " + response);

			
			return ResponseEntity.ok(response);
		}
		else {
			LOGGER.info("Otp not matched" + user.get().getOtp());
			
			LoginErrRes.put("Status", "Bad Request");
			LoginErrRes.put("StatusCode", 400);
			LoginErrRes.put("message" , "Otp not matched" );
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginErrRes);
			
		}
		
	}

	@GetMapping(value = "/forgot-password", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> forgotPassword(@RequestParam("userName") String userName,
			@RequestParam("mail") String mail) {
		UserEntity user = null;
		HashMap<String, Object> result = new HashMap<>();

		user = userRepository.findByUserNameAndEmail(userName, mail);
		if (Objects.isNull(user)) {
			result.put(STATUS, HttpStatus.NOT_FOUND.toString());
			result.put(MESSAGE, "User Not Found");
			return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
		} else {
			String pass = pwdUtil.generatePassword(8);
			System.out.println("System Generated Forgot Password Password::=>" + pass);
			user.setUserPassword(bCryptPasswordEncoder.encode(pass));
			sendEmail.sendEmail(user, pass);
			userRepository.save(user);
			result.put(STATUS, HttpStatus.OK.toString());
			result.put(MESSAGE, "Password Sent To Given Mail.");
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
		// Invalidate session
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		// Clear CSRF token cookie
		Cookie csrfCookie = new Cookie("XSRF-TOKEN", null);
		csrfCookie.setMaxAge(0); // Expire the cookie
		csrfCookie.setPath("/"); // Set the cookie path
		csrfCookie.setHttpOnly(true); // Prevent JavaScript access for security
		response.addCookie(csrfCookie);

		// Clear other cookies if necessary
		Cookie authCookie = new Cookie("AuthToken", null);
		authCookie.setMaxAge(0);
		authCookie.setPath("/");
		authCookie.setHttpOnly(true);
		response.addCookie(authCookie);

		return ResponseEntity.ok("Logout successful");
	}
	
	
	
	@PostMapping("/Sentotp")
	public ResponseEntity<?> generate_tokenN(@RequestBody JwtRequest loginUser, HttpServletRequest request) throws Exception {
		
		LOGGER.info("Inside SendOtp Controller !! ");
		
		Map<String, Object>  LoginErrRes = new HashMap<>();
		
		String clientip = GetAccessIP(request).trim();
		
		LOGGER.info("Ip to validate : "+ clientip);

		
		if (clientip.isEmpty()) {
			LoginErrRes.put("statusCode", 400);
			LoginErrRes.put("status", "Bad Request");
			LoginErrRes.put("message", "No Ip Found in X-FORWARDED-FOR ");
			LOGGER.info("No Ip Found in X-FORWARDED-FOR");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginErrRes);
		}
		
		Optional<UserEntity> user = userRepository.findByUserName(loginUser.getUsername());
		
		String registerIpString= user.get().getIpLists();
		
		if (registerIpString.contains(clientip)) {
			LOGGER.info("Valid Ip : "+ clientip);
			
			final CustomUserDetails userDetails = (CustomUserDetails) userDetailService
					.loadUserByUsername(loginUser.getUsername());
			
			
			if (!userDetails.isAccountNonExpired()) {
				LOGGER.info("User Account Expired due to Inactivity");
				LoginErrRes.put("statusCode", 400);
				LoginErrRes.put("status", "Bad Request");
				LoginErrRes.put("message", "User Account Expired due to Inactivity");
				
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginErrRes);

				
			}

			try {
				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
				
				LOGGER.info("Ip is Registed with : "+ user.get().getPhone());
				SendOTPResponsePojo otpRes = sendOTPByMno(user.get().getPhone());
				
				
				LOGGER.info("Otp pojo : "+ otpRes);
				
				if (otpRes!=null) {
					
					
					user.get().setOtp(otpRes.getOtp());
					userRepository.save(user.get());
					LOGGER.info("Otp Send Successfully");
					
					LoginErrRes.put("statusCode", 200);
					LoginErrRes.put("status", "OK");
					LoginErrRes.put("message", "Otp Send Successfully");
					
					return ResponseEntity.status(HttpStatus.OK).body(LoginErrRes);
					
				}
				else {
					LOGGER.info("Otp Send Not Successfull");
					
					LoginErrRes.put("statusCode", 400);
					LoginErrRes.put("status", "OK");
					LoginErrRes.put("message", "Otp Send Failure");
					
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginErrRes);
								
				}
				
				
			} catch (BadCredentialsException e) {
				
				LoginErrRes.put("statusCode", 400);
				LoginErrRes.put("status", "Bad Request");
				LoginErrRes.put("message", "Incorrect username or password");
				LOGGER.info("Incorrect username or password", e);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginErrRes);
			}

		
		}
		else {
			LoginErrRes.put("statusCode", 400);
			LoginErrRes.put("status", "Bad Request");
			LoginErrRes.put("message", "Invalid Ip :"+clientip);
			LOGGER.info("Invalid Ip");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginErrRes);
		}
		
	}

	
	public static String GetAccessIP(HttpServletRequest request) {

		
		String IL_XList = "";
		try {
			IL_XList = request.getHeader("X-FORWARDED-FOR");
		} catch (Exception ex) {
			LOGGER.info("GetAccessIP::Exception=" + ex.getMessage());
		}
		if (IL_XList == null) {
			IL_XList = "";
		}
		String ip = Jsoup.clean(IL_XList, Safelist.none());

		if (ip != null && !ip.trim().isEmpty()) {
		    LOGGER.info("in if: " + ip.length() + ", content: '" + ip + "'");
		    String[] ipRange = ip.split(",");
		    ip = ipRange[0].trim(); // Ensure the first IP is trimmed
		} else {
		    LOGGER.info("in else");
		    ip = request.getRemoteAddr();
		}

		return Jsoup.clean(ip.trim(), Safelist.none());
	}
	
	
	public SendOTPResponsePojo sendOTPByMno(String mobileNo) {
		String output = "";
		Random rnd = new Random();
		String otp = String.valueOf(1000 + rnd.nextInt(9000));
		SendOTPResponsePojo sendOTPResponsePojo = null;
		Map<String, String> map = new HashMap<String, String>();

		try {
			
            RestTemplate restTemplate = createRestTemplate(); // Use custom RestTemplate

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			HttpEntity<?> entity = new HttpEntity<>(headers);
				LOGGER.info(".............Going On To Creating OTP.........");
				String urlTemplate = UriComponentsBuilder.fromHttpUrl(otpSendApi).queryParam("username", otpUsername)
						.queryParam("password", otpPassword).queryParam("unicode", otpUnicode)
						.queryParam("from", otpFrom).queryParam("to", mobileNo)
						.queryParam("dltContentId", otpDltContentId)
						.queryParam("text", URLEncoder.encode("Your FONADA OTP is " + otp + ".", "UTF-8")).encode().toUriString();

				LOGGER.info("Request URL::" + urlTemplate);
				ResponseEntity<SendOTPResponsePojo> response = restTemplate.exchange(urlTemplate, HttpMethod.GET,
						entity, SendOTPResponsePojo.class);

				sendOTPResponsePojo = response.getBody();
				
				if (sendOTPResponsePojo!=null) {
					sendOTPResponsePojo.setOtp(otp);
				}
				
				return sendOTPResponsePojo;
				
		} catch (Exception e) {
			LOGGER.info("Exception in NetClientGet:- " + e);
		}
		return null;

	}
	
	
	   private RestTemplate createRestTemplate() throws Exception {
	        SSLContextBuilder sslContextBuilder = SSLContextBuilder.create()
	                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE);

	        HttpComponentsClientHttpRequestFactory requestFactory =
	                new HttpComponentsClientHttpRequestFactory(
	                        HttpClients.custom()
	                                .setSSLContext(sslContextBuilder.build())
	                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
	                                .build());

	        return new RestTemplate(requestFactory);
	    }

}
