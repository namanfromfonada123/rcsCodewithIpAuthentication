package com.messaging.rcs.jwt;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by sbsingh on Oct/27/2020.
 */
@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {

    TokenEntity getTokenStoreByUserNameAndIsActive(String userName, String isActive);
}
