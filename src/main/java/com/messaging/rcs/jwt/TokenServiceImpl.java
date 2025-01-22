package com.messaging.rcs.jwt;


import java.util.Date;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.repository.UserRepository;

/**
 * 
 * @author Rahul 2023-04-10
 *
 */
@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = Logger.getLogger(TokenServiceImpl.class.getName());
    @Resource
    private TokenRepository tokenRepository;
    @Resource
    private UserRepository userRepository;
    @Autowired
    private CustomUserDetailService userDetailService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private TokenService tokenService;

    @Override
    public void removeToken(String userName) {
        TokenEntity token = tokenRepository.getTokenStoreByUserNameAndIsActive(userName, "Y");
        if(token != null) {
            tokenRepository.delete(token);
            LOGGER.info("Token Expired Successfully");
        }
    }

    @Override
    public void saveToken(String userName, String jwt) {
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUserName(userName);
        tokenEntity.setJwt(jwt);
        tokenEntity.setCreateDtm(new Date());
        tokenEntity.setIsActive("Y");
        tokenRepository.save(tokenEntity);
        LOGGER.info("Token Stored Successfully");
    }

    @Override
    public String getToken(String userName) {
        String jwt = null;
        TokenEntity token = tokenRepository.getTokenStoreByUserNameAndIsActive(userName, "Y");
        if(token != null) {
            jwt = token.getJwt();
        }
        return jwt;
    }

    @Override
    public String getToken(Long userId) {
        UserEntity userEntity = userRepository.getUserEntityByUserId(userId);
        CustomUserDetails userDetails = (CustomUserDetails)userDetailService
                .loadUserByUsername(userEntity.getUserName());
        String jwt = jwtTokenUtil.generateTokenForPasswordReset(userDetails);
        tokenService.saveToken(userDetails.getUsername(), jwt);
        return jwt;
    }
}
