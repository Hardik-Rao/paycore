package com.paycore.fraud.service;

import com.paycore.common.PaymentEvent;
import com.paycore.fraud.domain.FraudAlert;
import com.paycore.fraud.domain.FraudRule;
import com.paycore.fraud.exception.FraudException;
import com.paycore.fraud.repository.FraudAlertRepository;
import com.paycore.fraud.repository.FraudRuleRepository;
import com.paycore.fraud.repository.PaymentEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FraudManagementService {

    private final FraudAlertRepository alertRepository;
    private final FraudRuleRepository ruleRepository;
    private final PaymentEvaluationRepository evaluationRepository;

    public Page<FraudAlert> listAlerts(String status, Pageable pageable) {
        if ("PENDING".equalsIgnoreCase(status)) return alertRepository.findByResolutionIsNull(pageable);
        if ("REVIEWED".equalsIgnoreCase(status)) return alertRepository.findByResolutionIsNotNull(pageable);
        return alertRepository.findAll(pageable);
    }

    public FraudAlert getAlert(UUID id) {
        return alertRepository.findById(id).orElseThrow(() -> new FraudException("NOT_FOUND", "Alert not found"));
    }

    @Transactional
    public FraudAlert review(UUID id, String resolution, String reviewer) {
        FraudAlert alert = getAlert(id);
        alert.setResolution(resolution);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(Instant.now());
        return alertRepository.save(alert);
    }

    public List<FraudRule> listRules() { return ruleRepository.findAll(); }

    @Transactional
    public FraudRule createRule(FraudRule rule) {
        rule.setId(UUID.randomUUID());
        rule.setActive(true);
        return ruleRepository.save(rule);
    }

    @Transactional
    public FraudRule toggleRule(UUID id) {
        FraudRule rule = ruleRepository.findById(id).orElseThrow(() -> new FraudException("NOT_FOUND", "Rule not found"));
        rule.setActive(!rule.isActive());
        return ruleRepository.save(rule);
    }

    public Map<String, Object> riskScore(String vpa) {
        long alerts = alertRepository.countByResolutionIsNull();
        return Map.of("vpa", vpa, "riskScore", Math.min(alerts * 5, 100), "pendingAlerts", alerts);
    }

    public Map<String, Object> stats() {
        long total = evaluationRepository.count();
        long blocked = evaluationRepository.findAll().stream().filter(e -> "AUTO_BLOCK".equals(e.getAction())).count();
        return Map.of("totalEvaluations", total, "blockRate", total == 0 ? 0 : (double) blocked / total,
                "pendingReviews", alertRepository.countByResolutionIsNull());
    }
}
