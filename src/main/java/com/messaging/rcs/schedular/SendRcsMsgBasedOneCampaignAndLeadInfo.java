/*
 * package com.messaging.rcs.schedular;
 * 
 * import java.text.SimpleDateFormat; import java.util.Date; import
 * java.util.List; import java.util.stream.Collectors;
 * 
 * import org.apache.log4j.Logger; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.beans.factory.annotation.Value; import
 * org.springframework.scheduling.annotation.Async; import
 * org.springframework.scheduling.annotation.Scheduled; import
 * org.springframework.stereotype.Service;
 * 
 * import com.google.gson.Gson; import com.messaging.rcs.domain.CampaignEntity;
 * import com.messaging.rcs.domain.LeadInfoDetailEntity; import
 * com.messaging.rcs.domain.LeadInfoEntity; import
 * com.messaging.rcs.domain.UserEntity; import
 * com.messaging.rcs.repository.CampaignRepository; import
 * com.messaging.rcs.repository.LeadInfoDetailRepository; import
 * com.messaging.rcs.repository.LeadInfoRepository; import
 * com.messaging.rcs.repository.UserRepository; import
 * com.messaging.rcs.vi.bot.request.BotApiRequest; import
 * com.messaging.rcs.vi.bot.request.MessageContact; import
 * com.messaging.rcs.vi.bot.request.RCSMessage; import
 * com.messaging.rcs.vi.bot.request.TemplateMessage; import
 * com.messaging.rcs.vi.bot.response.BotApiResponse; import
 * com.messaging.rcs.vi.bot.response.CustomParams;
 * 
 *//**
	 * 
	 * @author RahulRajput
	 *
	 */
