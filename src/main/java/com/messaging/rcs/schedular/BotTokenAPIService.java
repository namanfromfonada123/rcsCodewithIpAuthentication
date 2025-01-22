package com.messaging.rcs.schedular;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.http.NameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.messaging.rcs.domain.OperatorApiConfigEntity;
import com.messaging.rcs.model.RcsMsisdnRequestPojo;
import com.messaging.rcs.model.Template;

/**
 * createdOn:: 2023-04-01
 * 
 * @author Rahul
 *
 */
@Service
public class BotTokenAPIService {
	Logger log = LoggerFactory.getLogger(BotTokenAPIService.class);

	/*
	 * @Value("${vi.authorization}") public String viAuthorization;
	 */ @Value("${vi.tokenApi}")
	public String viTokenApi;
	@Value("${vi.grant_type}")
	public String viGrant_type;
	@Value("${vi.botAPI}")
	public String viBotAPI;
	@Value("${approved.template.status.api}")
	private String approvedTempalteStatusApi;
	@Value("${vi.checkRcsMsisdnApi}")
	public String checkRcsMsisdnApi;
	@Value("${operator.template.api}")
	public String operatorTemplateApi;
	@Value("${template.path}")
	private String templatePath;

	private final String boundary = "";
	private static final String LINE = "\r\n";
	private HttpURLConnection httpConn;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;

