/*
Copyright 2018 Google Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.messaging.rcs.rbm;

// [END import_libraries]
import static com.messaging.rcs.util.SystemConstants.DELIVERED;
import static com.messaging.rcs.util.SystemConstants.STOP;
import static com.messaging.rcs.util.SystemConstants.TEXT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.rcsbusinessmessaging.v1.RCSBusinessMessaging;
import com.google.api.services.rcsbusinessmessaging.v1.StandaloneCardHelper;
import com.google.api.services.rcsbusinessmessaging.v1.SuggestionHelper;
import com.google.api.services.rcsbusinessmessaging.v1.model.AgentContentMessage;
import com.google.api.services.rcsbusinessmessaging.v1.model.AgentMessage;
import com.google.api.services.rcsbusinessmessaging.v1.model.ContentInfo;
import com.google.api.services.rcsbusinessmessaging.v1.model.CreateCalendarEventAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.DialAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.LatLng;
import com.google.api.services.rcsbusinessmessaging.v1.model.OpenUrlAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.ShareLocationAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.StandaloneCard;
import com.google.api.services.rcsbusinessmessaging.v1.model.SuggestedAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.Suggestion;
import com.google.api.services.rcsbusinessmessaging.v1.model.ViewLocationAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.cards.CardOrientation;
import com.google.api.services.rcsbusinessmessaging.v1.model.cards.MediaHeight;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.messaging.rcs.domain.BlackListEntity;
import com.messaging.rcs.domain.LeadInfoDetailEntity;
import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.domain.MessageResponseLogs;
import com.messaging.rcs.rbm.lib.RbmApiHelper;
import com.messaging.rcs.rbm.messages.MyCreateCalendarEventAction;
import com.messaging.rcs.rbm.messages.MySuggestedAction;
import com.messaging.rcs.rbm.messages.MySuggestion;
import com.messaging.rcs.rbm.messages.MyViewLocationAction;
import com.messaging.rcs.rbm.messages.RichCards;
import com.messaging.rcs.rbm.messages.SuggestedReplies;
import com.messaging.rcs.rbm.model.PubSubDelieveryResponse;
import com.messaging.rcs.rbm.model.PubSubReplyResponse;
import com.messaging.rcs.repository.BlackListRepository;
import com.messaging.rcs.repository.LeadInfoDetailRepository;
import com.messaging.rcs.repository.LeadInfoRepository;
import com.messaging.rcs.repository.MessageResponseLogsRepository;

/**
 * RCS Business Messaging sample first agent.
 * <p>
 * Sends the following message to a user: "What is your favorite color?" Parses
 * the user's response and echos it in a new message.
 */

public class MessagingAgent {

	@Value("${rcsMsgCredentials.file.path}")
	String rcsMsgCredentials;

	private static final Logger logger = Logger.getLogger(MessagingAgent.class.getName());

	private static final String EXCEPTION_WAS_THROWN = "an exception was thrown";

	// constant for the URL to the rail ticket
	private static final String RAIL_TICKET_URL = "https://storage.googleapis.com/bonjour-rail.appspot.com/rail-ticket.png";

	private static final String SEATING_CHART_URL = "https://storage.googleapis.com/bonjour-rail.appspot.com/seating-chart-example.png";

	// constant for suggestion to view trip information
	private static final SuggestionHelper TRIP_INFORMATION_OPTION = new SuggestionHelper("Trip Information",
			"trip_info");

	// constant for suggestion to view food menu options
	private static final SuggestionHelper MENU_OPTION = new SuggestionHelper("Menu Options", "menu_options");

	// constant for suggestion to view the seating chart
	private static final SuggestionHelper SEATING_CHART_OPTION = new SuggestionHelper("Seating Chart",
			"seating_charting");

	// constant for suggestion to change the assigned seat
	private static final SuggestionHelper CHANGE_SEAT_OPTION = new SuggestionHelper("Change Seat", "change_seat");

	// constant for suggestion to speak with an agent
	private static final SuggestionHelper TALK_TO_AGENT_OPTION = new SuggestionHelper("Speak With an Agent",
			"talk_to_agent");

	// constant for suggestion to view the map of the trip
	private static final SuggestionHelper MAP_OPTION = new SuggestionHelper("View Map", "map");

	// constant for suggestion to save the trip as a calendar event
	private static final SuggestionHelper CALENDAR_OPTION = new SuggestionHelper("Add to Calendar", "calendar");

