package com.messaging.rcs.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UserType;
import com.messaging.rcs.domain.UserTypeRepository;

@Service
public class UserTypeService {

	@Autowired
	private UserTypeRepository userTypeRepository;

//	@PostConstruct
	public void saveUserType() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String date = sdf.format(new Date());

		UserType userT1 = new UserType();
		userT1.setCreatedBy("System");
		userT1.setCreatedDate(date);
		userT1.setLastModifiedBy("System");
		userT1.setLastModifiedDate(date);
		userT1.setType("OTP");
		userTypeRepository.save(userT1);

		UserType userT2 = new UserType();
		userT2.setCreatedBy("System");
		userT2.setCreatedDate(date);
		userT2.setLastModifiedBy("System");
		userT2.setLastModifiedDate(date);
		userT2.setType("PRO");
		userTypeRepository.save(userT2);

		UserType userT3 = new UserType();
		userT3.setCreatedBy("System");
		userT3.setCreatedDate(date);
		userT3.setLastModifiedBy("System");
		userT3.setLastModifiedDate(date);
		userT3.setType("TRANS");
		userTypeRepository.save(userT3);
	}
}
