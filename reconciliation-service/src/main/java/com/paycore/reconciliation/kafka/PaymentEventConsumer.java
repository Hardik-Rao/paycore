package com.paycore.reconciliation.kafka;

import com.paycore.common.KafkaTopics;
import com.paycore.common.PaymentEvent;
import com.paycore.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class PaymentEventConsumer {
    private final ReconciliationService reconciliationService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "reconciliation-service")
    public void consume(PaymentEvent event) {
        reconciliationService.trackPayment(event);
    }
}
