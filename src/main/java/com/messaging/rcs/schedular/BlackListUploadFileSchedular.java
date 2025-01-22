package com.messaging.rcs.schedular;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.BlackListUploadFile;
import com.messaging.rcs.domain.LeadFileStorage;
import com.messaging.rcs.email.repository.BlackListUploadFileRepository;
import com.messaging.rcs.exceptions.FileUploadException;
import com.messaging.rcs.service.BlacklistService;
import com.messaging.rcs.service.LeadServiceImpl;

@Service
public class BlackListUploadFileSchedular {

	@Autowired
	private BlacklistService blacklistService;
	@Autowired
	private BlackListUploadFileRepository blackListUploadFileRepository;
	private static final Logger LOGGER = Logger.getLogger(BlackListUploadFileSchedular.class.getName());

	//@Scheduled(cron = "0 */1 * * * *")
	public void uploadBlackListFile() throws FileUploadException, IOException {
		LOGGER.info("***** uploadBlackListFile Started *****");

		List<BlackListUploadFile> blackListUploadFileList = null;
		blackListUploadFileList = blackListUploadFileRepository.findByIsExecute(0);
		synchronized (this) {
			if (blackListUploadFileList.size() > 0) {
				for (BlackListUploadFile c : blackListUploadFileList) {
					blackListUploadFileRepository.updateIsExecuteById(c.getId());

				}
				for (BlackListUploadFile blackListUploadFile : blackListUploadFileList) {
					blacklistService.saveBlackListCSV(convert(blackListUploadFile), blackListUploadFile.getUserId());
				}
			}
		}
		LOGGER.info("***** uploadBlackListFile Ended *****");

	}

	public File convert(BlackListUploadFile leadFileStorage) throws IOException {
		File convFile = new File(leadFileStorage.getName());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(leadFileStorage.getData());
		fos.close();

		return convFile;
	}
}
