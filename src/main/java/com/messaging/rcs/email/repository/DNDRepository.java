
package com.messaging.rcs.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.messaging.rcs.domain.DNDEntity;

@Repository
public interface DNDRepository extends JpaRepository<DNDEntity, Long> {

	@Query(value = "select bl.f2 from dnd bl where bl.f2 =:phoneNumber limit 1 ", nativeQuery = true)
	String isDNDNo(@Param("phoneNumber") String phoneNumber);

}
