package com.messaging.rcs.email.service;

import com.messaging.rcs.email.model.EmailRequestModel;
import com.messaging.rcs.email.model.EmailTemplate;
import com.messaging.rcs.email.repository.EmailRepository;
import com.messaging.rcs.email.repository.EmailServerConfRepository;
import com.messaging.rcs.service.SystemConfigSetupService;
import com.sun.mail.smtp.SMTPTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(EmailServiceImpl.class.getName());
	private static final String ATTACHMENT_PATH = "/var/www/html/report/";

	@Autowired
	EmailRepository emailRepository;

	@Autowired
	EmailServerConfRepository emailServerConfRepository;
	@Autowired
	SystemConfigSetupService systemConfigSetupService;

	@Value("${ssl.enabled.email:true}")
	private String sslEnabledForEmail;
	String emailPort;
	String emailHost;
	String emailUsername;
	String EmailPassword;

	// @PostConstruct
	private void init() {
		emailPort = emailServerConfRepository.findByEventKey("EMAIL_SERVER_PORT").getValue();
		emailHost = emailServerConfRepository.findByEventKey("EMAIL_SERVER_HOST").getValue();
		emailUsername = emailServerConfRepository.findByEventKey("EMAIL_SERVER_USERNAME").getValue();
		EmailPassword = emailServerConfRepository.findByEventKey("EMAIL_SERVER_PASSWORD").getValue();
		logger.info("Loaded Email Server Conf..");
	}

	@Override
	public boolean sendEmail(Map<String, String> valueForReplace, EmailRequestModel emailRequestModel) {

		boolean ret = false;
		logger.info("Send Email : " + emailRequestModel);
		try {
			EmailTemplate emailTemplate = emailRepository.findByEventKey(emailRequestModel.getEventType());
			if (emailTemplate == null) {
				logger.warn("Template not found for Event Type " + emailRequestModel.getEventType());
				return false;
			} else {
				logger.info("emailTemplate" + emailTemplate);
			}

			String emailBody = null;
			// Replace dynamic body
			for (Entry<String, String> entry : valueForReplace.entrySet()) {
				if (entry == null || entry.getValue() == null) {
					continue;
				}
				if (emailBody == null) {
					if (emailTemplate.getEmailBody() != null) {
//                        logger.info("entry.getKey():"+entry.getKey()+" and entry.getValue():"+entry.getValue());
						emailBody = emailTemplate.getEmailBody().replaceAll("#" + entry.getKey(),
								entry.getValue().replaceAll("\\$", ""));
					} else {
						logger.error("No Body Found for Email");
					}
				} else {
					emailBody = emailBody != null
							? emailBody.replaceAll("#" + entry.getKey(), entry.getValue().replaceAll("\\$", ""))
							: emailBody;
				}
			}

			Properties prop = System.getProperties();
			prop.put("mail.smtp.auth", "true");
			prop.put("mail.smtp.port", emailPort);
			prop.put("mail.smtp.starttls.enable", "true");
			Session session = Session.getInstance(prop, null);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailTemplate.getFromEmail(), "Shivtel"));
			String toEmail = null;
			if (StringUtils.hasLength(emailRequestModel.getEmailTo())) {
				toEmail = emailRequestModel.getEmailTo();
			} else {
				toEmail = emailTemplate.getToEmail();
			}

			if (!StringUtils.hasLength(toEmail)) {
				logger.error("No Email configured to send email.... Quitting.." + emailTemplate);
				return false;
			}
			String toMail = null;
			if (StringUtils.hasLength(emailRequestModel.getEmailTo())) {
				toMail = emailRequestModel.getEmailTo();
			} else {
				toMail = emailTemplate.getToEmail();
			}

			if (!StringUtils.hasLength(toEmail)) {
				logger.error("No Email configured to send email.... Quitting.." + emailTemplate);
				return false;
			}

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toMail, false));

			if (emailRequestModel.getEmailCC() != null && !emailRequestModel.getEmailCC().isEmpty()) {
				message.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(emailRequestModel.getEmailCC(), false));

			}
			message.setSubject(emailTemplate.getSubject());
			logger.info("Email Subject : " + emailTemplate.getSubject());
			// logger.info("Email Body : "+emailBody);

			// HTML email
			// SSLUtil.turnOffSslChecking();
			message.setDataHandler(new DataHandler(new HTMLDataSource(emailBody)));

			SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
			if ("FALSE".equalsIgnoreCase(sslEnabledForEmail)) {
				t.setStartTLS(false);
			}

			// connect
			t.connect(emailHost, emailUsername, EmailPassword);

			// send Email
			t.sendMessage(message, message.getAllRecipients());

			if (t.isConnected()) {
				t.close();
				ret = true;
			} else {
				t.close();
				ret = false;
			}

		} catch (Exception e) {
			logger.error("Got Exception while sending email ", e);
		}

		return ret;
	}

	@Override
	public boolean sendEmail(Map<String, String> valueForReplace, EmailRequestModel emailRequestModel,
			String attachment) {
		if (!StringUtils.hasLength(attachment)) {
			return sendEmail(valueForReplace, emailRequestModel);
		}
		boolean ret = false;
		logger.info("DataExportService: Send Email : " + emailRequestModel);
		try {
			EmailTemplate emailTemplate = emailRepository.findByEventKey(emailRequestModel.getEventType());
			if (emailTemplate == null) {
				logger.warn("Template not found for Event Type " + emailRequestModel.getEventType());
				return false;
			} else {
				logger.info("emailTemplate:" + emailTemplate);
			}

			String emailBody = null;
			// Replace dynamic body
			for (Entry<String, String> entry : valueForReplace.entrySet()) {
				if (entry == null || entry.getValue() == null) {
					continue;
				}
				if (emailBody == null) {
					if (emailTemplate.getEmailBody() != null) {
						logger.info("entry.getKey():" + entry.getKey() + " and entry.getValue():" + entry.getValue());
						emailBody = emailTemplate.getEmailBody().replaceAll("#" + entry.getKey(), entry.getValue());
					} else {
						logger.error("No Body Found for Email");
					}
				} else {
					emailBody = emailBody != null ? emailBody.replaceAll("#" + entry.getKey(), entry.getValue())
							: emailBody;
				}
			}

			Properties prop = System.getProperties();
			prop.put("mail.smtp.auth", "true");
			prop.put("mail.smtp.port", emailPort);
			prop.put("mail.smtp.starttls.enable", "true");
			Session session = Session.getInstance(prop, null);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailTemplate.getFromEmail(), "Shivtel"));

			String[] recipientList = null;
			if (StringUtils.hasLength(emailRequestModel.getEmailTo())) {
				if (emailRequestModel.getEmailTo().indexOf(",") > 0) {
					recipientList = emailRequestModel.getEmailTo().split(",");
				} else {
					recipientList = new String[] { emailRequestModel.getEmailTo() };
				}
			} else if (StringUtils.hasLength(emailTemplate.getToEmail())) {
				if (emailTemplate.getToEmail().indexOf(",") > 0) {
					recipientList = emailTemplate.getToEmail().split(",");
				} else {
					recipientList = new String[] { emailTemplate.getToEmail().trim() };
				}
			} else {
				logger.error("No Recipients found !!");
				return false;
			}
			InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
			int counter = 0;
			for (String recipient : recipientList) {
				recipientAddress[counter] = new InternetAddress(recipient.trim());
				counter++;
			}
			message.setRecipients(Message.RecipientType.TO, recipientAddress);

			if (StringUtils.hasLength(emailRequestModel.getEmailCC())) {
				String[] recipientListCC = null;
				if (emailRequestModel.getEmailCC().indexOf(",") > 0) {
					recipientListCC = emailRequestModel.getEmailCC().split(",");
				} else {
					recipientListCC = new String[] { emailRequestModel.getEmailCC() };
				}
				InternetAddress[] recipientAddressCC = new InternetAddress[recipientListCC.length];
				counter = 0;
				for (String recipient : recipientListCC) {
					recipientAddressCC[counter] = new InternetAddress(recipient.trim());
					counter++;
				}
				message.setRecipients(Message.RecipientType.CC, recipientAddressCC);
			}

			// Create the message part
			MimeBodyPart attachmentPart = new MimeBodyPart();

			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setText(emailBody);
			bodyPart.setContent(emailBody, "text/html");

			// Create a multipar message
			MimeMultipart multipart = new MimeMultipart();

			// Set text message part

			// Part two is attachment
			String path = ATTACHMENT_PATH + "/";
			logger.info("DataExportService: Attachment:" + path + attachment);
			String attachmentFullPath = (path + attachment);
			DataSource source = new FileDataSource(attachmentFullPath);
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName(attachment);
			multipart.addBodyPart(bodyPart);
			multipart.addBodyPart(attachmentPart);
			// Send the complete message parts
			message.setContent(multipart);

			message.setSubject(emailTemplate.getSubject());
			logger.info("DataExportService:Email Subject : " + emailTemplate.getSubject());
			logger.info("DataExportService:Email Body : " + emailBody);

			// HTML email
			// SSLUtil.turnOffSslChecking();
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			if ("FALSE".equalsIgnoreCase(sslEnabledForEmail)) {
				transport.setStartTLS(false);
			}
			transport.connect(emailHost, emailUsername, EmailPassword);

			// send Email
			transport.sendMessage(message, message.getAllRecipients());

			if (transport.isConnected()) {
				transport.close();
				ret = true;
			} else {
				transport.close();
				ret = false;
			}

		} catch (Exception e) {
			logger.error("Got Exception while sending email ", e);
		}

		return ret;

	}

	@Override
	public boolean testEmail(Map<String, String> valueForReplace, EmailRequestModel emailRequestModel,
			String attachment) {
		boolean ret = false;
		logger.info("DataExportService: Send Email : " + emailRequestModel);
		try {
			EmailTemplate emailTemplate = new EmailTemplate();
			emailTemplate.setEmailBody(
					"Hi #username <br /> Please see attached Campaign Header Summary <br /><br /> Regards,<br />Team Testing");
			emailTemplate.setEventKey("CampaignHeader");
			emailTemplate.setSubject("Campaign Header Summary");
			if (emailTemplate == null) {
				logger.warn("DataExportService: Template not found for Event Type " + emailRequestModel.getEventType());
				return false;
			} else {
				logger.info("DataExportService: emailTemplate" + emailTemplate);
			}

//            String emailPort = "465";
//            String emailHost = "cs2000.hostgator.in";
//            String emailUsername = "dinesh@shivtel.com";
//            String EmailPassword = "Dky@scpl@805";

			String emailPort = "587";
			String emailHost = "smtp.gmail.com";
			String emailUsername = "info@qverifier.com";
			String EmailPassword = "Qverifier@123";

			String emailBody = null;
			// Replace dynamic body
			for (Entry<String, String> entry : valueForReplace.entrySet()) {
				if (entry == null || entry.getValue() == null) {
					continue;
				}
				if (emailBody == null) {
					if (emailTemplate.getEmailBody() != null) {
						logger.info("entry.getKey():" + entry.getKey() + " and entry.getValue():" + entry.getValue());
						emailBody = emailTemplate.getEmailBody().replaceAll("#" + entry.getKey(), entry.getValue());
					} else {
						logger.error("No Body Found for Email");
					}
				} else {
					emailBody = emailBody != null ? emailBody.replaceAll("#" + entry.getKey(), entry.getValue())
							: emailBody;
				}
			}

			Properties prop = System.getProperties();
			prop.put("mail.smtp.auth", "true");
			prop.put("mail.smtp.port", emailPort);
			prop.put("mail.smtp.starttls.enable", "true");
			Session session = Session.getInstance(prop, null);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailTemplate.getFromEmail(), "Saurabh"));
			String[] recipientList = emailRequestModel.getEmailTo().split(",");
			InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
			int counter = 0;
			for (String recipient : recipientList) {
				recipientAddress[counter] = new InternetAddress(recipient.trim());
				counter++;
			}
			message.setRecipients(Message.RecipientType.TO, recipientAddress);

			if (StringUtils.hasLength(emailRequestModel.getEmailCC())) {
				String[] recipientListCC = emailRequestModel.getEmailCC().split(",");
				InternetAddress[] recipientAddressCC = new InternetAddress[recipientListCC.length];
				counter = 0;
				for (String recipient : recipientList) {
					recipientAddressCC[counter] = new InternetAddress(recipient.trim());
					counter++;
				}
				message.setRecipients(Message.RecipientType.CC, recipientAddressCC);
			}

			// Create the message part
			MimeBodyPart attachmentPart = new MimeBodyPart();

			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setText(emailBody);
			bodyPart.setContent(emailBody, "text/html");

			// Create a multipar message
			MimeMultipart multipart = new MimeMultipart();

			// Set text message part

			// Part two is attachment
			Date now = new Date();
			DateFormat df = new SimpleDateFormat("yyy-MM-dd");
			String path = ATTACHMENT_PATH + "/" + df.format(now) + "/";
			logger.info("DataExportService: Attachment:" + path + attachment);
			String attachmentFullPath = (path + attachment);
			DataSource source = new FileDataSource(attachmentFullPath);
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName(attachment);
			multipart.addBodyPart(bodyPart);
			multipart.addBodyPart(attachmentPart);
			// Send the complete message parts
			message.setContent(multipart);

			message.setSubject(emailTemplate.getSubject());
			logger.info("DataExportService:Email Subject : " + emailTemplate.getSubject());
			logger.info("DataExportService:Email Body : " + emailBody);

			// HTML email

			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");

			transport.connect(emailHost, emailUsername, EmailPassword);

			// send Email
			transport.sendMessage(message, message.getAllRecipients());

			if (transport.isConnected()) {
				transport.close();
				ret = true;
			} else {
				transport.close();
				ret = false;
			}

		} catch (Exception e) {
			logger.error("Got Exception while sending email ", e);
		}

		return ret;

	}

	static class HTMLDataSource implements DataSource {

		private String html;

		public HTMLDataSource(String htmlString) {
			html = htmlString;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (html == null) {
				throw new IOException("html message is null!");
			}
			return new ByteArrayInputStream(html.getBytes());
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new IOException("This DataHandler cannot write HTML");
		}

		@Override
		public String getContentType() {
			return "text/html";
		}

		@Override
		public String getName() {
			return "HTMLDataSource";
		}
	}
}