	// constant for suggestion to book open seat 14
	private static final SuggestionHelper OPEN_SEAT_14 = new SuggestionHelper("Seat 14", "seat_14");

	// constant for suggestion to book open seat 15
	private static final SuggestionHelper OPEN_SEAT_15 = new SuggestionHelper("Seat 15", "seat_15");

	// constant for suggestion to book open seat 21
	private static final SuggestionHelper OPEN_SEAT_21 = new SuggestionHelper("Seat 21", "seat_21");

	// constant for ordering ham and cheese meal
	private static final SuggestionHelper ORDER_HAM_AND_CHEESE = new SuggestionHelper("Order", "order-ham-and-cheese");

	// constant for ordering ham and cheese meal
	private static final SuggestionHelper ORDER_CHICKEN_VEGGIE = new SuggestionHelper("Order", "order-chicken-veggie");

	// constant for ordering ham and cheese meal
	private static final SuggestionHelper ORDER_CHEESE_PLATE = new SuggestionHelper("Order", "order-cheese-plate");

	// constant for ordering ham and cheese meal
	private static final SuggestionHelper ORDER_APPLE_WALNUT = new SuggestionHelper("Order", "order-apple-walnut");

	// constant for carousel ham and cheese food item
	private static final StandaloneCardHelper HAM_AND_CHEESE_CARD = new StandaloneCardHelper("Ham and cheese sandwich",
			"With choice of beverage", "https://storage.googleapis.com/bonjour-rail.appspot.com/ham-and-cheese.png",
			ORDER_HAM_AND_CHEESE);

	// constant for carousel chicken veggie wrap food item
	private static final StandaloneCardHelper CHICKEN_VEGGIE_WRAP_CARD = new StandaloneCardHelper("Chicken veggie wrap",
			"With choice of beverage",
			"https://storage.googleapis.com/bonjour-rail.appspot.com/chicken-veggie-wrap.png", ORDER_CHICKEN_VEGGIE);

	// constant for carousel cheese plate food item
	private static final StandaloneCardHelper CHEESE_PLATE_CARD = new StandaloneCardHelper("Assorted cheese plate",
			"With choice of beverage",
			"https://storage.googleapis.com/bonjour-rail.appspot.com/assorted-cheese-plate.png", ORDER_CHEESE_PLATE);

	// constant for carousel apple walnut food item
	private static final StandaloneCardHelper APPLE_WALNUT_CARD = new StandaloneCardHelper("Apple walnut salad",
			"With choice of beverage", "https://storage.googleapis.com/bonjour-rail.appspot.com/apple-walnut-salad.png",
			ORDER_APPLE_WALNUT);

	// the name of the pub/sub pull subscription
	private static final String PUB_SUB_NAME = "rbm-agent-subscription";

	// pubsub subscription service for our pull requests
	private Subscriber subscriber;

	// reference to the RBM api builder
	private RCSBusinessMessaging.Builder builder;

	private RbmApiHelper rbmApiHelper;

	// the phone number, in E.164 format, to start a conversation with
	private String msisdn;
	private MessageResponseLogsRepository messageResponseLogsRepository;
	private LeadInfoDetailRepository leadInfoDetailRepository;
	private List<Long> activeLeads;
	private ReentrantReadWriteLock.ReadLock leadReadLock;
	private LeadInfoRepository leadInfoRepository;
	private BlackListRepository blackListRepository;

	/**
	 * Constructor of the FirstAgent class.
	 */
	public MessagingAgent(String msisdn) {
		logger.info("Initializing the agent.");

		this.msisdn = msisdn;

		// initialize pub/sub for pull monitoring
		initPubSub("rbm-agent-service-account-credentials.json");

		// initialize the API helper
		this.rbmApiHelper = new RbmApiHelper();
	}

	public MessagingAgent(MessageResponseLogsRepository messageResponseLogsRepository,
			LeadInfoDetailRepository leadInfoDetailRepository, List<Long> activeLeads,
			ReentrantReadWriteLock.ReadLock leadReadLock, LeadInfoRepository leadInfoRepository,
			BlackListRepository blackListRepository) {
		logger.info("Initializing the agent.");
		this.messageResponseLogsRepository = messageResponseLogsRepository;
		this.leadInfoDetailRepository = leadInfoDetailRepository;
		this.activeLeads = activeLeads;
		this.leadReadLock = leadReadLock;
		this.leadInfoRepository = leadInfoRepository;
		this.blackListRepository = blackListRepository;

		// initialize pub/sub for pull monitoring
		initPubSub("rbm-agent-service-account-credentials.json");

		// initialize the API helper
		this.rbmApiHelper = new RbmApiHelper();
	}

