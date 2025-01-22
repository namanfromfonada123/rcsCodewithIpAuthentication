package com.messaging.rcs.repository;

import com.messaging.rcs.domain.UserDailyUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

/**
 * Created by sbsingh on Nov/03/2020.
 */
public interface UserDailyUsageRepository extends JpaRepository<UserDailyUsageEntity, Long> {

    UserDailyUsageEntity getUserDailyUsageEntityByUserIdAndUploadDate(Long userId, Date createDate);
}
