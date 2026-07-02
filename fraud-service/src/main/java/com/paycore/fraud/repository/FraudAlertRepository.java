package com.paycore.fraud.repository;

import com.paycore.fraud.domain.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {
    Page<FraudAlert> findByResolutionIsNull(Pageable pageable);
    Page<FraudAlert> findByResolutionIsNotNull(Pageable pageable);
    long countByResolutionIsNull();
}
