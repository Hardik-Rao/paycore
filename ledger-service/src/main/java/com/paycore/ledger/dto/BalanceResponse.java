package com.paycore.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceResponse(String vpa, BigDecimal balance, Instant lastUpdated) {}
