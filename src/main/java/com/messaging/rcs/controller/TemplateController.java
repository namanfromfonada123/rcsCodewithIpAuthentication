package com.messaging.rcs.controller;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.messaging.rcs.domain.OperatorApiConfigEntity;
import com.messaging.rcs.domain.TemplateFileDB;
import com.messaging.rcs.domain.UserBotMappingEntity;
import com.messaging.rcs.email.model.ResponseFile;
import com.messaging.rcs.email.model.TemplateModel;
import com.messaging.rcs.email.repository.TemplateFileDbRepository;
import com.messaging.rcs.model.Template;
import com.messaging.rcs.repository.OperatorApiConfigRepository;
import com.messaging.rcs.repository.OperatorRepository;
import com.messaging.rcs.repository.RcsMsgTypeRepository;
import com.messaging.rcs.repository.TemplateRepository;
import com.messaging.rcs.repository.UserBotMappingRepository;
import com.messaging.rcs.repository.UserRepository;
import com.messaging.rcs.schedular.BotTokenAPIService;
import com.messaging.rcs.schedular.TokenPojo;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RestController
@RequestMapping(value = { "/api/v1/rcsmessaging/template" }, produces = { "application/json" })
public class TemplateController {
	@Value("${template.basic.token}")
	public String templateBasicToken;

	private static final Logger LOGGER = Logger
			.getLogger(com.messaging.rcs.controller.TemplateController.class.getName());

	private static final String STATUS = "status";

	private BeanUtilsBean beanUtils = new BeanUtilsBean();

	private static final String MESSAGE = "message";

	@Value("${template.path}")
	private String templatePath;

	@Value("${approved.template.status.api}")
	private String approvedTempalteStatusApi;

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private TemplateFileDbRepository templateFileDbRepository;

	@Autowired
	private BotTokenAPIService botTokenAPIService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RcsMsgTypeRepository rcsMsgTypeRepository;

	@Autowired
	private OperatorApiConfigRepository operatorApiConfigRepository;
	@Autowired
	private OperatorRepository operatorRepository;
	@Autowired
	private UserBotMappingRepository userBotMappingRepository;

