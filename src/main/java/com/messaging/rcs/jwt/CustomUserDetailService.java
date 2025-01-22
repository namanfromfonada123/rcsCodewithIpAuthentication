package com.messaging.rcs.jwt;


import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.messaging.rcs.domain.UserEntity;
import com.messaging.rcs.repository.UserRepository;

/**
 * 
 * @author Rahul 2023-04-10
 *
 */

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Resource
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        Optional<UserEntity> userEntity =  userRepository.findByUserName(userName);
        userEntity.orElseThrow(() -> new UsernameNotFoundException("Not found: "+userName));
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity.get());
        return customUserDetails;
    }
}
