package com.messaging.rcs.consumer;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.email.model.WebEnagage;
import com.messaging.rcs.model.ClientLeadCreatedPojo;
import com.messaging.rcs.model.SmsApiWebRequest;

@Service
public class RabbitMQSenderTelco {

	private static final Logger LOGGER = Logger.getLogger(RabbitMQSenderTelco.class.getName());

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Value("${jsa.rabbitmq.telco.exchange}")
	private String exchange;

	@Value("${javainuse.rabbitmq.telco.routingkey}")
	private String routingkey;
	@Autowired
	QueueName queuename;

	public String sendLeadId(Integer leadId, Long rcsMsgTypeId) throws Exception {
		if (rcsMsgTypeId == 1) {
			LOGGER.info("GOING ON CREATING QUEUE SEND LEAD_ID TO evt.remove.queue ::=>" + leadId);

			amqpTemplate.convertAndSend("", "evt.remove.queue", leadId);
		} else if (rcsMsgTypeId == 3) {
			LOGGER.info("GOING ON CREATING QUEUE SEND LEAD_ID TO evt.remove.queue And evt.remove.sms::=>" + leadId);

			amqpTemplate.convertAndSend("", "evt.remove.queue", leadId);

			amqpTemplate.convertAndSend("", "evt.remove.sms", leadId);
		} else {
			LOGGER.info("GOING ON CREATING QUEUE SEND LEAD_ID TO evt.remove.sms::=>" + leadId);

			amqpTemplate.convertAndSend("", "evt.remove.sms", leadId);
		}
		return "DONE";
	}

	public String sendInfoDetails(WebRequest webRequest, String leadId) throws Exception {
		amqpTemplate.convertAndSend(queuename.buildForExchange(leadId), queuename.buildForRouting(leadId), webRequest);
		LOGGER.info("SEND LEAD_INFO_DETAILS TO " + queuename.buildForExchange(leadId) + "And Request:: "
				+ webRequest.toString());
		return "DONE";
	}

	public String sendToWebEngageQueue(ClientLeadCreatedPojo leadInfo) throws Exception {
		amqpTemplate.convertAndSend("WebEngage.exchange", "WebEngage.routing", leadInfo);
		LOGGER.info("SEND LEAD_INFO_DETAILS TO WebEngage.exchange" + leadInfo.toString());
		return "DONE";
	}

	public String sendToBatchRecordToUserNameQueue(UserEntity userEntity, WebEnagage leadInfoDetails) throws Exception {
		amqpTemplate.convertAndSend(queuename.buildForExchange(userEntity.getUserName()),
				queuename.buildForRouting(userEntity.getUserName()), leadInfoDetails);
		LOGGER.info("QUEUE By API For NON_Rcs_Check SEND LEAD_INFO_DETAILS TO telco." + userEntity.getUserName()
				+ ".queue  Size ::" + leadInfoDetails.getLeadInfoDetailEntities().toString());
		return "DONE";
	}

	public String smsSendToBatchRecordToUserNameQueue(UserEntity userEntity, WebEnagage leadInfoDetails)
			throws Exception {
		amqpTemplate.convertAndSend("sms." + userEntity.getUserName() + ".exchange",
				"sms." + userEntity.getUserName() + ".routing", leadInfoDetails);
		LOGGER.info("SMS Record To Send SEND LEAD_INFO_DETAILS TO sms." + userEntity.getUserName() + ".queue  :: "
				+ leadInfoDetails.getLeadInfoDetailEntities().size());
		return "DONE";
	}

	public String sendToSmsApiQueue(SmsApiWebRequest smsApiWebRequest) throws Exception {
		amqpTemplate.convertAndSend("RelianceSmsQueue.exchange", "RelianceSmsQueue.routing", smsApiWebRequest);
		LOGGER.info("SEND LEAD_INFO_DETAILS TO RelianceSmsQueue.exchange" + smsApiWebRequest.toString());
		return "DONE";
	}
}