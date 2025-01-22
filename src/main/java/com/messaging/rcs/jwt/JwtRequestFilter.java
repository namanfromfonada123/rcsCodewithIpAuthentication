package com.messaging.rcs.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.messaging.rcs.service.UserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

	@Autowired
	private CustomUserDetailService userDetailsService;

	@Autowired
	private JwtTokenUtil jwtUtil;

	@Autowired
	private UserService userService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IllegalArgumentException, ServletException, ExpiredJwtException, MalformedJwtException,
			SignatureException, IOException {

		final String authorizationHeader = request.getHeader("Authorization");

		String username = null;
		String jwt = null;
		try {
			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				jwt = authorizationHeader.substring(7);

				username = jwtUtil.getUsernameFromToken(jwt);

			} // Skip token validation for whitelisted endpoints
			String requestURI = request.getRequestURI();
			if (requestURI.contains("/generateToken")) {
				chain.doFilter(request, response);
				return;
			}

			// List of public APIs that don't require authorization
		    List<String> publicApis = Arrays.asList(
		        "/api/v1/rcsmessaging/lead/generateLead",
		        "/api/v1/rcsmessaging/lead/rcsSmsApi",
		        "/api/v1/rcsmessaging/lead/rcsLeadGeneratedApi",
		        "/api/v1/rcsmessaging/demoRcs/callBackReport/findRcsCallBackMsgStatusFromDemoRcs",  // Wildcard for matching multiple paths
		        "/api/v1/rcsmessaging/auth/generateToken",  // Example public API
		        "/api/v1/rcsmessaging/auth/forgot-password",
		        "/api/v1/rcsmessaging/lead/getUserAndBotIdByMnoForDemoRcsService",
		        "/api/v1/rcsmessaging/auth/Sentotp"
		        // Add more public API paths dynamically here
		    );

		    // Check if the request URI is one of the public API paths
		    for (String publicApi : publicApis) {
		        if (requestURI.matches(publicApi.replace("**", ".*"))) {
		            chain.doFilter(request, response);
		            return;
		        }
		    }
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				CustomUserDetails userDetails = (CustomUserDetails) this.userDetailsService
						.loadUserByUsername(username);
				if (!userDetails.isAccountNonExpired()) {
					throw new RuntimeException("User Account Expired due to Inactivity");
				}

				if (Boolean.TRUE.equals(jwtUtil.validateToken(jwt, userDetails))) {

					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				} else {
					LOGGER.warn("Invalid token provided for username={}", username);
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
					return;
				}
			} else if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				LOGGER.warn("Missing or invalid Authorization header");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header missing or invalid");
				return;
			}
		} catch (ExpiredJwtException e) {
			LOGGER.error("JWT token expired", e);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Expired");
			return;
		} catch (Exception e) {
			LOGGER.error("Authentication error", e);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return;
		}

		chain.doFilter(request, response);
	}

	private boolean validUserId(HttpServletRequest request, CustomUserDetails userDetails) {
		boolean validUserId = true;
		if ("GET".equals(request.getMethod()) || "DELETE".equalsIgnoreCase(request.getMethod())) {
			String queryString = request.getQueryString();
			if (hasValue(queryString, "userId")) {
				Long userId = Long.valueOf(queryString.split("userId=")[1].split("&")[0]);
				if (userId.longValue() != userDetails.getUserId().longValue()
						&& !userService.isUserIdFromBodyChildOfLoggedInUser(userDetails.getUserId(), userId)) {
					throw new UsernameNotFoundException("" + userId);
				}
			} else if (hasValue(queryString, "parentUserId")) {
				Long userId = Long.valueOf(queryString.split("parentUserId=")[1].split("&")[0]);
				if (userId.longValue() != userDetails.getUserId().longValue()
						&& !userService.isUserIdFromBodyChildOfLoggedInUser(userDetails.getUserId(), userId)) {
					throw new UsernameNotFoundException("" + userId);
				}
			} else if (hasValue(queryString, "adminUserId")) {
				Long userId = Long.valueOf(queryString.split("adminUserId=")[1].split("&")[0]);
				if (userId.longValue() != userDetails.getUserId().longValue()
						&& !userService.isUserIdFromBodyChildOfLoggedInUser(userDetails.getUserId(), userId)) {
					throw new UsernameNotFoundException("" + userId);
				}
			} else if (request.getRequestURI() != null && request.getRequestURI().contains("user/")
					&& !request.getRequestURI().contains("/user/all")) {
				Long userId = Long
						.valueOf(request.getRequestURI().toLowerCase().split("user/")[1].split("/")[0].split("%")[0]);
				if (userId.longValue() != userDetails.getUserId().longValue()
						&& !userService.isUserIdFromBodyChildOfLoggedInUser(userDetails.getUserId(), userId)) {
					throw new UsernameNotFoundException("" + userId);
				}
			}
		}
		return validUserId;
	}

	/**
	 * extracts digit next to userId String example userId=12, this will check if
	 * there is some value after =
	 * 
	 * @param queryString
	 * @param userId
	 * @return
	 */
	private boolean hasValue(String queryString, String userId) {

		return StringUtils.hasLength(queryString) && queryString.contains(userId)
				&& StringUtils.hasLength(queryString.split(userId + "=")[1])
				&& !queryString.split(userId + "=")[1].startsWith("&");
	}
}
