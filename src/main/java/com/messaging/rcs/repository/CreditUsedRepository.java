package com.messaging.rcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.CreditUsedEntity;

@Repository
public interface CreditUsedRepository extends JpaRepository<CreditUsedEntity, Long> {

//	@Query(value="SELECT 1 * FROM credit_used ORDER BY anyField DESC LIMIT 1", nativeQuery = true)
	CreditUsedEntity findTopByUserIdOrderByCrdIdDesc(String userId);
	CreditUsedEntity findByCrdId(Long crdId);

}