	public void setActiveLeads(List<Long> activeLeads) {
		this.activeLeads = activeLeads;
	}

	/**
	 * Creates a MessageReceiver handler for pulling new messages from the pubsub
	 * subscription.
	 *
	 * @return The MessageReceiver listener.
	 */
	private MessageReceiver getMessageReceiver() {
		return new MessageReceiver() {
			/**
			 * Handle incoming message, then ack/nack the received message.
			 *
			 * @param message  The message sent by the user.
			 * @param consumer Consumer for accepting a reply.
			 */
			public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
				String jsonResponse = message.getData().toStringUtf8();

				// logger.info("Id : " + message.getMessageId());
				// logger.info(jsonResponse);
				if (jsonResponse.contains(DELIVERED)) {
					try {
						PubSubDelieveryResponse pubSubDelieveryResponse = (new ObjectMapper()).readValue(jsonResponse,
								PubSubDelieveryResponse.class);
						MessageResponseLogs messageResponseLogs = new MessageResponseLogs();
						messageResponseLogs.setResponseType(DELIVERED);
						// messageResponseLogs.setLeadId(leadId);
						// messageResponseLogs.setCampaignId(campaignId);
						String number = pubSubDelieveryResponse.getSenderPhoneNumber();
						messageResponseLogs.setMsisdn(number.substring(number.length() - 10));
						Long leadId = getMyLead(messageResponseLogs.getMsisdn());
						if (leadId != null) {
							messageResponseLogs.setLeadId(leadId);
						}
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
						try {
							messageResponseLogs.setSendTime(sdf.parse(pubSubDelieveryResponse.getSendTime()));
							messageResponseLogs.setRecvTime(new Date());
							messageResponseLogs.setCompleteResponse(jsonResponse);

						} catch (Exception e) {
						}
						messageResponseLogsRepository.save(messageResponseLogs);
//                        markNumberAsDialed(pubSubDelieveryResponse.getSenderPhoneNumber());
					} catch (Exception e) {
						logger.error("Exception processing response: ", e);
						return;
					}
				} else if (jsonResponse.contains("suggestionResponse")) {
					PubSubReplyResponse pubSubReplyResponse = null;
					try {
						pubSubReplyResponse = (new ObjectMapper()).readValue(jsonResponse, PubSubReplyResponse.class);
					} catch (Exception e) {
						logger.error("Exception processing response: ", e);
						return;
					}
					MessageResponseLogs messageResponseLogs = new MessageResponseLogs();
					messageResponseLogs.setResponseType(pubSubReplyResponse.getSuggestionResponse().getType());
					// messageResponseLogs.setLeadId(leadId);
					// messageResponseLogs.setCampaignId(campaignId);
					String number = pubSubReplyResponse.getSenderPhoneNumber();
					messageResponseLogs.setMsisdn(number.substring(number.length() - 10));
					Long leadId = getMyLead(messageResponseLogs.getMsisdn());
					if (leadId != null) {
						messageResponseLogs.setLeadId(leadId);
					}
					messageResponseLogs.setResponse(pubSubReplyResponse.getSuggestionResponse().getText());
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
					try {
						messageResponseLogs.setSendTime(sdf.parse(pubSubReplyResponse.getSendTime()));
						messageResponseLogs.setRecvTime(new Date());
						messageResponseLogs.setCompleteResponse(jsonResponse);

					} catch (Exception e) {
					}
					messageResponseLogsRepository.save(messageResponseLogs);

				} else if (jsonResponse.toLowerCase().contains(TEXT.toLowerCase())) {
					Gson gson = new Gson();
					Type type = new TypeToken<Map<String, String>>() {
					}.getType();
					Map<String, String> jsonMap = gson.fromJson(jsonResponse, type);

					// make sure the map contains response text
					if (jsonMap.containsKey("text")) {
						String userResponseText = jsonMap.get("text");
						String number = jsonMap.get("senderPhoneNumber");
						number = number.substring(number.length() - 10);
						Long userId = getUserId(number);
						if (STOP.equalsIgnoreCase(userResponseText)) {

							BlackListEntity blackListEntity = new BlackListEntity();
							blackListEntity.setPhoneNumber(number);
							blackListEntity.setCreateDtm(new Date());
							blackListEntity.setUserId(userId);
							blackListEntity.setPrefix(Integer.valueOf(number.substring(0, 3)));
							blackListRepository.save(blackListEntity);
							logger.info("Got Stop for :" + number + " added in Blacklist repository");
							String stopAckMessage = "You have been unsubscribed and will not receive message from us. Thankyou !! ";
							sendTextMessage(stopAckMessage, jsonMap.get("senderPhoneNumber"));
						}
					}
				}
				// let the service know we successfully processed the response
				consumer.ack();
			}
		};
	}

	private Long getMyLead(String msisdn) {
		try {
			leadReadLock.lock();
			for (Long leadId : activeLeads) {
				LeadInfoDetailEntity byLeadIdAndPhoneNumber = leadInfoDetailRepository.getByLeadIdAndPhoneNumber(leadId,
						msisdn);
				if (byLeadIdAndPhoneNumber != null) {
					return byLeadIdAndPhoneNumber.getLeadId();
				}
			}
			List<LeadInfoEntity> allActiveLeads = leadInfoRepository.getAllActiveLeads();
			if (!CollectionUtils.isEmpty(allActiveLeads)) {
				for (Long leadId : activeLeads) {
					LeadInfoDetailEntity byLeadIdAndPhoneNumber = leadInfoDetailRepository
							.getByLeadIdAndPhoneNumber(leadId, msisdn);
					if (byLeadIdAndPhoneNumber != null) {
						return byLeadIdAndPhoneNumber.getLeadId();
					}
				}
			}
			return null;
		} finally {
			leadReadLock.unlock();
		}
	}

	private Long getUserId(String msisdn) {
		try {
			leadReadLock.lock();
			for (Long leadId : activeLeads) {
				LeadInfoDetailEntity byLeadIdAndPhoneNumber = leadInfoDetailRepository.getByLeadIdAndPhoneNumber(leadId,
						msisdn);
				if (byLeadIdAndPhoneNumber != null) {
					return leadInfoRepository.getLeadInfoEntityByLeadId(leadId).getUserId();
				}
			}
			List<LeadInfoEntity> allActiveLeads = leadInfoRepository.getAllActiveLeads();
			if (!CollectionUtils.isEmpty(allActiveLeads)) {
				List<Long> leadIds = allActiveLeads.stream().map(t -> t.getLeadId()).collect(Collectors.toList());
				for (Long leadId : leadIds) {
					LeadInfoDetailEntity byLeadIdAndPhoneNumber = leadInfoDetailRepository
							.getByLeadIdAndPhoneNumber(leadId, msisdn);
					if (byLeadIdAndPhoneNumber != null) {
						activeLeads.add(leadId);
						return leadInfoRepository.getLeadInfoEntityByLeadId(leadId).getUserId();
					}
				}
			}
			return null;
		} finally {
			leadReadLock.unlock();
		}
	}

	private void markNumberAsDialed(String senderPhoneNumber) {
		String phoneNumber = senderPhoneNumber.substring(3);
		// leadInfoDetailRepository.updateCdrsAsDialedWithStatus(leadId,phoneNumber);
	}

	private static String getJson(Object obj, boolean excludeNullFields) {
		String jsonString = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			if (excludeNullFields) {
				objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			}
			objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			jsonString = objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException jpe) {
		}
		return jsonString;
	}

	private MessageReceiver getMessageReceiver2() {
		return new MessageReceiver() {
			@Override
			public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
				String jsonResponse = message.getData().toStringUtf8();

				logger.info("Id : " + message.getMessageId());
				logger.info(jsonResponse);

				// use Gson to convert JSON response into a Map
				Gson gson = new Gson();
				Type type = new TypeToken<Map<String, String>>() {
				}.getType();
				Map<String, String> jsonMap = gson.fromJson(jsonResponse, type);

			}
		};
	}

	/**
	 * Initializes a pull subscription to receive user responses.
	 */
	private void initPubSub(String credentialsFileLocation) {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			// File file = new
			// File("/opt/rcsmessaging/rbm-agent-service-account-credentials.json");
			File file = new File(rcsMsgCredentials);
			CredentialsProvider credentialsProvider = FixedCredentialsProvider
					.create(ServiceAccountCredentials.fromStream(new FileInputStream(file)));

			GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(file))
					.createScoped(Collections.singletonList("https://www.googleapis.com/auth/pubsub"));
			credentials.refreshIfExpired();
			String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

			ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, PUB_SUB_NAME);

			// Instantiate an asynchronous message receiver
			MessageReceiver receiver = this.getMessageReceiver();

			// create PubSub subscription
			subscriber = Subscriber.newBuilder(subscriptionName, receiver).setCredentialsProvider(credentialsProvider)
					.build();

			logger.info("Starting Pub/Sub listener");
			subscriber.startAsync();
