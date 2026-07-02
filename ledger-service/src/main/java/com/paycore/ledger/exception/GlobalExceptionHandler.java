package com.paycore.ledger.exception;

import com.paycore.common.ApiErrorResponse;
import com.paycore.common.CorrelationHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LedgerException.class)
    public ResponseEntity<ApiErrorResponse> handle(LedgerException ex, HttpServletRequest request) {
        HttpStatus status = "NOT_FOUND".equals(ex.getErrorCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(
                ApiErrorResponse.of(status.value(), ex.getErrorCode(), ex.getMessage(), correlationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(
                ApiErrorResponse.of(500, "INTERNAL_ERROR", ex.getMessage(), correlationId(request)));
    }

    private String correlationId(HttpServletRequest request) {
        String id = request.getHeader(CorrelationHeaders.CORRELATION_ID);
        return id != null ? id : "unknown";
    }
}
