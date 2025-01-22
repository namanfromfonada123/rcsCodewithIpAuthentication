package com.messaging.rcs.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.messaging.rcs.domain.BlackListEntity;
import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.exceptions.FileUploadException;
import com.messaging.rcs.jwt.Constants;
import com.messaging.rcs.model.DataContainer;
import com.messaging.rcs.repository.BlackListRepository;
import com.messaging.rcs.util.CSVHelper;
import com.messaging.rcs.util.CSVUtils;
import com.messaging.rcs.util.MobileNumberValidation;
import com.messaging.rcs.util.StringUtil;

@Service
public class BlacklistService {
	@Autowired
	RedisTemplate<String, BlackListEntity> blacklistTemplate;
	@Autowired
	private BlackListRepository blacklistrepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistService.class);

	/**
	 * 
	 * @param s
	 * @return
	 */
	/**
	 * This method is useful for save data in redis after save db based on mno and
	 * clientId like:: 12345679_15
	 * 
	 * @param blackList
	 */
	public void saveBlackListInRedisCacheByMsisdn(BlackListEntity blackList) {

		blacklistTemplate.opsForValue().set(blackList.getPhoneNumber() + "_" + blackList.getUserId(),
				blackList);
	}

	public DataContainer createBlackListNo(BlackListEntity s) {
		LOGGER.info("***** Inside BlacklistdbServicesaveBlackListNo");

		DataContainer data = new DataContainer();
		BlackListEntity blackList = null;
		BlackListEntity existBlackList = null;
		UserEntity user = null;
		try {
			if (Objects.isNull(s.getUserId())) {
				data.setMsg("User Id Required.");
				data.setStatus(404);
				return data;
			}
			if (Objects.nonNull(s.getPhoneNumber())) {
				existBlackList = blacklistrepository.findByUserIdAndPhoneNumber(s.getUserId(), s.getPhoneNumber());
				if (Objects.isNull(existBlackList)) {
					s.setCreateDtm(new Date());
					s.setPrefix(Integer.valueOf(s.getPhoneNumber().substring(0, 3)));

					blackList = blacklistrepository.save(s);
					saveBlackListInRedisCacheByMsisdn(blackList);
					data.setData(blackList);
					data.setMsg(Constants.SUCCESSADD_MSG);
					data.setStatus(Constants.REQUEST_SUCCESS);
					LOGGER.info("***** Black List No Saved::" + data.toString());
				} else {
					data.setData(existBlackList);
					data.setMsg(Constants.RECORD_ALREADY_EXISTS);
					data.setStatus(Constants.RECORD_EXISTS);
				}

			} else {
				data.setMsg("Mobile No Required.");
				data.setStatus(404);
				LOGGER.info("***** BlackList User Not Found::" + data.toString());

			}
		} catch (Exception e) {
			LOGGER.info("Got Exception::" + e.getMessage());
			data.setMsg(e.getMessage());
			e.printStackTrace();
		}
		return data;

	}

	@Async("blackListUploadFileExecutor")
	public CompletableFuture<Map<String, String>> saveBlackListCSV(File file, Long userId) throws FileUploadException {

		List<List<String>> dataList = new ArrayList<>();
		List<String[]> csvData;
		List<List<String>> excelData = null;
		String response = "";
		Map<String, String> responseList = new HashMap<String, String>();

		try {
			if ("csv".equals(file.getName().substring(file.getName().lastIndexOf('.') + 1))) {
				csvData = CSVHelper.convertToCSVStringList(new FileInputStream(file));
				dataList = parseCsvFile(csvData);
				response = CSVHelper.validateHeader(csvData.get(0), Constants.SMS_BLACKLIST_COLUMN_LENGTH);
			} else {
				excelData = CSVUtils.excelToStringList(file, Constants.SMS_BLACKLIST_COLUMN_LENGTH);
				response = CSVUtils.validateExcel(excelData, Constants.SMS_BLACKLIST_COLUMN_LENGTH);
				dataList = parseExcelFile(excelData);
			}
			if (response.contains(Constants.SUCCESS_MSG)) {
				if (dataList.size() > 0) {
					responseList.put("Status", Constants.SUCCESS_MSG);
					return CompletableFuture.completedFuture(createBlackListByUploadFile(dataList, file, userId));
				}
			} else {
				responseList.put("Status", response);

			}

		} catch (Exception ee) {
			responseList.put("Got Exception", ee.getMessage());

			ee.printStackTrace();
			// throw new RuntimeException("Fail To Store CSV Data: " + ee.getMessage());
		}
		return CompletableFuture.completedFuture(responseList);

	}

	private List<List<String>> parseCsvFile(List<String[]> csvData) {
		return csvData.stream().map(row -> new ArrayList<>(Arrays.asList(row)))
				.filter(dataSet -> Boolean.FALSE.equals(dataSet.stream().allMatch(StringUtil::isBlank)))
				.collect(Collectors.toList());
	}

	public Map<String, String> createBlackListByUploadFile(List<List<String>> dataList, File file, Long userId) {
		LOGGER.info("***** Inside BlacklistdbService.createBlackListByUploadFile() *****");
		long startTime = System.currentTimeMillis();
		BlackListEntity saveList = null;
		BlackListEntity black = null;
		Map<String, String> response = new HashMap<String, String>();
		int rowNumber = 0;
		BlackListEntity existBlackList = null;

		List<BlackListEntity> blackList = new ArrayList<BlackListEntity>();
		try {

			for (List<String> row : dataList) {
				if (rowNumber == 0) {
					row.removeAll(Collections.singleton(""));
					rowNumber++;
				} else {

					if (StringUtil.isNotBlank(row.get(0).trim())) {
						black = new BlackListEntity();

						if (MobileNumberValidation.isValidMobileNo(row.get(0).trim())) {
							black.setPhoneNumber(row.get(0).trim());
							black.setPrefix(Integer.valueOf(black.getPhoneNumber().substring(0, 3)));
							black.setCreateDtm(new Date());

							if (StringUtil.isNotBlank(String.valueOf(userId))) {
								black.setUserId(userId);
							}
							// response.put(row.get(0), Constants.BLACKLIST_MOBILE_VALIDATOR_MSG);
							blackList.add(black);

						} else {
							// response.put(row.get(0), Constants.BLACKLIST_MOBILE_INVALID_MSG);
						}

					}

				}
			}
			if (blackList.size() > 0) {
				LOGGER.info("Going To Save Record For BlackList No:: " + blackList.size());
				// blacklistrepository.saveAll(blackList);
				for (BlackListEntity blkList : blackList) {
					existBlackList = blacklistrepository.findByUserIdAndPhoneNumber(blkList.getUserId(),
							blkList.getPhoneNumber());
					if (Objects.isNull(existBlackList)) {

						saveList = blacklistrepository.save(blkList);
						// response.put(saveList.getPhoneNumber(), "Record Saved.");
					} else {
						// response.put(blkList.getPhoneNumber(), "Record Already Exist ::"+);

					}
				}
				LOGGER.info("BlackList Upload Successfully Added And Execution Time ::"
						+ (System.currentTimeMillis() - startTime));

				response.put("Success", "BlackList Upload Successfully Added.");

			}
		} catch (Exception e) {
			response.put("Got Exception", e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	public static <T> List<T> getListFromIterator(Iterator<T> iterator) {
		Iterable<T> iterable = () -> iterator;

		return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
	}

	/*
	 * public ByteArrayInputStream load() { List<BlackListEntity> incomingDid =
	 * blacklistrepository.findAll(); ByteArrayInputStream in =
	 * CSVHelper.writeInCSV(incomingDid); return in; }
	 */

	public List<BlackListEntity> getBlackList() {
		return blacklistrepository.findAll();
	}

	private List<List<String>> parseExcelFile(List<List<String>> csvData) {
		return csvData.stream().filter(dataSet -> Boolean.FALSE.equals(dataSet.stream().allMatch(StringUtil::isBlank)))
				.collect(Collectors.toList());
	}

	public DataContainer findByMsisdnAndClientid(String msisdn, Long userId) {
		LOGGER.info("***** Inside BlacklistdbService.findByMsisdnAndClientid()");
		DataContainer data = new DataContainer();
		try {
			BlackListEntity blackListEntity = blacklistrepository.findByUserIdAndPhoneNumber(userId, msisdn);
			if (blackListEntity != null) {
				data.setData(blackListEntity);
				data.setMsg("Record Founded.");
				data.setStatus(200);
			} else {
				data.setMsg("Record Not Found.");
				data.setStatus(404);
			}
		} catch (Exception e) {
			e.printStackTrace();
			BlackListEntity blacklist = new BlackListEntity();
			blacklist.setId(0L);
			blacklist.setPhoneNumber(String.valueOf(msisdn) != null ? String.valueOf(msisdn) : "0");
			blacklist.setUserId(userId);
			data.setData(blacklist);
			data.setMsg(e.getMessage());
			data.setStatus(500);

		}
		return data;
	}

	public File convert(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}
}
