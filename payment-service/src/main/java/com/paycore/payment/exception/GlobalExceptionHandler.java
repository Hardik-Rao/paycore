package com.paycore.payment.exception;

import com.paycore.common.ApiErrorResponse;
import com.paycore.common.CorrelationHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiErrorResponse> handlePayment(PaymentException ex, HttpServletRequest request) {
        HttpStatus status = mapStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(
                ApiErrorResponse.of(status.value(), ex.getErrorCode(), ex.getMessage(), correlationId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(400, "VALIDATION_ERROR", message, correlationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(
                ApiErrorResponse.of(500, "INTERNAL_ERROR", ex.getMessage(), correlationId(request)));
    }

    private HttpStatus mapStatus(String code) {
        return switch (code) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT", "INVALID_STATE" -> HttpStatus.CONFLICT;
            case "ACCOUNT_INACTIVE", "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "LOCK_FAILED" -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private String correlationId(HttpServletRequest request) {
        String id = request.getHeader(CorrelationHeaders.CORRELATION_ID);
        return id != null ? id : "unknown";
    }
}
