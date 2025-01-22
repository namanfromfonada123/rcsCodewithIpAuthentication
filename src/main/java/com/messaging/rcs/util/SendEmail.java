package com.messaging.rcs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UserEntity;

/**
 * 
 * @author RahulRajput 19-07-2023
 *
 */
@Service
public class SendEmail {

	@Value("${spring.mail.username}")
	String userMail;
	@Autowired
	public JavaMailSender emailSender;

	public static final Logger Logger = LoggerFactory.getLogger(SendEmail.class);

	public String sendEmail(UserEntity user, String password) {
		Logger.info("***** Forgot-Password Email is Sending ******");
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(userMail);
		msg.setTo(user.getEmail());

		msg.setSubject("Forgot Password By " + user.getUserName());
		msg.setText("Dear User,\n \n Here is your Password :" + password
				+ "\n \nKindly reset your new password using Forgot-Password.\nStep1::First Copy Password From Mail.\nStep2::Login Panel By Using Mail Password.\nStep3::After Login Change Password Button Available.\nStep4:: Click On Change Password.\nStep5::Now Enter Mail Password Inside Old Password.\nStep::Enter New Password.\nStep::Click On Submit. \n Thanks,\n Mverify Support.");

		emailSender.send(msg);
		return "Success";

	}

}
