
package com.messaging.rcs.jwt;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 
 * @author RahulRajput
 *
 */
@Configuration
@EnableWebSecurity
@Component("com.messaging.rcs")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;
	@Autowired
	CustomUserDetailService userDetailsService;

	@Autowired
	JwtRequestFilter jwtRequestFilter;

	/*
	 * @Value("${com.allowed.origins:*}") private String allowedOrigins;
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.userDetailsService(userDetailsService);
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {

		httpSecurity.csrf().disable().cors().and().anonymous().and().authorizeRequests()
				.antMatchers("/api/v1/rcsmessaging/auth/generateToken").permitAll()
				.antMatchers("/api/v1/rcsmessaging/auth/Sentotp").permitAll()
				.antMatchers("/api/v1/rcsmessaging/lead/generateLead").permitAll()
				.antMatchers("/api/v1/rcsmessaging/lead/rcsSmsApi").permitAll()
				.antMatchers("/api/v1/rcsmessaging/lead/rcsLeadGeneratedApi").permitAll()
				.antMatchers("/api/v1/rcsmessaging/demoRcs/callBackReport/findRcsCallBackMsgStatusFromDemoRcs")
				.permitAll().antMatchers("/api/v1/rcsmessaging/lead/**").permitAll()// allow CORS option // // calls
				.antMatchers("/api/v1/rcsmessaging/versionInfo").permitAll()
				.antMatchers("/api/v1/rcsmessaging/report/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/sms/clickUrl/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/auth/forgot-password").permitAll()
				.antMatchers("/api/v1/rcsmessaging/user/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/multipleUserBot/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/campaign/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/template/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/blacklist/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/sms/url**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/addMessage/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/credit/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/rcsMsgType/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/operator/**").permitAll()
				.antMatchers("/api/v1/rcsmessaging/lead/getUserAndBotIdByMnoForDemoRcsService").permitAll().anyRequest()
				.authenticated().and().exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		// httpSecurity.headers().frameOptions().disable();

	}

	@Override

	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Autowired
	public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(encoder());
	}

	@Bean
	public BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	/*
	 * @Bean public NoOpPasswordEncoder passwordEncoder(){ return
	 * (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance(); }
	 */

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(
				Arrays.asList("http://localhost:4200", "*", "http://fuat.flash49.com", "https://app.flash49.com"));
		configuration.setAllowedMethods(Arrays.asList("OPTIONS", "DELETE", "GET", "POST"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
