package com.messaging.rcs.email.service;


import com.messaging.rcs.email.model.EmailRequestModel;

import java.util.Map;

public interface EmailService {
	
	boolean sendEmail(Map<String, String> valueForReplace, EmailRequestModel emailRequestModel);
	boolean sendEmail(Map<String, String> valueForReplace, EmailRequestModel emailRequestModel, String attachment);
	boolean testEmail(Map<String, String> valueForReplace, EmailRequestModel emailRequestModel, String attachment);
}
