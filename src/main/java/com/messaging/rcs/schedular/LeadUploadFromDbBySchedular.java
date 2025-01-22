package com.messaging.rcs.schedular;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.LeadFileStorage;
import com.messaging.rcs.repository.LeadFileStorageRepository;
import com.messaging.rcs.service.LeadServiceImpl;

@Service
public class LeadUploadFromDbBySchedular {

	@Autowired
	private LeadFileStorageRepository leadFileStorageRepository;
	@Autowired
	private LeadServiceImpl leadServiceImpl;
	private static final Logger LOGGER = Logger.getLogger(LeadServiceImpl.class.getName());

	//@Scheduled(cron = "0 */1 * * * *")
	public void getFileFromDbByLeadId() throws IOException, Exception {
		LOGGER.info("***** LeadUploadFromDbBySchedular Started *****");

		List<LeadFileStorage> leadFileList = null;
		synchronized (this) {
			leadFileList = leadFileStorageRepository.findByIsSchedule(0);

			LOGGER.info("***** LeadUploadFromDbBySchedular Size() ***** ::" + leadFileList.size());

			if (leadFileList.size() > 0) {
				for (LeadFileStorage lfs : leadFileList) {
					leadFileStorageRepository.updateById(lfs.getLead_file_id());
					LOGGER.info("***** LeadUploadFromDbBySchedular Size() ***** ::" + leadFileList.size());

				}

				for (LeadFileStorage leadFileStorage : leadFileList) {
					leadServiceImpl.createLeadInfoDetailAfterUploadFileFromDB(leadFileStorage.getLeadId().intValue(),
							convert(leadFileStorage), leadFileStorage.isDnd(), leadFileStorage.isDuplicate());

				}
			}

			LOGGER.info("***** LeadUploadFromDbBySchedular Ended *****");
		}
	}

	public File convert(LeadFileStorage leadFileStorage) throws IOException {
		File convFile = new File(leadFileStorage.getLeadFileName());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(leadFileStorage.getData());
		fos.close();

		return convFile;
	}
}
