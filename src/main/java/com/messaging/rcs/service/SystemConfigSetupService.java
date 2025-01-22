package com.messaging.rcs.service;

import com.messaging.rcs.domain.MessageTypeEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.model.EmailServerConf;
import com.messaging.rcs.email.repository.EmailServerConfRepository;
import com.messaging.rcs.repository.MessageTypeRepository;
import com.messaging.rcs.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author RahulRajput 26-05-2023
 *
 */

@Component
public class SystemConfigSetupService {
	private static final Logger LOGGER = Logger.getLogger(SystemConfigSetupService.class.getName());
	@Resource
	private UserRepository userRepository;
	@Resource
	private EmailServerConfRepository emailServerConfRepository;
	@Resource
	private MessageTypeRepository messageTypeRepository;

	//@PostConstruct
	public void init() {
		LOGGER.info("Setting up configurations...");
		Optional<UserEntity> optionalUserEntity = userRepository.findByUserName("admin");
		if (!optionalUserEntity.isPresent()) {
			setupAdminUser();
		}
		EmailServerConf email_server_port = emailServerConfRepository.findByEventKey("EMAIL_SERVER_PORT");
		if (email_server_port == null) {
			setupEmailConf();
		}

		List<MessageTypeEntity> all = messageTypeRepository.findAll();
		if (CollectionUtils.isEmpty(all)) {
			setupMessageTypes();
		}
	}

	private void setupMessageTypes() {
		List<MessageTypeEntity> messageTypeEntities = new ArrayList<>();
		MessageTypeEntity messageTypeEntity = new MessageTypeEntity();
		messageTypeEntity.setMessageType("Text");
		messageTypeEntities.add(messageTypeEntity);
		messageTypeEntity = new MessageTypeEntity();
		messageTypeEntity.setMessageType("Media");
		messageTypeEntities.add(messageTypeEntity);
		messageTypeEntity = new MessageTypeEntity();
		messageTypeEntity.setMessageType("Suggested replies");
		messageTypeEntities.add(messageTypeEntity);
		messageTypeEntity = new MessageTypeEntity();
		messageTypeEntity.setMessageType("Rich cards");
		messageTypeEntities.add(messageTypeEntity);
		messageTypeEntity = new MessageTypeEntity();
		messageTypeEntity.setMessageType("Rich card carousels");
		messageTypeEntities.add(messageTypeEntity);
		messageTypeRepository.saveAll(messageTypeEntities);

	}

	private void setupAdminUser() {
		LOGGER.info("Creating admin User");
		UserEntity user = new UserEntity();
		user.setUserName("admin");
		user.setEmail("admin@shivtel.com");
		user.setUserPassword("admin@123");
		user.setFirstName("SYSTEM");
		user.setLastName("User");
		user.setActive("Y");
		user.setCompanyName("Shivtel");
		user.setMultipleLoginAllowed("Y");
		user.setParentUserId(0l);
		user.setIsDeleted(0);
		user.setUserType("1");
		user.setNotificationRequired("N");
		user.setChannelPriorityScheme("RR");
		user.setChannelPrioritySchemeValue("0");
		user.setCreatedBy("admin");
		user.setCreatedDate(new Date());
		userRepository.save(user);
	}

	private void setupEmailConf() {
		List<EmailServerConf> emailServerConfs = new ArrayList<>();
		EmailServerConf emailServerConf = new EmailServerConf();
		emailServerConf.setName("EMAIL_SERVER_PORT");
		emailServerConf.setValue("587");
		emailServerConfs.add(emailServerConf);

		emailServerConf = new EmailServerConf();
		emailServerConf.setName("EMAIL_SERVER_HOST");
		emailServerConf.setValue("mail.fonada.com");
		emailServerConfs.add(emailServerConf);

		emailServerConf = new EmailServerConf();
		emailServerConf.setName("EMAIL_SERVER_USERNAME");
		emailServerConf.setValue("reports@fonada.com");
		emailServerConfs.add(emailServerConf);

		emailServerConf = new EmailServerConf();
		emailServerConf.setName("EMAIL_SERVER_PASSWORD");
		emailServerConf.setValue("Rep0rt$scpl@209");
		emailServerConfs.add(emailServerConf);

		emailServerConfRepository.saveAll(emailServerConfs);
	}
}
