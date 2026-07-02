package com.paycore.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status,
        String errorCode,
        String message,
        String correlationId,
        Instant timestamp
) {
    public static ApiErrorResponse of(int status, String errorCode, String message, String correlationId) {
        return new ApiErrorResponse(status, errorCode, message, correlationId, Instant.now());
    }
}
