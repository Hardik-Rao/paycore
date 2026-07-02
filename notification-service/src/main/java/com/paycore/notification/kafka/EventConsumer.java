package com.paycore.notification.kafka;

import com.paycore.common.FraudEvent;
import com.paycore.common.KafkaTopics;
import com.paycore.common.PaymentEvent;
import com.paycore.notification.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component @RequiredArgsConstructor
public class EventConsumer {
    private final WebhookService webhookService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "notification-service")
    public void onPayment(PaymentEvent event) {
        webhookService.handleEvent("payment." + event.status().name().toLowerCase(),
                event.id(), Map.of("paymentId", event.id().toString(), "status", event.status().name(),
                        "amount", event.amount(), "payerVpa", event.payerVpa()));
    }

    @KafkaListener(topics = KafkaTopics.FRAUD_EVENTS, groupId = "notification-service-fraud")
    public void onFraud(FraudEvent event) {
        webhookService.handleEvent("fraud.evaluated", event.paymentId(),
                Map.of("paymentId", event.paymentId().toString(), "score", event.fraudScore(), "action", event.action()));
    }
}
