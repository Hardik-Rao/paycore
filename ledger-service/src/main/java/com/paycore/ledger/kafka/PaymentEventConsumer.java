package com.paycore.ledger.kafka;

import com.paycore.common.KafkaTopics;
import com.paycore.common.PaymentEvent;
import com.paycore.common.PaymentStatus;
import com.paycore.ledger.service.LedgerProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final LedgerProcessingService ledgerProcessingService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "ledger-service")
    public void consume(PaymentEvent event) {
        if (event.status() == PaymentStatus.SUCCESS || event.status() == PaymentStatus.REVERSED) {
            log.info("Processing ledger for payment {} status {}", event.id(), event.status());
            ledgerProcessingService.processPaymentEvent(event);
        }
    }
}
