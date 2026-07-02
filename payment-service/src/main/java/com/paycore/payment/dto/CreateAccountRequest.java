package com.paycore.payment.dto;

import com.paycore.payment.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotBlank String vpa,
        @NotBlank String accountHolder,
        @NotNull AccountType accountType
) {}
