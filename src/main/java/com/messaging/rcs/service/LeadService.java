package com.messaging.rcs.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.messaging.rcs.domain.LeadInfoEntity;
import com.messaging.rcs.model.LeadInfo;

/**
 * 
 * @author RahulRajput
 *
 */
public interface LeadService {
	boolean uploadAllowed(LeadInfo leadInfo);

	boolean uploadAllowed(int leadId, MultipartFile file) throws Exception;

	boolean uploadAllowed(LeadInfo leadInfo, MultipartFile file) throws Exception;

	LeadInfoEntity saveLeadWithDnDCheck2(LeadInfo leadInfo, boolean isDND) throws Exception;

	LeadInfo save(int Id, MultipartFile file, boolean isDND, boolean isDuplicate) throws Exception;

	LeadInfo save(LeadInfo leadInfo, MultipartFile file, boolean isDND, boolean isDuplicate) throws Exception;

	void updateAttemptInfo(LeadInfo returnedLead);

	void processTestNumber(String phoneNumber);

	// public List<LeadInfo> getAllLeadInfoByUserId(Long userId) throws Exception;

	public List<LeadInfo> getAllLeadInfoByUserIdAndDateBetween(String starteDate, String endDate, Long userId,
			Integer start, Integer limit, String campaignId, String leadId) throws Exception;

	public boolean checkIfNameExists(String leadName);

	LeadInfoEntity rcsAPIsaveLeadWithDnDCheck3(LeadInfo leadInfo, boolean isDND,String checkRcs) throws Exception;

}
