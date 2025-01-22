package com.messaging.rcs.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.BlackListEntity;

@Repository
public interface BlackListRepository extends JpaRepository<BlackListEntity,Long> {

    @Query(value = "select bl.phone_number from black_list bl where bl.user_id =:userId  and bl.phone_number =:phoneNumber   limit 1 ", nativeQuery = true)
    String isBlackList(@Param("phoneNumber") String phoneNumber,@Param("userId") long userId);

    List<BlackListEntity> getAllByPhoneNumberIn(List<String> numbers);
    List<BlackListEntity> getAllByUserIdAndPhoneNumberIn(Long userId, List<String> numbers);
    BlackListEntity findByUserIdAndPhoneNumber(Long userId, String numbers);
    List<BlackListEntity> findByUserId(Long userId);
    void deleteById(Long blacklistId);

}
