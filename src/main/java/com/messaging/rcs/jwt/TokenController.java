package com.messaging.rcs.jwt;

import static com.messaging.rcs.util.SystemConstants.BASE_URL;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import java.util.Optional;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.repository.UserRepository;

/**
 * 
 * @author Rahul 2023-04-10
 *
 */

@CrossOrigin
@Controller
//@RequestMapping(value = BASE_URL + "/auth", produces = APPLICATION_JSON_VALUE)
public class TokenController {
	private static final Logger LOGGER = Logger.getLogger(TokenController.class.getName());

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtil jwtTokenUtil;
	/*
	 * @Autowired private CustomUserDetailService userDetailService;
	 */ @Autowired
	private TokenService tokenService;
	@Autowired
	private com.messaging.rcs.repository.UserRoleRepo userRoleRepo;
	@Autowired
	private UserRepository userRepository;

	@PostMapping("/generateToken")
	public ResponseEntity<?> generateToken(@RequestBody AuthenticationRequest loginUser, HttpServletRequest request)
			throws AuthenticationException {
		LOGGER.info("generate-token:=" + loginUser.getUsername());
		// user lUser = new user();
		// Allow subclasses to set the "details" property

		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
		LOGGER.info("authenticate=");
		final Optional<UserEntity> user = userRepository.findByUserName(loginUser.getUsername());
		// final user_admin user =
		// user_service.CheckUserFromDatabase(loginUser.getUsername(),Dkey);
		LOGGER.info("generate-token:user=" + user.get().getUserName());

		// lUser.setUsername(loginUser.getUsername());
		// lUser.setPassword(loginUser.getPassword());
		final String token = jwtTokenUtil.generateToken(user.get().getUserName());

		LOGGER.info("generate-token::Token=" + token);

		ApiResponse<AuthToken> response = new ApiResponse<>(user.get().getAiVideo(),200,
				userRoleRepo.findById(user.get().getRole_id()).get().getName(), user.get().getUserType(),
				user.get().getPhone(), user.get().getUserId(), user.get().getDailyUsageLimit(), user.get().getBotId(),
				user.get().getCompanyLogo(), user.get().getCopyRight(), new AuthToken(token, user.get().getUserName()));
		LOGGER.info("generate-token::=> " + response);
		return ResponseEntity.ok(response);
	}

	/*
	 * @RequestMapping(value = "/token", method = POST) public ResponseEntity<?>
	 * createAuthenticationToken(@RequestBody AuthenticationRequest
	 * authenticationRequest) throws Exception { final CustomUserDetails userDetails
	 * = (CustomUserDetails) userDetailService
	 * .loadUserByUsername(authenticationRequest.getUsername()); if
	 * (!userDetails.isAccountNonExpired()) { throw new
	 * RuntimeException("User Account Expired due to Inactivity"); } try {
	 * authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
	 * authenticationRequest.getUsername(), authenticationRequest.getPassword())); }
	 * catch (BadCredentialsException e) { throw new
	 * Exception("Incorrect username or password", e); }
	 * 
	 * String jwt = null; if (userDetails.isMultipleLoginAllowed()) { jwt =
	 * tokenService.getToken(userDetails.getUsername());
	 * 
	 * if (jwt == null || !StringUtils.hasLength(jwt)) { // jwt =
	 * jwtTokenUtil.generateToken(userDetails);
	 * tokenService.saveToken(userDetails.getUsername(), jwt); } try {
	 * jwtTokenUtil.validateToken(jwt, userDetails); LOGGER.info("Valid Token for "
	 * + userDetails.getUsername());
	 * 
	 * } catch (Exception e) { LOGGER.info("Expired Token for " +
	 * userDetails.getUsername() + " regenrating");
	 * tokenService.removeToken(userDetails.getUsername()); // jwt =
	 * jwtTokenUtil.generateToken(userDetails);
	 * tokenService.saveToken(userDetails.getUsername(), jwt); } } else {
	 * tokenService.removeToken(userDetails.getUsername()); // jwt =
	 * jwtTokenUtil.generateToken(userDetails);
	 * tokenService.saveToken(userDetails.getUsername(), jwt); } return
	 * ResponseEntity.ok(new AuthenticationResponse(jwt)); }
	 */
	@PostMapping(value = "/logout")
	public ResponseEntity<?> logout(@RequestBody AuthenticationRequest authenticationRequest) {
		tokenService.removeToken(authenticationRequest.getUsername());
		SecurityContextHolder.clearContext();
		return ResponseEntity.ok("Success");
	}
}
