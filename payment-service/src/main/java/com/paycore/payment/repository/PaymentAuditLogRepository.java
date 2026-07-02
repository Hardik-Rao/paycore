package com.paycore.payment.repository;

import com.paycore.payment.domain.PaymentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, UUID> {
    List<PaymentAuditLog> findByPaymentIdOrderByChangedAtAsc(UUID paymentId);
}
