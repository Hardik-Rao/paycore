package com.paycore.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paycore.notification.domain.*;
import com.paycore.notification.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final int[] RETRY_SECONDS = {0, 30, 120, 600, 3600};

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final NotificationLogRepository logRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MeterRegistry meterRegistry;

    @Transactional
    public WebhookSubscription create(WebhookSubscription sub) {
        sub.setId(UUID.randomUUID());
        sub.setActive(true);
        sub.setCreatedAt(Instant.now());
        return subscriptionRepository.save(sub);
    }

    public List<WebhookSubscription> list() { return subscriptionRepository.findAll(); }

    @Transactional
    public void delete(UUID id) { subscriptionRepository.deleteById(id); }

    @Transactional
    public void handleEvent(String eventType, UUID paymentId, Map<String, Object> payload) {
        logNotification(paymentId, "SMS", "simulated", "SENT", null);
        logNotification(paymentId, "EMAIL", "merchant@paycore.io", "SENT", null);

        for (WebhookSubscription sub : subscriptionRepository.findByActiveTrue()) {
            if (sub.getEvents().contains(eventType) || sub.getEvents().contains("*")) {
                WebhookDelivery delivery = WebhookDelivery.builder()
                        .id(UUID.randomUUID()).subscriptionId(sub.getId())
                        .paymentId(paymentId).eventType(eventType).payload(payload)
                        .status("PENDING").attempts(0).nextRetryAt(Instant.now()).build();
                deliveryRepository.save(delivery);
                deliver(delivery, sub);
            }
        }
    }

    public void deliver(WebhookDelivery delivery, WebhookSubscription sub) {
        try {
            String body = objectMapper.writeValueAsString(delivery.getPayload());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-PayCore-Signature", hmac(body, sub.getSecretKey()));
            ResponseEntity<String> response = restTemplate.exchange(
                    sub.getEndpointUrl(), HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            delivery.setAttempts(delivery.getAttempts() + 1);
            delivery.setLastAttemptAt(Instant.now());
            delivery.setResponseStatusCode(response.getStatusCode().value());
            delivery.setResponseBody(response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                delivery.setStatus("DELIVERED");
                delivery.setNextRetryAt(null);
                meterRegistry.gauge("webhook_delivery_success_rate", 1.0);
            } else {
                scheduleRetry(delivery);
            }
        } catch (Exception ex) {
            delivery.setAttempts(delivery.getAttempts() + 1);
            delivery.setLastAttemptAt(Instant.now());
            delivery.setResponseBody(ex.getMessage());
            scheduleRetry(delivery);
        }
        deliveryRepository.save(delivery);
    }

    private void scheduleRetry(WebhookDelivery delivery) {
        if (delivery.getAttempts() >= 5) {
            delivery.setStatus("DEAD");
            logNotification(delivery.getPaymentId(), "EMAIL", "merchant@paycore.io", "FAILED",
                    "Webhook delivery dead after 5 attempts");
            meterRegistry.gauge("webhook_delivery_success_rate", 0.0);
        } else {
            delivery.setStatus("PENDING");
            int delay = RETRY_SECONDS[Math.min(delivery.getAttempts(), RETRY_SECONDS.length - 1)];
            delivery.setNextRetryAt(Instant.now().plusSeconds(delay + new Random().nextInt(5)));
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void retryPending() {
        for (WebhookDelivery d : deliveryRepository.findDueRetries(Instant.now())) {
            subscriptionRepository.findById(d.getSubscriptionId()).ifPresent(s -> deliver(d, s));
        }
    }

    @Transactional
    public WebhookDelivery retry(UUID id) {
        WebhookDelivery d = deliveryRepository.findById(id).orElseThrow();
        WebhookSubscription s = subscriptionRepository.findById(d.getSubscriptionId()).orElseThrow();
        deliver(d, s);
        return d;
    }

    public Map<String, Object> stats() {
        long delivered = deliveryRepository.countByStatus("DELIVERED");
        long total = deliveryRepository.count();
        return Map.of("successRate", total == 0 ? 1.0 : (double) delivered / total,
                "pending", deliveryRepository.countByStatus("PENDING"),
                "dead", deliveryRepository.countByStatus("DEAD"));
    }

    private void logNotification(UUID paymentId, String channel, String recipient, String status, String error) {
        logRepository.save(NotificationLog.builder()
                .id(UUID.randomUUID()).paymentId(paymentId).channel(channel)
                .recipient(recipient).status(status).sentAt(Instant.now()).errorMessage(error).build());
    }

    private String hmac(String body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
