package com.paycore.ledger.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        String month,
        BigDecimal totalDebits,
        BigDecimal totalCredits,
        int entryCount,
        BigDecimal closingBalance
) {}
