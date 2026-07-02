package com.paycore.fraud.repository;

import com.paycore.fraud.domain.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FraudRuleRepository extends JpaRepository<FraudRule, UUID> {
    List<FraudRule> findByActiveTrue();
}