	@PostMapping({ "/resizeImage" })
	public String resizeImage(@RequestParam("imageHttpUrl") String imageHttpUrl,
			@RequestParam("targetWidth") int targetWidth, @RequestParam("targetHeight") int targetHeight)
			throws Exception {
		URL url = null;
		try {
			LOGGER.info("***** Controller Inside Resize Image *****");
			url = saveActualImage(imageHttpUrl);
			String extension = "";
			extension = FilenameUtils.getExtension(url.getPath());
			BufferedImage tempImg = null;
			Image img = ImageIO.read(new File(this.templatePath + FilenameUtils.getName(url.getPath())));
			tempImg = resizeImage(img, targetWidth, targetHeight);
			ImageIO.write(tempImg, extension, new File(this.templatePath + FilenameUtils.getName(url.getPath())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.templatePath + FilenameUtils.getName(url.getPath());
	}

	public URL saveActualImage(String imageHttpUrl) throws IOException {
		LOGGER.info("***** Now Getting Image From URL saveActualImage() *****");
		URL url = new URL(imageHttpUrl);
		URLConnection uc = url.openConnection();
		uc.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		uc.connect();
		InputStream is = uc.getInputStream();
		LOGGER.info(
				"Now Image Is Saving Inside This Path:: " + this.templatePath + FilenameUtils.getName(url.getPath()));
		OutputStream os = new FileOutputStream(this.templatePath + FilenameUtils.getName(url.getPath()));
		byte[] b = new byte[2048];
		int length;
		while ((length = is.read(b)) != -1)
			os.write(b, 0, length);
		is.close();
		os.close();
		return url;
	}

	public void imageCompress() throws IOException {
		File inputFile = new File("Path of the input imageimage1.jpg");
		File outFile = new File("Path of the outPut result imageopImage.jpg");
		String fileType = "jpg";
		BufferedImage tempImg = null;
		Image img = ImageIO.read(inputFile);
		tempImg = resizeImage(img, 600, 400);
		ImageIO.write(tempImg, fileType, outFile);
	}

	public BufferedImage resizeImage(Image image, int width, int height) {
		LOGGER.info(
				"***** Now Image is going  For Designing inside resizeImage(final Image image, int width, int height)  *****");
		BufferedImage bufferedImage = new BufferedImage(width, height, 1);
		Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.Src);
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		graphics2D.dispose();
		LOGGER.info("***** Image  Designing Done Iinside resizeImage(final Image image, int width, int height)  *****");
		return bufferedImage;
	}

	public File convert(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	@PostMapping({ "/addTemplate" })
	public ResponseEntity<HashMap<String, Object>> createTemplate(
			@RequestParam(value = "addTemplate") String addTemplate,
			@RequestParam(value = "files", required = false) MultipartFile[] files,
			@RequestParam(value = "fileName", required = false) List<String> fileName) throws Exception {
		LOGGER.info("Inside Template Controller::" + addTemplate.toString());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String date = sdf.format(new Date());
		String fileResponse = "";
		List<String> fileNames = new ArrayList<>();
		Template savedtemplate = null;
		Template template = new Template();
		TemplateModel templateModel = (TemplateModel) (new ObjectMapper()).readValue(addTemplate, TemplateModel.class);
		HashMap<String, Object> result = new HashMap<>();
		OperatorApiConfigEntity operatorApiConfigEntity = null;
		UserBotMappingEntity userBotMappingEntity = null;
		try {

			if (Integer.valueOf(templateModel.getRcsMsgTypeId()).intValue() == 2) {
				LOGGER.info("****** Controller Checking SMS Param ******");
				if (Objects.isNull(templateModel.getSms_content())
						|| Objects.isNull(templateModel.getSms_dlt_content_id())
						|| Objects.isNull(templateModel.getSms_dlt_principle_id())
						|| Objects.isNull(templateModel.getSms_senderId())) {
					result.put("status", HttpStatus.NOT_FOUND);
					result.put("message",
							"SMS ContentId Or DltPrincipleId Or DltContentId Or SenderId Or Content Cannot Be Empty");
					return new ResponseEntity(result, HttpStatus.CREATED);
				}
			}

			if (Objects.isNull(templateModel.getUserBotMappingId())) {
				result.put("status", HttpStatus.NOT_FOUND);
				result.put("message", "User Bot ID Cannot Be Empty");
				return new ResponseEntity(result, HttpStatus.CREATED);
			} else {
				userBotMappingEntity = userBotMappingRepository.findById(templateModel.getUserBotMappingId());
			}
			if (Objects.nonNull(
					this.templateRepository.findFirstByTemplateCodeOrderById(templateModel.getTemplateCode()))) {
				result.put("status", HttpStatus.CREATED);
				result.put("message", "Template Code Already Exist.");
				return new ResponseEntity(result, HttpStatus.CREATED);
			}
			if (templateModel != null) {
				template.setStatus(Integer.valueOf(0));
				this.beanUtils.copyProperties(template, templateModel);
			}
			if (Integer.valueOf(templateModel.getRcsMsgTypeId()).intValue() == 2) {
				template.setStatus(Integer.valueOf(1));
				template.setApproveResponse("approved");
				template = (Template) this.templateRepository.save(template);
				result.put("message", "Template Added Successfully ");
				result.put("status", HttpStatus.OK);
				result.put("Template", template);
				return new ResponseEntity(result, HttpStatus.CREATED);
			}
			if (Integer.valueOf(templateModel.getRcsMsgTypeId()).intValue() == 1
					|| Integer.valueOf(templateModel.getRcsMsgTypeId()).intValue() == 4) {
				if (Objects.nonNull(files))
					for (MultipartFile file : files) {
						fileResponse = "And Files Saved this location ::" + this.templatePath;
						LOGGER.info("****** SAVING RCS FILE GIVEN PATH ::" + fileResponse + "/"
								+ file.getOriginalFilename());
						byte[] bytes = new byte[0];
						bytes = file.getBytes();
						Files.write(Paths.get(this.templatePath + file.getOriginalFilename(), new String[0]), bytes,
								new java.nio.file.OpenOption[0]);
					}

				operatorApiConfigEntity = operatorApiConfigRepository.findByApiTypeAndTemplateTypeAndOperatorId(
						"TEMPLATE",
						operatorRepository.findByOperatorName(userBotMappingEntity.getOperator()).getOperatorId());

				if (Objects.isNull(operatorApiConfigEntity)) {
					LOGGER.info("****** Controller Checking SMS Param ******");
					if (Objects.isNull(operatorApiConfigEntity)) {
						result.put("status", HttpStatus.NOT_FOUND);
						result.put("message", "Template Api Config Not Exist.");
						return new ResponseEntity(result, HttpStatus.CREATED);
					}
				}
				template.setOperator(userBotMappingEntity.getOperator());
				template.setTemplateMode(userBotMappingEntity.getBotType());

				ResponseEntity<HashMap<String, Object>> operatorOutput = sendTemplateToOperator(userBotMappingEntity,
						operatorApiConfigEntity, template, files, fileNames);
				if (operatorOutput.getStatusCodeValue() == 201) {
					LOGGER.info("****** GOING ON SAVING TEMPALTE AFTER GETTING OPERATOR RESPONSE ::"
							+ operatorOutput.getBody());
					template = (Template) this.templateRepository.save(template);
					if (Objects.nonNull(files))
						for (MultipartFile file : files) {
							LOGGER.info("****** GOING ON SAVING RCS FILE IN DB ::" + fileResponse + "/"
									+ file.getOriginalFilename());
							try {
								TemplateFileDB fileDb = new TemplateFileDB();
								fileDb.setData(file.getBytes());
								fileDb.setName(file.getOriginalFilename());
								fileDb.setType(file.getContentType());
								fileDb.setUserId(templateModel.getTemplateUserId());
								fileDb.setCreatedDate(date);
								fileDb.setTemplateId(template.getId());
								fileNames.add(file.getOriginalFilename());
								TemplateFileDB savedTemplateFile = (TemplateFileDB) this.templateFileDbRepository
										.save(fileDb);
								if (savedTemplateFile != null)
									LOGGER.info(
											"******AFTER SAVED TEMPLATE ID IN TEMPLATE FILE DB TABLE With Template Id::"
													+ savedTemplateFile

															.getTemplateId());
							} catch (IOException e) {
								e.printStackTrace();
								result.put("message", e.getMessage());
								result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
								result.put("Template", template);
								return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
					return operatorOutput;
				}
				return operatorOutput;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("message", e.getMessage());
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			result.put("Template", template);
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping({ "/findAllTemplate" })
	public ResponseEntity<HashMap<String, Object>> findAllTemplate(@RequestParam("rcsMsgTypeId") String rcsMsgTypeId,
			@RequestParam("from") String from, @RequestParam("to") String to,
			@RequestParam("templateUserId") Long templateUserId,
			@RequestParam(value = "start", required = false) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "templateCode", required = false) String templateCode,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam("downloadStatus") String downloadStatus,
			@RequestParam(value = "botId", required = false) String botId) throws Exception {
		LOGGER.info("Inside Template Controller");
		HashMap<String, Object> result = new HashMap<>();
		long templateCount = 0L;
		Pageable pageable = null;
		List<Template> templateList = null;
		try {
			LOGGER.info("Template Total Size::=>" + templateCount);
			if (limit.intValue() == 0 && !downloadStatus.equalsIgnoreCase("D")) {
				templateList = this.templateRepository.findAllByTemplateUserIdWithoutDateRange(templateUserId,
						rcsMsgTypeId);
			} else {
				PageRequest pageRequest = null;
				start = Integer.valueOf(start.intValue() - 1);
				if (start.intValue() >= 0) {
					int pageSize = limit.intValue();
					int pageNum = (start.intValue() != 0) ? (start.intValue() / pageSize) : 0;
					pageRequest = PageRequest.of(pageNum, pageSize);
				}
				if (Objects.nonNull(templateCode) && Objects.nonNull(status) && Objects.nonNull(botId)) {
					LOGGER.info("TemplaCode And Status Is Not Null.");
					templateCount = this.templateRepository.countByTemplateUserIdAndStatusAndTemplateCodeAndBotId(
							templateUserId, templateCode, status, rcsMsgTypeId, botId);
					templateList = this.templateRepository.findAllByUserIdAndTemplateCodeAndStatusAndBotId(
							(Pageable) pageRequest, templateUserId, templateCode, status, rcsMsgTypeId, botId);
				}
				if (Objects.isNull(templateCode) && Objects.isNull(botId) && Objects.nonNull(status)
						&& limit.intValue() != 0) {
					LOGGER.info("Status  Is  NOT Null.");
					templateCount = this.templateRepository.countByTemplateUserIdAndStatus(templateUserId, status,
							rcsMsgTypeId);
					templateList = this.templateRepository.findAllByUserIdAndStatus((Pageable) pageRequest,
							templateUserId, status, rcsMsgTypeId);
				}
				if (Objects.nonNull(templateCode) && Objects.isNull(botId) && Objects.isNull(status)) {
					LOGGER.info("TemplateCode  Is NOT Null.");
					templateCount = this.templateRepository.countByTemplateUserIdAndTemplateCode(templateUserId,
							templateCode, rcsMsgTypeId);
					templateList = this.templateRepository.findAllByUserIdAndTemplateCode((Pageable) pageRequest,
							templateUserId, templateCode, rcsMsgTypeId);
				}
				if (Objects.isNull(templateCode) && Objects.nonNull(botId) && Objects.isNull(status)) {
					LOGGER.info("Bot Id  Is NOT Null.");
					templateCount = this.templateRepository.countByTemplateUserIdAndBotId(templateUserId, botId,
							rcsMsgTypeId);
					templateList = this.templateRepository.findAllByUserIdAndBotId((Pageable) pageRequest,
							templateUserId, botId, rcsMsgTypeId);
				}
				if (Objects.isNull(templateCode) && Objects.isNull(status) && Objects.isNull(botId)) {
					LOGGER.info("TemplaCode And Status Is Null.");
					templateCount = this.templateRepository.countByTemplateUserIdWithDateRange(from + " 00:00:00",
							to + " 23:59:59", templateUserId, rcsMsgTypeId);
					templateList = this.templateRepository.findAllByUserId(from + " 00:00:00", to + " 23:59:59",
							(Pageable) pageRequest, templateUserId, rcsMsgTypeId);
				}
				if (Objects.isNull(templateCode) && Objects.nonNull(status) && downloadStatus.equalsIgnoreCase("D")) {
					LOGGER.info("Status  Is  NOT Null.");
					templateCount = this.templateRepository.countByTemplateUserIdAndStatus(templateUserId, status,
							rcsMsgTypeId);
					templateList = this.templateRepository.findAllByUserIdAndStatusWithoutPageAble(templateUserId,
							status, rcsMsgTypeId);
				}
			}
			if (Objects.nonNull(templateList)) {
				result.put("status", HttpStatus.OK);
				result.put("template", templateList);
				result.put("totalCount", Long.valueOf(templateCount));
				result.put("message", "Record Founded.");
				return new ResponseEntity(result, HttpStatus.OK);
			}
			result.put("status", HttpStatus.NOT_FOUND);
			result.put("message", "Record Not Founded.");
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			result.put("status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
			result.put("message", "GOT Exception");
			e.printStackTrace();
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping({ "/getAllTemplateName" })
	public ResponseEntity<HashMap<String, Object>> getAllTemplateName(@RequestParam("rcsMsgTypeId") String rcsMsgTypeId,
			@RequestParam("templateUserId") Long templateUserId) throws Exception {
		LOGGER.info("Inside Template Controller");
		HashMap<String, Object> result = new HashMap<>();
		List<Template> templateList = null;
		List<HashMap<String, Object>> templateNameList = new ArrayList<>();
		/*
		 * if (rcsMsgTypeId.equalsIgnoreCase("1")) {
		 * LOGGER.info("***** Getting All Template ****** "); templateList =
		 * this.templateRepository.findListByTemplateByUserId(templateUserId); } else {
		 */
		templateList = this.templateRepository.findAllByTemplateUserIdAndStatusAndRcsMsgTypeId(templateUserId,
				Integer.valueOf(1), rcsMsgTypeId);
		// }
		if (templateList.size() > 0) {
			for (Template tmp : templateList) {
				HashMap<String, Object> templateName = new HashMap<>();
				templateName.put("id", tmp.getId());
				templateName.put("templateCode", tmp.getTemplateCode());
				templateName.put("botId", tmp.getBotId());
				templateNameList.add(templateName);
			}
			result.put("status", HttpStatus.OK);
			result.put("template", templateNameList);
			result.put("message", "Record Founded.");
			return new ResponseEntity(result, HttpStatus.OK);
		}
		result.put("status", HttpStatus.NOT_FOUND);
		result.put("message", "Record Not Founded.");
		return new ResponseEntity(result, HttpStatus.NOT_FOUND);
	}

	@GetMapping({ "/getAllTemplateNameAndIdWithDateFilter" })
	public ResponseEntity<HashMap<String, Object>> getAllTemplateNameAndIdWithDateFilter(
			@RequestParam("rcsMsgTypeId") String rcsMsgTypeId, @RequestParam("from") String from,
			@RequestParam("to") String to, @RequestParam("templateUserId") Long templateUserId) throws Exception {
		LOGGER.info("Inside Template Controller getAllTemplateNameAndIdWithDateFilter");
		HashMap<String, Object> result = new HashMap<>();
		List<Template> templateList = null;
		if (rcsMsgTypeId.equalsIgnoreCase("0")) {
			LOGGER.info("***** Getting All Template ****** ");
			templateList = this.templateRepository.findListByTemplateByUserId(templateUserId);
		} else {
			LOGGER.info("***** Getting All Template Based On RcsMsgTypeId ****** ");
			templateList = this.templateRepository.findAllByTemplateUserId(from + " 00:00:00", to + " 23:59:59",
					templateUserId, rcsMsgTypeId);
		}
		if (templateList.size() > 0) {
			List<HashMap<String, Object>> templateNameList = new ArrayList<>();
			for (Template tmp : templateList) {
				HashMap<String, Object> templateName = new HashMap<>();
				templateName.put("id", tmp.getId());
				templateName.put("templateCode", tmp.getTemplateCode());
				templateNameList.add(templateName);
			}
			result.put("status", HttpStatus.OK);
			result.put("template", templateNameList);
			result.put("message", "Record Founded.");
			return new ResponseEntity(result, HttpStatus.OK);
		}
		result.put("status", HttpStatus.NOT_FOUND);
		result.put("message", "Record Not Founded.");
		return new ResponseEntity(result, HttpStatus.NOT_FOUND);
	}

	@GetMapping({ "/files" })
	public ResponseEntity<List<ResponseFile>> getListFiles() {
		List<ResponseFile> files = (List<ResponseFile>) this.templateFileDbRepository.findAll().stream().map(dbFile -> {
			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/files/")
					.path(String.valueOf(dbFile.getId())).toUriString();
			return new ResponseFile(dbFile.getName(), fileDownloadUri, dbFile.getType(), (dbFile.getData()).length);
		}).collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(files);
	}

	@GetMapping({ "getFilesbyId" })
	public ResponseEntity<byte[]> getFile(@RequestParam("id") Long id) {
		TemplateFileDB fileDB = this.templateFileDbRepository.findById(id).get();
		return ((ResponseEntity.BodyBuilder) ResponseEntity.ok().header("Content-Disposition",
				new String[] { "attachment; filename=\"" + fileDB.getName() + "\"" })).body(fileDB.getData());
	}

	public ResponseEntity<HashMap<String, Object>> sendTemplateToOperator(UserBotMappingEntity userBotMappingEntity,
			OperatorApiConfigEntity operatorApiConfigEntity, Template template, MultipartFile[] files,
			List<String> fileNames) {
		LOGGER.info("*****Ready to Send Template Json  To Operator ******");
		HashMap<String, Object> result = new HashMap<>();
		ResponseEntity<String> templateResponse = null;
		TokenPojo pojo = null;
		OperatorApiConfigEntity operatorApiConForToken = null;
		try {
			LOGGER.info("Basic Token :: " + userBotMappingEntity.getBasicToken());
			// this.botTokenAPIService.getTokenFromClientAPI(this.templateBasicToken),
			// TokenPojo.class
			operatorApiConForToken = operatorApiConfigRepository.findByApiTypeAndTemplateTypeAndOperatorId("TOKEN",
					operatorRepository.findByOperatorName(userBotMappingEntity.getOperator()).getOperatorId());

			pojo = (TokenPojo) (new Gson())
					.fromJson(this.botTokenAPIService.getTokenFromClientAPI(operatorApiConForToken.getApiUrl(),
							operatorApiConfigEntity.getBasicToken() != null ? operatorApiConfigEntity.getBasicToken()
									: userBotMappingEntity.getBasicToken()),
							TokenPojo.class);
			if (pojo != null) {
				LOGGER.info("TOKEN :: " + pojo.getAccess_token());
				templateResponse = this.botTokenAPIService.sendTemplateToOperator(operatorApiConfigEntity,
						template.getBotId(), pojo.getAccess_token(), template, files, fileNames);
				if (templateResponse != null && templateResponse.getStatusCodeValue() == 202
						|| templateResponse.getStatusCodeValue() == 200) {
					LOGGER.info("OPERATOR PUSHED TEMPLATE RESPONSE  :: " + (String) templateResponse.getBody());
					result.put("status", HttpStatus.OK);
					result.put("template", templateResponse.getBody());
					result.put("message", "Template Pushed To Operator Successfully.");
					LOGGER.info("***** Template Json Pushed To Operator ******" + result.toString());
					Integer updatePushedTemplateResponse = Integer.valueOf(0);
					updatePushedTemplateResponse = this.templateRepository
							.updateTemplatePushedResponseByTemplateId(template.getId());
					if (updatePushedTemplateResponse.intValue() == 1) {
						LOGGER.info("***** Pushed Template Response Updated. ******");
					} else {
						LOGGER.info("***** Pushed Template Response Not Updated. ******");
					}
					return new ResponseEntity(result, HttpStatus.CREATED);
				}
				result.put("status", HttpStatus.NOT_FOUND);
				result.put("template", templateResponse.getBody());
				result.put("message", "Template Json Not Pushed To Operator.");
				LOGGER.info("***** Template Json Not Pushed To Operator ******" + result.toString());
				return new ResponseEntity(result, HttpStatus.NOT_FOUND);
			}
			result.put("status", Integer.valueOf(400));
			result.put("message", "Token Not Created.");
			LOGGER.info("***** Token Not Created ******");
			return new ResponseEntity(result, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			LOGGER.info("***** Operator Given Error ******" + result.toString());
			e.printStackTrace();
			result.put("status", Integer.valueOf(500));
			result.put("message", e.getMessage());
			e.printStackTrace();
			return new ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unused")
//	@Scheduled(cron = "0 */1 * * * *")
	public void checkTempalteStatusFromOperator() {
		LOGGER.info("*****Template Approval Schedular Task Started ******");

		HashMap<String, Object> result = new HashMap<>();

		TokenPojo pojo = null;
		List<Template> templateList = null;
		OperatorApiConfigEntity operatorApiConfigEntity = null;
		UserBotMappingEntity userBotMappingEntity = null;
		try {
			templateList = templateRepository.findPendingTemplateForApprovelFromOperator();
			LOGGER.info("*****Got Size For Approve Template ******" + templateList.size());

			if (templateList.size() > 0) {
				for (Template template : templateList) {
					operatorApiConfigEntity = operatorApiConfigRepository.findByApiTypeAndTemplateTypeAndOperatorId(
							"TOKEN", operatorRepository.findByOperatorName(template.getOperator()).getOperatorId());

					if (Objects.nonNull(operatorApiConfigEntity)) {
						pojo = new Gson()
								.fromJson(botTokenAPIService.getTokenFromClientAPI(operatorApiConfigEntity.getApiUrl(),
										operatorApiConfigEntity.getBasicToken() != null
												? operatorApiConfigEntity.getBasicToken()
												: userBotMappingRepository.findById(template.getUserBotMappingId())
														.getBasicToken()),
										TokenPojo.class);
						LOGGER.info("***** Token Got It ::******" + pojo.getAccess_token());

						if (pojo != null) {
							operatorApiConfigEntity = operatorApiConfigRepository
									.findByApiTypeAndTemplateTypeAndOperatorId("STATUS", operatorRepository
											.findByOperatorName(template.getOperator()).getOperatorId());

							ResponseEntity<String> templateResponse = botTokenAPIService.findTemplateStatusFromOperator(
									operatorApiConfigEntity, pojo.getAccess_token(),
									Base64.getEncoder().encodeToString(template.getTemplateCode().getBytes()),
									template.getBotId());
							// Base64.getEncoder().encodeToString(template.getTemplateCode().getBytes()),
							
							if (templateResponse != null && templateResponse.getBody().contains("approved")) {
								LOGGER.info("***** GOING ON UPATING APPROVED OPERATOR STATUS ::"
										+ templateResponse.getBody());

								JSONObject responseObj = new JSONObject(templateResponse.getBody());

								JSONObject templateModel = null;
								String code = null;
								if (responseObj.has("templateModel")) {

									templateModel = responseObj.getJSONObject("templateModel");
									code = templateModel.optString("status");

								}
								if (responseObj.has("templateStatus")) {
									
									code = responseObj.optString("templateStatus");

								}
									LOGGER.info("**** RESPONSE GOT IT FROM findTemplateStatusFromOperator() ::"
											+ templateResponse);

									result.put(STATUS, HttpStatus.OK);
									result.put("template", templateResponse);
									result.put(MESSAGE, "Template Pused To Operator For Approvel");
									LOGGER.info("***** Template Pused To Operator For Approvel  ******");

									Integer updateStatus = templateRepository.updateStatusByTemplateId(
											templateResponse.getBody(), code, template.getId());
									if (updateStatus == 1) {
										LOGGER.info("***** Approve Template Status Updated. ******");

									} else {
										LOGGER.info("***** Approve Template Status Not Updated. ******");

									}
								}
							 else {
								LOGGER.info("***** GOING ON UPATING OPERATOR STATUS ::" + templateResponse.getBody());
								JSONObject responseObj = new JSONObject(templateResponse.getBody());

								//if (responseObj.has("templateModel")) {

									JSONObject templateModel = null;
									String code = null;
									if (responseObj.has("templateModel")) {

										templateModel = responseObj.getJSONObject("templateModel");
										code = templateModel.optString("status");

									}
									if (responseObj.has("templateStatus")) {
										
										code = responseObj.optString("templateStatus");

									}
									Integer updateStatus = templateRepository.rejectStatusByTemplateId(
											templateResponse.getBody(), code, template.getId());
									if (updateStatus == 1) {
										LOGGER.info("***** REJECT STATUS UPATED ******");

									} else {
										LOGGER.info("***** REJECT STATUS NOT UPATED ******");

									}
								//}
								result.put(STATUS, HttpStatus.NOT_FOUND);
								result.put("template", template);
								result.put(MESSAGE, "Approve Template Status Not Updated");
								LOGGER.info("***** Approve Template Status Not Updated ******");
							}
						} else {
							result.put(STATUS, HttpStatus.NOT_FOUND);
							result.put(MESSAGE, "Token Not Created.");
							LOGGER.info("***** Token Not Created ******");
						}
					} else {
						result.put(STATUS, HttpStatus.NOT_FOUND);
						result.put(MESSAGE, "STATUS API Configuration Doesn't Exists.");
						LOGGER.info("***** STATUS API Configuration Doesn't Exists. ******");
					}
				}
			}
		}catch(

	Exception e)
	{

		result.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
		result.put(MESSAGE, e.getMessage());
		LOGGER.info("***** Operator Given Error " + result.toString());

		e.printStackTrace();
	}LOGGER.info("*****Template Approval Schedular Task Done ******");

	}

	public Response sss(String botId, String viAuthorization, Template templateJson, MultipartFile[] files)
			throws IOException {
		OkHttpClient client = (new OkHttpClient()).newBuilder().build();
		MediaType mediaType = MediaType.parse("text/plain");
		MultipartBody multipartBody = (new MultipartBody.Builder()).setType(MultipartBody.FORM)
				.addFormDataPart("rich_template_data", templateJson.getTemplateJson())
				.addFormDataPart("multimedia_files", "/C:/Users/rahul/Downloads/banner.jpg")
				.addFormDataPart("multimedia_files", "/C:/Users/rahul/Downloads/banners.jpg", RequestBody.create(
						MediaType.parse("application/octet-stream"), new File("/C:/Users/rahul/Downloads/banners.jpg")))
				.build();
		Request request = (new Request.Builder())
				.url("https://virbm.in/directory/secure/api/v1/bots/f7u6t8vnIvM43ely/templates")
				.method("POST", (RequestBody) multipartBody).addHeader("Authorization", "Bearer " + viAuthorization)
				.build();
		Response response = client.newCall(request).execute();
		response.code();
		return response;
	}
}