	/**
	 * 
	 * @param viAuthorization
	 * @return
	 */
	public String getTokenFromClientAPI(String TokenApi, String viAuthorization) {

		String output = "";
		try {

			URL url = new URL(TokenApi);
			System.out.println("URL used for authentication is ::" + url+"\n And BasicToken "+viAuthorization);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", viAuthorization);
			conn.setRequestProperty("grant_type", viGrant_type);

			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			// os.write(JsonData.toString().getBytes());
			os.flush();
			if (conn.getResponseCode() != 200) {

				log.info("Error Response Got From Client URI Service.");

				InputStreamReader in = new InputStreamReader(conn.getErrorStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
				JSONObject responseObj = new JSONObject(output);
				JSONObject response = responseObj.getJSONObject("response");
				String errors = response.optString("errors");

				log.info("Error Message From Client API" + errors);
				return output;
			} else {
				log.info("Preparing Request for Sending To Client API");
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
			}
			conn.disconnect();

		} catch (IOException | RuntimeException e) {
			log.info("Exception in NetClientGet:- " + e);
		}
		return output;
	}

	public String sendMessageByMSISDToClientAPI(String JsonData, String token, String botId) {
		log.info("**** Token :::+> " + token);
		String output = "";
		try {

			URL url = new URL(viBotAPI.replace("{botId}", botId));
			log.info("URL used for authentication viBotAPI is ::" + url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("authorization", "Bearer " + token);

			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			os.write(JsonData.getBytes());
			os.flush();

			if (conn.getResponseCode() != 200) {

				log.info("Error Response Got From Client URI Service.");

				InputStreamReader in = new InputStreamReader(conn.getErrorStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop; // System.out.println("JSON is :::" + output);

				}
				JSONObject responseObj = new JSONObject(output);
				JSONObject response = responseObj.getJSONObject("response");
				String errors = response.optString("errors");

				log.info("Error Message From Client API" + errors);
				return output;
			} else {

				log.info("Preparing Request for Sending To Client API");
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
			}
			log.info("******* Reponse ****** =>" + output);
			conn.disconnect();

		} catch (IOException | RuntimeException e) {
			log.info("Exception in NetClientGet:- " + e);
			return output;
		}
		return output;
	}

	/**
	 * 
	 * @param jsonData
	 * @param authorization
	 * @param msisdn
	 * @param botId
	 * @return
	 */
	public String checkRcsNumber(String authorization, String msisdn, String botId) {
		RcsMsisdnRequestPojo rcsMsisdn = new RcsMsisdnRequestPojo();
		List<String> addMsisdn = new ArrayList<>();
		String output = "";
		TokenPojo pojo = null;
		boolean isRcs = false;

		try {
			if (authorization != null && msisdn != null) {
				// addMsisdn.add(msisdn);
				// rcsMsisdn.setUsers(addMsisdn);

				// = new Gson().fromJson(getTokenFromClientAPI(authorization), TokenPojo.class);
				// log.info("Token Rcs ::=> " + authorization);
				/*
				 * if (pojo.getAccess_token() != null || !pojo.getAccess_token().equals("") ||
				 * !pojo.getAccess_token().isEmpty()) {
				 */
				String rcsURl = checkRcsMsisdnApi + msisdn;
				// if (authorization != null) {
				URL url = new URL(rcsURl.replace("{botId}", botId));
				// URL url = new URL(checkRcsMsisdnApi);

				log.info("URL used for authentication viBotAPI is ::" + url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Authorization", "Bearer " + authorization);

				conn.setDoOutput(true);

				/*
				 * List<NameValuePair> params = new ArrayList<NameValuePair>(); params.add(new
				 * BasicNameValuePair("userContact", msisdn));
				 * 
				 * OutputStream os = conn.getOutputStream(); BufferedWriter writer = new
				 * BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				 * writer.write(getQuery(params)); writer.flush(); writer.close(); os.close();
				 */
				conn.connect();

				String outputloop = "";

				if (conn.getResponseCode() != 200) {

					log.info("Error Response Got From Client URI Service.");

					InputStreamReader in = new InputStreamReader(conn.getErrorStream());
					BufferedReader br = new BufferedReader(in);
					output = "";
					while ((outputloop = br.readLine()) != null) {
						output = output + outputloop; // System.out.println("JSON is :::" + output);

					}
					// JSONObject responseObj = new JSONObject(output);
					// JSONObject response = responseObj.getJSONObject("response");
					// String errors = response.optString("errors");

					log.info("Error Message From Client API" + output);
				} else {

					log.info("Preparing Request for Sending To Client API");
					InputStreamReader in = new InputStreamReader(conn.getInputStream());
					BufferedReader br = new BufferedReader(in);
					while ((outputloop = br.readLine()) != null) {
						output = output + outputloop;
						log.info("******* Success Reponse ****** =>" + output);

						// System.out.println("JSON is :::" + output);

					}
					isRcs = true;
				}
				/*
				 * while ((outputloop = br.readLine()) != null) { output = output + outputloop;
				 * 
				 * } log.info("******* Reponse ****** =>" + output);
				 */
				conn.disconnect();
				/*
				 * } else { log.info("***** TOKEN GOT EMPTY *****"); }
				 */
			} else {
				log.info("***** MSISDN OR TOKEN CANNOT BE EMPTY IN USER *****");
			}

		} catch (IOException | RuntimeException e) {
			log.info("Exception in NetClientGet:- " + e);

		}
		return output;

	}

	public String checkRcsBulkNumber(String authorization, List<String> JsonMnoist, String botId) {
		RestTemplate restTemplate = new RestTemplate();
		@SuppressWarnings("unused")
		ObjectMapper objectMapper = new ObjectMapper();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(authorization);
		headers.setAccessControlRequestMethod(HttpMethod.POST);

		JSONObject requestJson = new JSONObject();
		requestJson.put("users", new Gson().toJson(JsonMnoist).toString());
		String rcsURl = checkRcsMsisdnApi;
		HttpEntity<String> request = new HttpEntity<String>(requestJson.toString(), headers);
		log.info("URL For Bulk Request:: " + rcsURl.replace("{botId}", botId) + "/HTTP HEADERS::" + request.toString());
		ResponseEntity<String> rcsBulkResponse = restTemplate.exchange(rcsURl.replace("{botId}", botId),
				HttpMethod.POST, request, String.class);

		log.info(rcsBulkResponse.getBody());
		return rcsBulkResponse.getBody();
		// JsonNode root = objectMapper.readTree(personResultAsJsonStr);
	}

	public String checkBulkRcsNumber(String authorization, List<String> JsonMnoist, String botId)
			throws InterruptedException {
		String output = "";
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		map.put("users", JsonMnoist);
		String JsonData = new Gson().toJson(map).toString();
		log.info("JSON Request ::" + JsonMnoist.size());
		try {
			// Thread.sleep(30000);
			if (authorization != null) {
				String rcsURl = checkRcsMsisdnApi;
				URL url = new URL(rcsURl.replace("{botId}", botId));

				log.info("URL used for authentication viBotAPI is ::" + url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Authorization", "bearer " + authorization);

				conn.setDoOutput(true);

				// Send post request
				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				wr.writeBytes(JsonData);
				wr.flush();
				wr.close();

				/*
				 * OutputStream os = conn.getOutputStream(); os.write(JsonData.getBytes());
				 * os.flush();
				 */

				String outputloop = "";

				/*
				 * if (conn.getResponseCode() >= 200) {
				 * 
				 * log.info("Error Response Got From Client URI Service.");
				 * 
				 * InputStreamReader in = new InputStreamReader(conn.getErrorStream());
				 * BufferedReader br = new BufferedReader(in); output = ""; while ((outputloop =
				 * br.readLine()) != null) { output = output + outputloop; //
				 * System.out.println("JSON is :::" + output);
				 * 
				 * } log.info("\nError Message From Client API" + output); } else {
				 */

				log.info("Preparing Request for Sending To Client API");
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;

				}
				// }

				conn.disconnect();

			} else {
				log.info("***** MSISDN OR TOKEN CANNOT BE EMPTY IN USER *****");
			}

		} catch (IOException | RuntimeException e) {
			// Thread.sleep(30000);
			log.info("After Got e.getLocalizedMessage() " + e.getLocalizedMessage());
			log.info("After Got e.getMessage() " + e.getMessage());

			log.info("Inside if (e.getLocalizedMessage().contains(429))" + e.getMessage());
			return checkBulkRcsNumber(authorization, JsonMnoist, botId);
			// "{\r\n" + "\"rcsEnabledContacts\": [\r\n" + "\"429\"]\r\n" + "}";
		}
		return output;

	}

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	/**
	 * 
	 * @param viAuthorization
	 * @param templateJson
	 * @return
	 * @throws IOException
	 */

	public ResponseEntity<String> sendTemplateToOperator(OperatorApiConfigEntity operatorApiConfigEntity, String botId,
			String viAuthorization, Template templateJson, MultipartFile[] files,List<String> fileNames) throws IOException {
		System.out.println(
				"URL used for authentication is ::" + operatorApiConfigEntity.getApiUrl().replace("{botId}", botId));
		System.out.println("BOT ID:: " + botId);
		System.out.println("AUTHORIZATION :: " + viAuthorization);
		System.out.println("TEMPLATE JSON ::" + templateJson.getTemplateJson());
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("rich_template_data", templateJson.getTemplateJson());

		if (templateJson.getTemplateMsgType().equalsIgnoreCase("rich_card")
				|| templateJson.getTemplateMsgType().equalsIgnoreCase("carousel")) {
			if (Objects.nonNull(files)) {
				for (MultipartFile file : files) {
					System.out.println("Adding File In Bytes Inside multimedia_files Param ::" + file.getOriginalFilename());

					body.add("multimedia_files", new FileSystemResource(templatePath + file.getOriginalFilename()));
				}
			} else {
				for (String file : fileNames) {
					System.out.println("Text  File In Bytes Inside multimedia_files Param ::" + file);

					body.add("multimedia_files", file);
				}
			}
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setBearerAuth(viAuthorization);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		return restTemplate.exchange(operatorApiConfigEntity.getApiUrl().replace("{botId}", botId), HttpMethod.POST,
				requestEntity, String.class);
	}

	public String sendTemplateToOperatorssss(String botId, String viAuthorization, String templateJson) {

		String output = "";
		try {

			URL url = new URL(operatorTemplateApi.replace("{botId}", botId));
			System.out.println("URL used for authentication is ::" + url);
			System.out.println("BOT ID:: " + botId);
			System.out.println("AUTHORIZATION :: " + viAuthorization);
			System.out.println("TEMPLATE JSON ::" + templateJson);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "multipart/form-data");
			conn.setRequestProperty("Authorization", "Bearer " + viAuthorization);
			conn.setRequestProperty("rich_template_data", templateJson);

			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			// os.write(JsonData.toString().getBytes());
			os.flush();
			if (conn.getResponseCode() != 200) {

				log.info("Error Response Got From Client URI Service.");

				InputStreamReader in = new InputStreamReader(conn.getErrorStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
				JSONObject responseObj = new JSONObject(output);
				JSONObject response = responseObj.getJSONObject("response");
				String errors = response.optString("errors");

				log.info("Error Message From Client API" + errors);
				return output;
			} else {
				log.info("Preparing Request for Sending To Client API");
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
			}
			conn.disconnect();

		} catch (IOException | RuntimeException e) {
			log.info("Exception in NetClientGet:- " + e);
		}
		return output;
	}

	public ResponseEntity<String> findTemplateStatusFromOperator(OperatorApiConfigEntity operatorApiConfigEntity,
			String viAuthorization, String templateName, String botId) {
		System.out.println("URL used for authentication is ::"
				+ operatorApiConfigEntity.getApiUrl().replace("{botId}", botId).replace("{name}", templateName));
		// + approvedTempalteStatusApi.replace("{botId}", botId).replace("{name}",
		// templateName));
		System.out.println("BOT ID:: " + botId);
		System.out.println("AUTHORIZATION :: " + viAuthorization);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(viAuthorization);
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		return restTemplate.exchange(
				operatorApiConfigEntity.getApiUrl().replace("{botId}", botId).replace("{name}", templateName),
				HttpMethod.GET, entity, String.class);
	}

	public String findTemplateStatusFromOperatorBackUp(String viAuthorization, String templateName, String botId) {

		String output = "";
		try {

			URL url = new URL(approvedTempalteStatusApi.replace("{botId}", botId).replace("{name}", templateName));
			System.out.println("URL used for authentication is ::" + url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			// conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + viAuthorization);

			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			// os.write(JsonData.toString().getBytes());
			os.flush();
			if (conn.getResponseCode() != 200) {

				log.info("Error Response Got From Client URI Service.");

				InputStreamReader in = new InputStreamReader(conn.getErrorStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
				JSONObject responseObj = new JSONObject(output);
				JSONObject response = responseObj.getJSONObject("response");
				String errors = response.optString("errors");

				log.info("Error Message From Client API" + errors);
				return output;
			} else {
				log.info("Preparing Request for Sending To Client API");
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
			}
			conn.disconnect();

		} catch (IOException | RuntimeException e) {
			log.info("Exception in NetClientGet:- " + e);
		}
		return output;
	}

	/**
	 * 
	 * @param viAuthorization
	 * @return
	 */
	public String getTokenFromClientAPI(String viAuthorization) {

		String output = "";
		try {

			URL url = new URL(viTokenApi);
			System.out.println("URL used for authentication is ::" + url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", viAuthorization);
			conn.setRequestProperty("grant_type", viGrant_type);

			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			// os.write(JsonData.toString().getBytes());
			os.flush();
			if (conn.getResponseCode() != 200) {

				log.info("Error Response Got From Client URI Service.");

				InputStreamReader in = new InputStreamReader(conn.getErrorStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
				JSONObject responseObj = new JSONObject(output);
				JSONObject response = responseObj.getJSONObject("response");
				String errors = response.optString("errors");

				log.info("Error Message From Client API" + errors);
				return output;
			} else {
				log.info("Preparing Request for Sending To Client API");
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				output = "";
				String outputloop = "";
				while ((outputloop = br.readLine()) != null) {
					output = output + outputloop;
					// System.out.println("JSON is :::" + output);

				}
			}
			conn.disconnect();

		} catch (IOException | RuntimeException e) {
			log.info("Exception in NetClientGet:- " + e);
		}
		return output;
	}

	/*
	 * public void HttpPostMultipart(String requestURL, String charset, Map<String,
	 * String> headers) throws IOException {
	 * 
	 * this.charset = charset; boundary = UUID.randomUUID().toString(); URL url =
	 * new URL(requestURL); httpConn = (HttpURLConnection) url.openConnection();
	 * httpConn.setUseCaches(false); httpConn.setDoOutput(true); // indicates POST
	 * method httpConn.setDoInput(true); httpConn.setRequestProperty("Content-Type",
	 * "multipart/form-data; boundary=" + boundary); if (headers != null &&
	 * headers.size() > 0) { Iterator<String> it = headers.keySet().iterator();
	 * while (it.hasNext()) { String key = it.next(); String value =
	 * headers.get(key); httpConn.setRequestProperty(key, value); } } outputStream =
	 * httpConn.getOutputStream(); writer = new PrintWriter(new
	 * OutputStreamWriter(outputStream, charset), true); }
	 */
}
