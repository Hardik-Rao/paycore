package com.paycore.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record ReversePaymentRequest(@NotBlank String reason) {}