//            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();
//            TopicName topic = ProjectTopicName.of(projectId, "topic-"+leadId);
//            TopicAdminSettings.Builder topicAdminBuilder = TopicAdminSettings.newBuilder();
//            topicAdminBuilder.setCredentialsProvider(credentialsProvider);
//            try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminBuilder.build())) {
//                topicAdminClient.createTopic(topic);
//            }
//            ProjectSubscriptionName subscriptionName;
//            subscriptionName =
//                    ProjectSubscriptionName.of(projectId, PUB_SUB_NAME);
//            try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
//                subscriptionAdminClient.createSubscription(subscriptionName, topic, PushConfig.getDefaultInstance(), 0);
//            }
//
//            // Instantiate an asynchronous message receiver
//            MessageReceiver receiver = this.getMessageReceiver();

			// create PubSub subscription
//            subscriber = Subscriber.newBuilder(subscriptionName, receiver)
//                    .setCredentialsProvider(credentialsProvider)
//                    .build();
//
//            logger.info("Starting Pub/Sub listener");
//            subscriber.startAsync();
		} catch (Exception e) {
			logger.error(EXCEPTION_WAS_THROWN, e);
		}
	}

	public void subscribeAsyncExample(String projectId, String subscriptionId, String credentialsFileLocation) {
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

		// Instantiate an asynchronous message receiver.
		MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
			// Handle incoming message, then ack the received message.
			System.out.println("Id: " + message.getMessageId());
			System.out.println("Data: " + message.getData().toStringUtf8());
			consumer.ack();
		};

		Subscriber subscriber = null;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			// File file = new
			// File("/opt/rcsmessaging/rbm-agent-service-account-credentials.json");
			File file = new File(rcsMsgCredentials);
			CredentialsProvider credentialsProvider = FixedCredentialsProvider
					.create(ServiceAccountCredentials.fromStream(new FileInputStream(file)));

			subscriber = Subscriber.newBuilder(subscriptionName, receiver).setCredentialsProvider(credentialsProvider)
					.build();
			// Start the subscriber.
			subscriber.startAsync().awaitRunning();
			logger.info("Listening for messages on" + subscriptionName.toString());
			// Allow the subscriber to run for 30s unless an unrecoverable error occurs.
			subscriber.awaitTerminated(30, TimeUnit.SECONDS);
		} catch (Exception e) {
			// Shut down the subscriber after 30s. Stop receiving messages.
			subscriber.stopAsync();
		}
	}

	/**
	 * Takes the user's response and creates an appropriate response.
	 * <p>
	 * In this sample, the RBM agent responds with "I like USER_RESPONSE too!"
	 *
	 * @param responseText      The response the user sent to the agent.
	 * @param senderPhoneNumber The phone number that send the response.
	 */
	private void handleUserResponse(String responseText, String senderPhoneNumber) {
		responseText = responseText.toLowerCase();

//        if (responseText.equals("stop")) {
//            // Any real agent must support this command
//            // TODO: Client typed stop, agent should no longer send messages to this msisdn
//            logger.info(msisdn + " asked to stop agent messaging");
//        } else {
//            rbmApiHelper.sendIsTypingMessage(senderPhoneNumber);
//
//            try {
//                rbmApiHelper.sendTextMessage("I like " + responseText + " too!",
//                        senderPhoneNumber);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
	}

	/**
	 * Sends a user an invite to test this agent.
	 */
	private void sendTesterInvite() {
		try {
			rbmApiHelper.registerTester(msisdn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends the initial greeting of "What is your favorite color?" to the user.
	 */
	public void sendTextMessage(String textMessage, String msisdn) {
		try {
			rbmApiHelper.sendTextMessage(textMessage, msisdn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMediaMessage(String fileUrl, String msisdn) {
		try {
			// create media only message
			AgentContentMessage agentContentMessage = new AgentContentMessage();
			agentContentMessage.setContentInfo(new ContentInfo().setFileUrl(fileUrl));

			// attach content to message
			AgentMessage agentMessage = new AgentMessage();
			agentMessage.setContentMessage(agentContentMessage);

			rbmApiHelper.sendAgentMessage(agentMessage, msisdn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendSuggestedRepliesMessage(SuggestedReplies suggestedReplies, String msisdn) {
		try {
			List<Suggestion> suggestions = new ArrayList<>();
			boolean isSuggestedReplies = !suggestedReplies.getSuggestedReplies().isEmpty();
			if (isSuggestedReplies) {
				for (String suggestedReply : suggestedReplies.getSuggestedReplies()) {
					Suggestion suggestion = new SuggestionHelper(suggestedReply,
							suggestedReply.replaceAll("\\s+", "_").toLowerCase()).getSuggestedReply();
					suggestions.add(suggestion);
				}
			} else {
				for (MySuggestion mySuggestion : suggestedReplies.getSuggestions()) {
					Suggestion suggestion = new Suggestion();
					SuggestedAction suggestedAction = new SuggestedAction();
					suggestedAction.setText(mySuggestion.getAction().getText());
					suggestedAction.setPostbackData(mySuggestion.getAction().getPostbackData());
					if (mySuggestion.getAction() != null && mySuggestion.getAction().getOpenUrlAction() != null
							&& !StringUtils.isEmpty(mySuggestion.getAction().getOpenUrlAction().getUrl())) {
						OpenUrlAction openUrlAction = new OpenUrlAction();
						openUrlAction.setUrl(mySuggestion.getAction().getOpenUrlAction().getUrl());
						suggestedAction.setOpenUrlAction(openUrlAction);
					} else if (mySuggestion.getAction() != null
							&& mySuggestion.getAction().getCreateCalendarEventAction() != null) {
						MyCreateCalendarEventAction myCreateCalendarEventAction = mySuggestion.getAction()
								.getCreateCalendarEventAction();
						CreateCalendarEventAction createCalendarEventAction = new CreateCalendarEventAction();
						createCalendarEventAction.setTitle(myCreateCalendarEventAction.getTitle());
						createCalendarEventAction.setDescription(myCreateCalendarEventAction.getDescription());
						createCalendarEventAction.setStartTime(myCreateCalendarEventAction.getStartTime());
						createCalendarEventAction.setEndTime(myCreateCalendarEventAction.getEndTime());
						suggestedAction.setCreateCalendarEventAction(createCalendarEventAction);
					} else if (mySuggestion.getAction() != null && mySuggestion.getAction().getDialAction() != null) {
						DialAction dialAction = new DialAction();
						dialAction.setPhoneNumber(mySuggestion.getAction().getDialAction().getPhoneNumber());
						suggestedAction.setDialAction(dialAction);
					} else if (mySuggestion.getAction() != null && mySuggestion.getAction().isShareLocationAction()) {
						ShareLocationAction shareLocationAction = new ShareLocationAction();
						suggestedAction.setShareLocationAction(shareLocationAction);
					} else if (mySuggestion.getAction() != null
							&& mySuggestion.getAction().getViewLocationAction() != null) {
						MyViewLocationAction myViewLocationAction = mySuggestion.getAction().getViewLocationAction();
						ViewLocationAction viewLocationAction = new ViewLocationAction();
						if (myViewLocationAction.getLatLong() != null) {
							LatLng latLng = new LatLng();
							latLng.setLatitude(myViewLocationAction.getLatLong().getLatitude());
							latLng.setLongitude(myViewLocationAction.getLatLong().getLongitude());
							viewLocationAction.setLatLong(latLng);
						}
						viewLocationAction.setLabel(myViewLocationAction.getLabel());
						viewLocationAction.setQuery(myViewLocationAction.getQuery());
						suggestedAction.setViewLocationAction(viewLocationAction);
					}

					suggestion.setAction(suggestedAction);
					suggestions.add(suggestion);
				}
			}

			// Send simple text message to user
			rbmApiHelper.sendTextMessage(suggestedReplies.getTextMessage(), msisdn, suggestions);
		} catch (Exception e) {
			logger.error("Got Exception in sending message ", e);
		}
	}

	public void sendGreeting() throws IOException {
		List<Suggestion> suggestedActions = getDefaultSuggestionList();

		// create a standalone card for the rail ticket
		StandaloneCard standaloneCard = rbmApiHelper.createStandaloneCard("Hello, welcome to Shivtel Rail.",
				"How can I help you?",
				"https://i.pinimg.com/736x/a0/67/5e/a0675e5161d7ae5be2550987f397a641--flower-shops-paper-flowers.jpg",
				MediaHeight.TALL, CardOrientation.VERTICAL, suggestedActions);

		rbmApiHelper.sendStandaloneCard(standaloneCard, msisdn);
	}

	public StandaloneCard createStandaloneCard(RichCards richCards) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		SuggestedReplies suggestedReplies = richCards.getSuggestedReplies();
		for (MySuggestion mySuggestion : suggestedReplies.getSuggestions()) {
			// Suggestion suggestion = new SuggestionHelper(suggestedReply,
			// suggestedReply.replaceAll("\\s+", "_").toLowerCase()).getSuggestedReply();
			Suggestion suggestion = new Suggestion();
			MySuggestedAction mySuggestedAction = suggestedReplies.getSuggestions().get(0).getAction();
			SuggestedAction suggestedAction = new SuggestedAction();
			suggestedAction.setText(mySuggestion.getAction().getText());
			suggestedAction.setPostbackData(mySuggestion.getAction().getPostbackData());
			DialAction dialAction = new DialAction();
			dialAction.setPhoneNumber(mySuggestedAction.getDialAction().getPhoneNumber());
			suggestedAction.setDialAction(dialAction);
			suggestion.setAction(suggestedAction);
			suggestions.add(suggestion);
		}

		MediaHeight mediaHeight;
		if (MediaHeight.TALL.toString().equalsIgnoreCase(richCards.getHeight())) {
			mediaHeight = MediaHeight.TALL;
		} else if (MediaHeight.MEDIUM.toString().equalsIgnoreCase(richCards.getHeight())) {
			mediaHeight = MediaHeight.MEDIUM;
		} else {
			mediaHeight = MediaHeight.SHORT;
		}

		CardOrientation cardOrientation;
		if (CardOrientation.VERTICAL.toString().equalsIgnoreCase(richCards.getOrientation())) {
			cardOrientation = CardOrientation.VERTICAL;
		} else {
			cardOrientation = CardOrientation.HORIZONTAL;
		}

		StandaloneCard standaloneCard = rbmApiHelper.createStandaloneCard(richCards.getTitle(),
				richCards.getDescription(), richCards.getImageUrl(), mediaHeight, cardOrientation, suggestions);
		return standaloneCard;
	}

	public void sendStandaloneCard(StandaloneCard standaloneCard, String msisdn) throws IOException {
		rbmApiHelper.sendStandaloneCard(standaloneCard, msisdn);
	}

	/**
	 * Creates the default set of suggested replies for a client.
	 * 
	 * @return A list of suggested replies.
	 */
	private List<Suggestion> getDefaultSuggestionList() {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();

		suggestions.add(TRIP_INFORMATION_OPTION.getSuggestedReply());
		suggestions.add(MENU_OPTION.getSuggestedReply());
		suggestions.add(SEATING_CHART_OPTION.getSuggestedReply());

		return suggestions;
	}

	// [START run_application]
	public static void main(String[] args) {
		if (args.length != 2 && args.length != 3) {
			logger.info("Usage: mvn exec:java " + "-Dexec.args=\"<PHONE E.164> <MODE>\"");

			System.exit(-1);
		}

		try {
			String msisdn = args[0];
			String mode = "chat";

			if (args.length > 1) {
				mode = args[1];
			}

			// create agent
			MessagingAgent messagingAgent = new MessagingAgent(msisdn);

			if (mode.equals("chat")) {
				// send opening message to user
				messagingAgent.sendGreeting();

				// run until terminated
				while (true) {
					Thread.sleep(Long.MAX_VALUE);
				}
			} else {
				// send tester invite to user
				messagingAgent.sendTesterInvite();

				logger.info("Tester invite sent to " + msisdn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// [END run_application]
}