/*
 * @Service public class SendRcsMsgBasedOneCampaignAndLeadInfo { private static
 * final Logger LOGGER =
 * Logger.getLogger(SendRcsMsgBasedOneCampaignAndLeadInfo.class.getName());
 * 
 * @Value("${bot.fileUrl}") public String botfileUrl;
 * 
 * @Value("${templateCode}") public String templateCode;
 * 
 * @Autowired private CampaignRepository campaignRepository;
 * 
 * @Autowired private LeadInfoRepository leadInfoRepository;
 * 
 * @Autowired private LeadInfoDetailRepository leadInfoDetailRepository;
 * 
 * @Autowired private UserRepository userRepository;
 * 
 * @Autowired private BotTokenAPIService botTokenAPIService;
 * 
 * //@Scheduled(cron = "0 * * * * *")
 * 
 * @Async("processExecutor") public void sendRcsMsg() throws Exception {
 * LOGGER.info("***** sendRcsMsg() Schedular Started *****");
 * List<CampaignEntity> activeCamList = null; List<LeadInfoEntity> leadInfoList
 * = null; List<LeadInfoDetailEntity> leadInfoDetailsList = null;
 * List<UserEntity> userList = userRepository.findAll(); activeCamList =
 * campaignRepository.getActiveCampaignsByUserId(); if (activeCamList.size() >
 * 0) { LOGGER.info("CAMPAIGN SIZE FROM SCHEDULAR:: " + activeCamList.size());
 * for (CampaignEntity cam : activeCamList) { List<UserEntity> campUser =
 * userList.stream().filter(user -> user.getUserId() == cam.getUserId())
 * .collect(Collectors.toList());
 * 
 * leadInfoList =
 * leadInfoRepository.getAllActiveLeadsByCampaignIdAndUserId(cam.getCampaignId()
 * , cam.getUserId()); if (leadInfoList.size() > 0) {
 * LOGGER.info("LEAD INFO SIZE FROM SCHEDULAR:: " + leadInfoList.size());
 * 
 * for (LeadInfoEntity leadInfo : leadInfoList) { leadInfoDetailsList =
 * leadInfoDetailRepository.findByLeadIdAndUserId(leadInfo.getLeadId()); if
 * (leadInfoDetailsList.size() > 0) {
 * LOGGER.info("LEAD INFO DETAILS SIZE FROM SCHEDULAR:: " +
 * leadInfoList.size());
 * 
 * for (LeadInfoDetailEntity leadInfoDetails : leadInfoDetailsList) {
 * sendRcsMSGByMSISDN(campUser.get(0), leadInfoDetails); } }
 * leadInfoRepository.updateLeadCompletionStatus(leadInfo.getLeadId(),
 * "Completed");
 * 
 * LOGGER.info("**** leadInfoRepository.updateLeadCompletionStatus(" +
 * leadInfo.getLeadId() + ",Completed) *****");
 * 
 * 
 * } } // TODO update campaign status
 * campaignRepository.updateCampaignStatus(cam.getCampaignId(), "Completed");
 * 
 * LOGGER.info( "**** campaignRepository.updateCampaignStatus(" +
 * cam.getCampaignId() + ",Completed) *****");
 * 
 * }
 * 
 * } else { LOGGER.info("**** Active Campaign List Not Found. *****"); }
 * LOGGER.info("***** sendRcsMsg() Schedular Ended *****"); }
 * 
 *//**
	 * 
	 * @param campUser        synchronized
	 * @param leadInfoDetails
	 * @throws Exception
	 *//*
		 * public void sendRcsMSGByMSISDN(UserEntity campUser, LeadInfoDetailEntity
		 * leadInfoDetails) throws Exception { SimpleDateFormat sdf = new
		 * SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); TokenPojo pojo = null;
		 * 
		 * System.out.println("******** RCS MSG Ready For Sending This MSISDN ::=> " +
		 * leadInfoDetails.getPhoneNumber()); String responseFromViBotAPI = "";
		 * BotApiResponse botApiResponse = null; try {
		 * 
		 * pojo = new
		 * Gson().fromJson(botTokenAPIService.getTokenFromClientAPI(campUser.getBotToken
		 * ()), TokenPojo.class); if (pojo.getAccess_token() == null ||
		 * pojo.getAccess_token().equals("") || pojo.getAccess_token().isEmpty()) {
		 * System.out.println("***** Token Not Generated *****"); throw new
		 * Exception("***** Token Not Generated *****"); } BotApiRequest botRequest =
		 * new BotApiRequest(); TemplateMessage templateMessage = new TemplateMessage();
		 * MessageContact messageContact = new MessageContact(); RCSMessage RCSMessage =
		 * new RCSMessage();
		 * messageContact.setUserContact(leadInfoDetails.getPhoneNumber()); CustomParams
		 * customParams = new CustomParams(); customParams.setAmount("5000");
		 * customParams.setDate(sdf.format(new Date()));
		 * templateMessage.setTemplateCode(templateCode);
		 * //templateMessage.setCustomParams(new
		 * Gson().toJson(customParams).toString());
		 * RCSMessage.setTemplateMessage(templateMessage);
		 * 
		 * botRequest.setRCSMessage(RCSMessage);
		 * botRequest.setMessageContact(messageContact);
		 * 
		 * System.out.
		 * println("********  Request For botTokenService.sendMessageByMSISDToClientAPI():: "
		 * + new Gson().toJson(botRequest).toString());
		 * 
		 * responseFromViBotAPI = botTokenAPIService .sendMessageByMSISDToClientAPI(new
		 * Gson().toJson(botRequest).toString(),
		 * pojo.getAccess_token(),campUser.getBotId());
		 * System.out.println("******** Response " + responseFromViBotAPI);
		 * 
		 * botApiResponse = new Gson().fromJson(responseFromViBotAPI,
		 * BotApiResponse.class);
		 * 
		 * if (botApiResponse.getRCSMessage().getStatus().equalsIgnoreCase("failed")) {
		 * System.out.println("******** Response " + botApiResponse.toString());
		 * 
		 * leadInfoDetailRepository.updateStatusByLeadInfoDetailIdAndStatusAndLeadId(
		 * leadInfoDetails.getLeadInfoDetailId(),
		 * botApiResponse.getRCSMessage().getStatus(), leadInfoDetails.getLeadId());
		 * 
		 * } else { System.out.println("******** Response " +
		 * botApiResponse.toString());
		 * 
		 * leadInfoDetailRepository.updateStatusByLeadInfoDetailIdAndStatusAndLeadId(
		 * leadInfoDetails.getLeadInfoDetailId(),
		 * botApiResponse.getRCSMessage().getStatus(), leadInfoDetails.getLeadId());
		 * 
		 * // update lead_info_details
		 * 
		 * System.out.println("***** Lead Info Details Updated. *****");
		 * 
		 * }
		 * 
		 * } catch (Exception e) { e.printStackTrace(); } } }
		 */