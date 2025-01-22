package com.messaging.rcs.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.model.User;

/**
 * 
 * @author RahulRajput
 *
 */
public interface UserService {

	UserEntity createUser(User user, MultipartFile companyBanner, MultipartFile companyLogo) throws Exception;

	UserEntity updateUser(User user,MultipartFile companyBanner,MultipartFile companyLogo);

	void deleteUser(Long id) throws Exception;

	UserEntity getUser(Long userId);

	void deactivateUser(Long id,String active);

	List<UserEntity> getAdminUser() throws Exception;

	List<User> getAllUsers() throws InvocationTargetException, IllegalAccessException;

	UserEntity login(User user);
	// String getLoggedInUserName();
	// Long getLoggedInUserId();

	boolean isUserIdFromBodyChildOfLoggedInUser(long possibleParentId, long userIdFromBody);
}
