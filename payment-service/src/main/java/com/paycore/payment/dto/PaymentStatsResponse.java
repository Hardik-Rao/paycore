package com.paycore.payment.dto;

import com.paycore.common.PaymentStatus;

import java.math.BigDecimal;

public record PaymentStatsResponse(
        long totalPayments,
        long successCount,
        long failedCount,
        double successRate,
        BigDecimal totalVolume,
        BigDecimal averageAmount
) {}
