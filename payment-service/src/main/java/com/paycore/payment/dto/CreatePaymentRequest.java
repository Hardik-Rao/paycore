package com.paycore.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;

public record CreatePaymentRequest(
        @NotBlank String idempotencyKey,
        @NotBlank String payerVpa,
        @NotBlank String payeeVpa,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency,
        Map<String, Object> metadata
) {}
