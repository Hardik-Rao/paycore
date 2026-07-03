package com.paycore.payment.service;

import com.paycore.common.PaymentStatus;
import com.paycore.payment.domain.*;
import com.paycore.payment.dto.*;
import com.paycore.payment.exception.PaymentException;
import com.paycore.payment.kafka.PaymentEventPublisher;
import com.paycore.payment.repository.AccountRepository;
import com.paycore.payment.repository.PaymentAuditLogRepository;
import com.paycore.payment.repository.PaymentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;
    private final IdempotencyService idempotencyService;
    private final DistributedLockService lockService;
    private final PaymentEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    @Transactional
    public PaymentResponse initiatePayment(CreatePaymentRequest request) {
        return idempotencyService.get(request.idempotencyKey(), PaymentResponse.class)
                .orElseGet(() -> {
                    String lockToken = lockService.acquire(request.idempotencyKey());
                    if (lockToken == null) {
                        throw new PaymentException("LOCK_FAILED", "Could not acquire lock for idempotency key");
                    }
                    try {
                        paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                                .ifPresent(p -> {
                                    throw new PaymentException("CONFLICT", "Payment already exists");
                                });
                        validateAccounts(request.payerVpa(), request.payeeVpa());
                        Timer.Sample sample = Timer.start(meterRegistry);
                        Instant now = Instant.now();
                        Payment payment = Payment.builder()
                                .id(UUID.randomUUID())
                                .idempotencyKey(request.idempotencyKey())
                                .payerVpa(request.payerVpa())
                                .payeeVpa(request.payeeVpa())
                                .amount(request.amount())
                                .currency(request.currency())
                                .status(PaymentStatus.INITIATED)
                                .metadata(request.metadata())
                                .initiatedAt(now)
                                .updatedAt(now)
                                .build();
                        paymentRepository.save(payment);
                        audit(payment.getId(), null, PaymentStatus.INITIATED, "system", "Payment initiated");
                        transition(payment, PaymentStatus.PROCESSING, "system", "Processing payment");
                        boolean success = simulateProcessing(payment);
                        if (success) {
                            payment.setProcessedAt(Instant.now());
                            transition(payment, PaymentStatus.SUCCESS, "system", "Payment successful");
                            recordMetric("SUCCESS");
                        } else {
                            payment.setFailureReason("Processing failed");
                            transition(payment, PaymentStatus.FAILED, "system", "Payment failed");
                            recordMetric("FAILED");
                        }
                        sample.stop(Timer.builder("payment_processing_duration_seconds")
                                .tag("status", payment.getStatus().name())
                                .register(meterRegistry));
                        PaymentResponse response = toResponse(payment);
                        idempotencyService.put(request.idempotencyKey(), response);
                        return response;
                    } finally {
                        lockService.release(request.idempotencyKey(), lockToken);
                    }
                });
    }

    public PaymentResponse getById(UUID id) {
        return paymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Payment not found"));
    }

    public PaymentResponse getByIdempotencyKey(String key) {
        return paymentRepository.findByIdempotencyKey(key)
                .map(this::toResponse)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Payment not found"));
    }

    public Page<PaymentResponse> list(String payer, PaymentStatus status, Instant from, Instant to, Pageable pageable) {
        if (payer == null && status == null && from == null && to == null) {
            return paymentRepository.findAll(pageable).map(this::toResponse);
        }
        return paymentRepository.findWithFilters(payer, status, from, to, pageable).map(this::toResponse);
    }

    @Transactional
    public PaymentResponse reverse(UUID id, ReversePaymentRequest request) {
        Payment payment = findPayment(id);
        transition(payment, PaymentStatus.REVERSED, "system", request.reason());
        payment.setReversalReason(request.reason());
        recordMetric("REVERSED");
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse retry(UUID id) {
        Payment payment = findPayment(id);
        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new PaymentException("INVALID_STATE", "Only failed payments can be retried");
        }
        transition(payment, PaymentStatus.PROCESSING, "system", "Retrying payment");
        payment.setFailureReason(null);
        if (simulateProcessing(payment)) {
            payment.setProcessedAt(Instant.now());
            transition(payment, PaymentStatus.SUCCESS, "system", "Retry successful");
            recordMetric("SUCCESS");
        } else {
            payment.setFailureReason("Processing failed on retry");
            transition(payment, PaymentStatus.FAILED, "system", "Retry failed");
            recordMetric("FAILED");
        }
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse cancel(UUID id) {
        Payment payment = findPayment(id);
        transition(payment, PaymentStatus.CANCELLED, "system", "Payment cancelled");
        recordMetric("CANCELLED");
        return toResponse(payment);
    }

    public List<AuditLogResponse> auditTrail(UUID id) {
        findPayment(id);
        return auditLogRepository.findByPaymentIdOrderByChangedAtAsc(id).stream()
                .map(a -> new AuditLogResponse(a.getId(), a.getOldStatus(), a.getNewStatus(),
                        a.getChangedBy(), a.getChangedAt(), a.getReason()))
                .toList();
    }

    public PaymentStatsResponse stats(Instant from, Instant to) {
        long success = paymentRepository.countByStatusAndPeriod(PaymentStatus.SUCCESS, from, to);
        long failed = paymentRepository.countByStatusAndPeriod(PaymentStatus.FAILED, from, to);
        long total = success + failed + paymentRepository.countByStatusAndPeriod(PaymentStatus.INITIATED, from, to)
                + paymentRepository.countByStatusAndPeriod(PaymentStatus.PROCESSING, from, to)
                + paymentRepository.countByStatusAndPeriod(PaymentStatus.REVERSED, from, to)
                + paymentRepository.countByStatusAndPeriod(PaymentStatus.CANCELLED, from, to);
        double rate = total == 0 ? 0 : (success * 100.0 / total);
        return new PaymentStatsResponse(
                total, success, failed,
                BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                paymentRepository.sumAmountBetween(from, to),
                paymentRepository.avgSuccessAmountBetween(from, to)
        );
    }

    private void validateAccounts(String payerVpa, String payeeVpa) {
        Account payer = accountRepository.findByVpa(payerVpa)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Payer account not found"));
        Account payee = accountRepository.findByVpa(payeeVpa)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Payee account not found"));
        if (!payer.isActive() || !payee.isActive()) {
            throw new PaymentException("ACCOUNT_INACTIVE", "Account is inactive");
        }
    }

    private Payment findPayment(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Payment not found"));
    }

    private void transition(Payment payment, PaymentStatus newStatus, String changedBy, String reason) {
        PaymentStateMachine.validateTransition(payment.getStatus(), newStatus);
        PaymentStatus old = payment.getStatus();
        payment.setStatus(newStatus);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
        audit(payment.getId(), old, newStatus, changedBy, reason);
        eventPublisher.publish(payment, "STATUS_CHANGED");
    }

    private void audit(UUID paymentId, PaymentStatus old, PaymentStatus newStatus, String changedBy, String reason) {
        auditLogRepository.save(PaymentAuditLog.builder()
                .id(UUID.randomUUID())
                .paymentId(paymentId)
                .oldStatus(old)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(Instant.now())
                .reason(reason)
                .build());
    }

    private boolean simulateProcessing(Payment payment) {
        return payment.getAmount().compareTo(new BigDecimal("1000000")) < 0;
    }

    private void recordMetric(String status) {
        meterRegistry.counter("payment_requests_total", "status", status).increment();
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getIdempotencyKey(), p.getPayerVpa(), p.getPayeeVpa(),
                p.getAmount(), p.getCurrency(), p.getStatus(), p.getFraudScore(),
                p.getFailureReason(), p.getReversalReason(), p.getMetadata(),
                p.getInitiatedAt(), p.getProcessedAt(), p.getUpdatedAt()
        );
    }
}
