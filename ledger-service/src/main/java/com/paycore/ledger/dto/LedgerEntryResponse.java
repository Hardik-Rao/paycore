package com.paycore.ledger.dto;

import com.paycore.ledger.domain.EntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id,
        UUID paymentId,
        EntryType entryType,
        BigDecimal amount,
        String currency,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {}
