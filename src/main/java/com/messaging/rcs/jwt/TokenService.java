package com.messaging.rcs.jwt;

/**
 * 
 * @author Rahul 2023-04-10
 *
 */
public interface TokenService {

    void removeToken(String userName);
    void saveToken(String userName, String jwt);
    String getToken(String userName);
    String getToken(Long userId);
}
