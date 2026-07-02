package com.paycore.payment.dto;

import com.paycore.payment.domain.AccountType;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String vpa,
        String accountHolder,
        AccountType accountType,
        String kycStatus,
        boolean active,
        Instant createdAt
) {}
