package com.paycore.fraud.service;

import com.paycore.common.FraudEvent;
import com.paycore.common.KafkaTopics;
import com.paycore.common.PaymentEvent;
import com.paycore.fraud.domain.*;
import com.paycore.fraud.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FraudEvaluationService {

    private final FraudRuleRepository ruleRepository;
    private final FraudAlertRepository alertRepository;
    private final PaymentEvaluationRepository evaluationRepository;
    private final DeviceFingerprintRepository deviceRepository;
    private final StringRedisTemplate redis;
    private final KafkaTemplate<String, FraudEvent> kafka;
    private final MeterRegistry meterRegistry;

    @Transactional
    public EvaluationResult evaluate(PaymentEvent event) {
        if (evaluationRepository.existsById(event.id())) {
            PaymentEvaluation existing = evaluationRepository.findById(event.id()).orElseThrow();
            return new EvaluationResult(event.id(), existing.getFraudScore(), existing.getAction());
        }

        List<String> triggered = new ArrayList<>();
        int score = 0;
        for (FraudRule rule : ruleRepository.findByActiveTrue()) {
            if (matches(rule, event)) {
                triggered.add(rule.getRuleName());
                score += scoreFor(rule.getRuleType());
            }
        }
        score = Math.min(score, 100);
        String action = score <= 40 ? "AUTO_APPROVE" : score <= 70 ? "REVIEW" : "AUTO_BLOCK";

        meterRegistry.summary("fraud_score_distribution").record(score);

        evaluationRepository.save(PaymentEvaluation.builder()
                .paymentId(event.id()).fraudScore(score).action(action).evaluatedAt(Instant.now()).build());

        FraudEvent fraudEvent = new FraudEvent(event.id(), score, action, triggered, Instant.now());
        kafka.send(KafkaTopics.FRAUD_EVENTS, event.id().toString(), fraudEvent);
        if ("REVIEW".equals(action) || "AUTO_BLOCK".equals(action)) {
            kafka.send(KafkaTopics.FRAUD_ALERTS, event.id().toString(), fraudEvent);
            alertRepository.save(FraudAlert.builder()
                    .id(UUID.randomUUID()).paymentId(event.id())
                    .fraudScore(score).triggeredRules(triggered).actionTaken(action)
                    .createdAt(Instant.now()).build());
        }
        trackDevice(event);
        return new EvaluationResult(event.id(), score, action);
    }

    private boolean matches(FraudRule rule, PaymentEvent event) {
        return switch (rule.getRuleType()) {
            case "VELOCITY" -> {
                String key = "velocity:" + event.payerVpa();
                Long count = redis.opsForValue().increment(key);
                if (count != null && count == 1) redis.expire(key, java.time.Duration.ofSeconds(rule.getWindowSeconds()));
                yield count != null && count >= rule.getThreshold().intValue();
            }
            case "AMOUNT_THRESHOLD" -> event.amount().compareTo(rule.getThreshold()) > 0;
            case "NEW_DEVICE" -> {
                String hash = deviceHash(event);
                boolean known = deviceRepository.findByAccountIdAndDeviceHash(event.payerVpa(), hash).isPresent();
                yield !known && event.amount().compareTo(rule.getThreshold()) > 0;
            }
            case "NIGHT_LARGE" -> {
                LocalTime t = event.initiatedAt().atZone(ZoneId.of("Asia/Kolkata")).toLocalTime();
                boolean night = t.isAfter(LocalTime.of(23, 0)) || t.isBefore(LocalTime.of(5, 0));
                yield night && event.amount().compareTo(rule.getThreshold()) > 0;
            }
            case "ROUND_AMOUNT" -> event.amount().remainder(BigDecimal.valueOf(10000)).compareTo(BigDecimal.ZERO) == 0
                    && event.amount().compareTo(BigDecimal.valueOf(10000)) >= 0;
            case "INACTIVE_ACCOUNT" -> {
                String key = "last_payment:" + event.payerVpa();
                String last = redis.opsForValue().get(key);
                redis.opsForValue().set(key, Instant.now().toString());
                yield last == null;
            }
            default -> false;
        };
    }

    private int scoreFor(String type) {
        return switch (type) {
            case "VELOCITY" -> 40;
            case "AMOUNT_THRESHOLD" -> 30;
            case "NEW_DEVICE" -> 25;
            case "NIGHT_LARGE" -> 20;
            case "ROUND_AMOUNT" -> 10;
            case "INACTIVE_ACCOUNT" -> 15;
            default -> 0;
        };
    }

    private String deviceHash(PaymentEvent event) {
        Object device = event.metadata() != null ? event.metadata().get("deviceId") : null;
        return device != null ? device.toString() : "unknown";
    }

    private void trackDevice(PaymentEvent event) {
        String hash = deviceHash(event);
        deviceRepository.findByAccountIdAndDeviceHash(event.payerVpa(), hash).ifPresentOrElse(fp -> {
            fp.setLastSeen(Instant.now());
            fp.setPaymentCount(fp.getPaymentCount() + 1);
            deviceRepository.save(fp);
        }, () -> deviceRepository.save(DeviceFingerprint.builder()
                .id(UUID.randomUUID()).accountId(event.payerVpa()).deviceHash(hash)
                .firstSeen(Instant.now()).lastSeen(Instant.now()).paymentCount(1).trusted(false).build()));
    }

    public record EvaluationResult(UUID paymentId, int fraudScore, String action) {}
}
