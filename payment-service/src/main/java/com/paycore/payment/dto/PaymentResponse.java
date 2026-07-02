package com.paycore.payment.dto;

import com.paycore.common.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PaymentResponse(
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
        Instant updatedAt
) {}
