package com.paycore.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentEvent(
        UUID id,
        String idempotencyKey,
        String payerVpa,
        String payeeVpa,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Integer fraudScore,
        String failureReason,
        String reversalReason,
        Map<String, Object> metadata,
        Instant initiatedAt,
        Instant processedAt,
        Instant updatedAt,
        String eventType
) {
}
