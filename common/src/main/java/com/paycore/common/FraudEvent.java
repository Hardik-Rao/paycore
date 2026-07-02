package com.paycore.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FraudEvent(
        UUID paymentId,
        int fraudScore,
        String action,
        List<String> triggeredRules,
        Instant evaluatedAt
) {
}
