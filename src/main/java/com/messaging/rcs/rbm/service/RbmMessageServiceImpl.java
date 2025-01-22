package com.messaging.rcs.rbm.service;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.rcsbusinessmessaging.v1.SuggestionHelper;
import com.google.api.services.rcsbusinessmessaging.v1.model.OpenUrlAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.RichCard;
import com.google.api.services.rcsbusinessmessaging.v1.model.SuggestedAction;
import com.google.api.services.rcsbusinessmessaging.v1.model.Suggestion;
import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.MessageTypeEntity;
import com.messaging.rcs.rbm.messages.*;
import com.messaging.rcs.repository.CampaignRepository;
import com.messaging.rcs.repository.MessageTypeRepository;
import com.messaging.rcs.service.MessageProcessingEngine;
import com.messaging.rcs.service.Notifications;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.messaging.rcs.util.SystemConstants.*;

/**
 * Created by sbsingh on Nov/21/2021.
 */
@Service
public class RbmMessageServiceImpl implements RbmMessageService{

    private static final Logger LOGGER = Logger.getLogger(RbmMessageServiceImpl.class.getName());

    @Autowired
    private MessageTypeRepository messageTypeRepository;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private MessageProcessingEngine messageProcessingEngine;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<MessageTypeEntity> getAllMessageTypes() {
        return messageTypeRepository.findAll();
    }

    @Override
    public void addMessageToCampaign(Long campaignId, Long messageId, Object message) throws Exception{
        CampaignEntity campaignEntity = campaignRepository.getByCampaignId(campaignId);
        if (campaignEntity == null) {
            throw new RuntimeException("Campaign not found !!");
        }

        MessageTypeEntity byMessageId = messageTypeRepository.findByMessageId(messageId);
        if (byMessageId ==  null) {
            throw new RuntimeException("Message Id incorrect !!");
        }
        String messageType = byMessageId.getMessageType();
        String messageBody= null;
        switch (messageType) {
            case TEXT:
                Text text = objectMapper.readValue((String)message, Text.class);
                messageBody = getJson(text, false);
                break;
            case MEDIA:
                Media media = objectMapper.readValue((String)message, Media.class);
                messageBody = getJson(media, false);
                break;
            case SUGGESTED_REPLIES:
                SuggestedReplies suggestedReplies = objectMapper.readValue((String)message, SuggestedReplies.class);
                messageBody = getJson(suggestedReplies, false);
                break;
            case  RICH_CARDS:
                RichCards richCards = objectMapper.readValue((String)message, RichCards.class);
                messageBody = getJson(richCards, false);
                break;
            case RICH_CARD_CAR:
                RichCardCarousels richCardCarousels = objectMapper.readValue((String)message, RichCardCarousels.class);
                messageBody = getJson(richCardCarousels, false);
                break;
            default:
                throw new RuntimeException("Incorrect Message Type");
        }

        campaignEntity.setMessageId(messageId);
        campaignEntity.setMessageJson(messageBody);
        campaignEntity = campaignRepository.save(campaignEntity);
        Notifications.getsInstance().notifyEventCaptured(CAMPAIGN_CREATED,campaignEntity);
        messageProcessingEngine.addOrReplaceMessage(campaignEntity);
        LOGGER.info("Saved campaign with Message Body");
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
            LOGGER.error("Unable to deserialize obj " + obj.toString(), jpe);
        }
        return jsonString;
    }

    public static void main(String[] args) {

       RichCards richCards = new RichCards();
       richCards.setTitle("This is the Title");
       richCards.setDescription("This is the Description");
       richCards.setImageUrl("https://www.google.com/logos/doodles/2021/poland-national-day-2021-6753651837109131-2xa.gif");
       richCards.setHeight("MEDIUM");
       richCards.setOrientation("VERTICAL");

        SuggestedReplies suggestedReplies = new SuggestedReplies();
        MySuggestion mySuggestion = new MySuggestion();
        MySuggestedAction suggestedAction = new MySuggestedAction();
        suggestedAction.setText("Call Now");
        suggestedAction.setPostbackData("Call_Now");
        MyDialAction myDialAction = new MyDialAction();
        myDialAction.setPhoneNumber("+918800737800");
        suggestedAction.setDialAction(myDialAction);
        mySuggestion.setAction(suggestedAction);
        suggestedReplies.getSuggestions().add(mySuggestion);
        richCards.setSuggestedReplies(suggestedReplies);

//        Suggestion suggestion1 = new Suggestion();
//        MySuggestion mySuggestion = new MySuggestion();
//        mySuggestion.setReply(suggestion1.getReply());
//        MySuggestedAction suggestedAction1 = new MySuggestedAction();
//        MyOpenUrlAction openUrlAction1 = new MyOpenUrlAction();
//        openUrlAction1.setUrl("https://www.apple.com/in/?cid-oas-in-domains-apple.in/");
//        suggestedAction1.setOpenUrlAction(openUrlAction1);
//        suggestedAction1.setText("Apple");
//        suggestedAction1.setPostbackData("Option_1");
//        mySuggestion.setAction(suggestedAction1);
//        suggestedReplies.getSuggestions().add(mySuggestion);
//
//        Suggestion suggestion2 = new Suggestion();
//        MySuggestion mySuggestion2 = new MySuggestion();
//        mySuggestion2.setReply(suggestion2.getReply());
//        MySuggestedAction suggestedAction2 = new MySuggestedAction();
//        MyOpenUrlAction openUrlAction2 = new MyOpenUrlAction();
//        openUrlAction2.setUrl("https://www.google.com/");
//        suggestedAction2.setOpenUrlAction(openUrlAction2);
//        suggestedAction2.setText("Google");
//        suggestedAction2.setPostbackData("Option_2");
//        mySuggestion2.setAction(suggestedAction2);
//        suggestedReplies.getSuggestions().add(mySuggestion2);
//
//        Suggestion suggestion3 = new Suggestion();
//        MySuggestion mySuggestion3 = new MySuggestion();
//        mySuggestion3.setReply(suggestion3.getReply());
//        MySuggestedAction suggestedAction3 = new MySuggestedAction();
//        MyOpenUrlAction openUrlAction3 = new MyOpenUrlAction();
//        openUrlAction3.setUrl("https://in.search.yahoo.com/?fr2=inr");
//        suggestedAction3.setOpenUrlAction(openUrlAction3);
//        suggestedAction3.setText("Yahoo");
//        suggestedAction3.setPostbackData("Option_3");
//        mySuggestion3.setAction(suggestedAction3);
//        suggestedReplies.getSuggestions().add(mySuggestion3);
//
//        Suggestion suggestion4 = new Suggestion();
//        MySuggestion mySuggestion4 = new MySuggestion();
//        mySuggestion4.setReply(suggestion4.getReply());
//        MySuggestedAction suggestedAction4 = new MySuggestedAction();
//        MyOpenUrlAction openUrlAction4 = new MyOpenUrlAction();
//        openUrlAction4.setUrl("https://www.tesla.com/");
//        suggestedAction4.setOpenUrlAction(openUrlAction4);
//        suggestedAction4.setText("Tesla");
//        suggestedAction4.setPostbackData("Option_4");
//        mySuggestion4.setAction(suggestedAction4);
//        suggestedReplies.getSuggestions().add(mySuggestion4);



        System.out.println(getJson(richCards, false));
    }
}
