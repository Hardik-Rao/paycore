package com.paycore.payment.dto;

import com.paycore.common.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        PaymentStatus oldStatus,
        PaymentStatus newStatus,
        String changedBy,
        Instant changedAt,
        String reason
) {}
