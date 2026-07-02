package com.paycore.fraud.web.api;

import com.paycore.common.PaymentEvent;
import com.paycore.fraud.domain.FraudAlert;
import com.paycore.fraud.domain.FraudRule;
import com.paycore.fraud.service.FraudEvaluationService;
import com.paycore.fraud.service.FraudManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/fraud") @RequiredArgsConstructor
public class FraudController {
    private final FraudEvaluationService evaluationService;
    private final FraudManagementService managementService;

    @PostMapping("/evaluate/{paymentId}")
    public FraudEvaluationService.EvaluationResult evaluate(@PathVariable UUID paymentId,
            @RequestBody PaymentEvent event) {
        return evaluationService.evaluate(event);
    }

    @GetMapping("/alerts")
    public Page<FraudAlert> alerts(@RequestParam(required = false) String status, Pageable pageable) {
        return managementService.listAlerts(status, pageable);
    }

    @GetMapping("/alerts/{id}")
    public FraudAlert getAlert(@PathVariable UUID id) { return managementService.getAlert(id); }

    @PostMapping("/alerts/{id}/review")
    public FraudAlert review(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return managementService.review(id, body.get("resolution"), body.getOrDefault("reviewedBy", "admin"));
    }

    @GetMapping("/rules")
    public List<FraudRule> rules() { return managementService.listRules(); }

    @PostMapping("/rules")
    public FraudRule createRule(@RequestBody FraudRule rule) { return managementService.createRule(rule); }

    @PatchMapping("/rules/{id}/toggle")
    public FraudRule toggle(@PathVariable UUID id) { return managementService.toggleRule(id); }

    @GetMapping("/accounts/{vpa}/risk-score")
    public Map<String, Object> risk(@PathVariable String vpa) { return managementService.riskScore(vpa); }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return managementService.stats(); }
}
