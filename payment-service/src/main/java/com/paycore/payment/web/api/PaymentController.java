package com.paycore.payment.web.api;

import com.paycore.common.PaymentStatus;
import com.paycore.payment.dto.*;
import com.paycore.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse initiate(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.initiatePayment(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable UUID id) {
        return paymentService.getById(id);
    }

    @GetMapping("/idempotency/{key}")
    public PaymentResponse getByIdempotency(@PathVariable String key) {
        return paymentService.getByIdempotencyKey(key);
    }

    @GetMapping
    public Page<PaymentResponse> list(
            @RequestParam(required = false) String payer,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return paymentService.list(payer, status, from, to, pageable);
    }

    @PostMapping("/{id}/reverse")
    public PaymentResponse reverse(@PathVariable UUID id, @Valid @RequestBody ReversePaymentRequest request) {
        return paymentService.reverse(id, request);
    }

    @PostMapping("/{id}/retry")
    public PaymentResponse retry(@PathVariable UUID id) {
        return paymentService.retry(id);
    }

    @GetMapping("/{id}/audit-trail")
    public List<AuditLogResponse> auditTrail(@PathVariable UUID id) {
        return paymentService.auditTrail(id);
    }

    @PostMapping("/{id}/cancel")
    public PaymentResponse cancel(@PathVariable UUID id) {
        return paymentService.cancel(id);
    }

    @GetMapping("/stats")
    public PaymentStatsResponse stats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return paymentService.stats(from, to);
    }
}
