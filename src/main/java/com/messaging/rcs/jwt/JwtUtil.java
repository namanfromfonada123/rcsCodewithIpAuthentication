package com.messaging.rcs.jwt;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtUtil {

	private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());
	public static final long ACCESS_TOKEN_VALIDITY_SECONDS = 5 * 60 * 60;
	public static final String SIGNING_KEY = "timesmobile@123";

	@Resource
	private TokenRepository tokenRepository;

	private static final String SECRET_KEY = "lWbF6jNkG8PE0AUP";

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public String generateToken(UserEntity userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userDetails.getUserName());
	}

	public String generateTokenForPasswordReset(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return createTokenSmallExpiry(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
				.signWith(SignatureAlgorithm.HS256, SIGNING_KEY).compact();
	}

	public String generateToken(String userName) {
		return doGenerateToken(userName);
	}

	private String doGenerateToken(String subject) {

		Claims claims = Jwts.claims().setSubject(subject);
		claims.put("scopes", Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));

		return Jwts.builder().setClaims(claims).setIssuer("http://timesinternet.in")
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
				.signWith(SignatureAlgorithm.HS256, SIGNING_KEY).compact();
	}

	private String createTokenSmallExpiry(Map<String, Object> claims, String subject) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
	}

	private String createTokenForPB(Map<String, Object> claims, String subject, final String secretKey) {
		Header header = Jwts.header();
		header.setType("JWT");
		String jwt = null;
		try {
			jwt = Jwts.builder().setHeader((Map<String, Object>) header).setClaims(claims).setSubject(subject)
					.setIssuedAt(new Date(System.currentTimeMillis()))
					.signWith(SignatureAlgorithm.HS256, secretKey.getBytes("UTF-8")).compact();
		} catch (Exception e) {
			LOGGER.error("Got Exception createTokenForPB - ", e);
		}
		return jwt;
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		boolean validToken = false;
		TokenEntity tokenEntity = tokenRepository.getTokenStoreByUserNameAndIsActive(username, "Y");
		if (tokenEntity != null && tokenEntity.getJwt().equals(token)) {
			validToken = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
		}

		return validToken;
	}
}