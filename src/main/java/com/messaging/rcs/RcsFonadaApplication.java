package com.messaging.rcs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.messaging.rcs.configuration.HashMaps;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.repository.UserRepository;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableAsync
@EnableSwagger2
@EnableJpaRepositories
@EnableWebMvc
@EnableTransactionManagement
@EnableWebSecurity
@ComponentScan(basePackages = "com.messaging.rcs")
@EnableScheduling
@EnableCaching
public class RcsFonadaApplication implements CommandLineRunner {
	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		// System.setProperty("server.port","8172");
		// System.setProperty("server.connection-timeout","300000");
		// System.setProperty("server.tomcat.max-threads","250");
		SpringApplication.run(RcsFonadaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		List<UserEntity> clientlist = userRepository.findAll();
		for (UserEntity client : clientlist) {
			HashMaps.userEntityHashmap.put(client.getApiKey(), client);
		}
	}
}
