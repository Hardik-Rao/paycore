package com.paycore.payment.kafka;

import com.paycore.common.*;
import com.paycore.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publish(Payment payment, String eventType) {
        PaymentEvent event = toEvent(payment, eventType);
        kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, payment.getId().toString(), event);
        switch (payment.getStatus()) {
            case SUCCESS -> kafkaTemplate.send(KafkaTopics.PAYMENT_SUCCESS, payment.getId().toString(), event);
            case FAILED -> kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, payment.getId().toString(), event);
            case REVERSED -> kafkaTemplate.send(KafkaTopics.PAYMENT_REVERSED, payment.getId().toString(), event);
            default -> { }
        }
        log.info("Published payment event {} for payment {}", eventType, payment.getId());
    }

    private PaymentEvent toEvent(Payment payment, String eventType) {
        return new PaymentEvent(
                payment.getId(),
                payment.getIdempotencyKey(),
                payment.getPayerVpa(),
                payment.getPayeeVpa(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getFraudScore(),
                payment.getFailureReason(),
                payment.getReversalReason(),
                payment.getMetadata(),
                payment.getInitiatedAt(),
                payment.getProcessedAt(),
                payment.getUpdatedAt(),
                eventType
        );
    }
}
