package com.paycore.fraud.kafka;

import com.paycore.common.KafkaTopics;
import com.paycore.common.PaymentEvent;
import com.paycore.common.PaymentStatus;
import com.paycore.fraud.service.FraudEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class PaymentEventConsumer {
    private final FraudEvaluationService evaluationService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "fraud-service")
    public void consume(PaymentEvent event) {
        if (event.status() == PaymentStatus.PROCESSING || event.status() == PaymentStatus.SUCCESS) {
            evaluationService.evaluate(event);
        }
    }
}
