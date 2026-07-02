package com.paycore.payment.repository;

import com.paycore.common.PaymentStatus;
import com.paycore.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT p FROM Payment p
            WHERE (:payer IS NULL OR p.payerVpa = :payer)
              AND (:status IS NULL OR p.status = :status)
              AND (:from IS NULL OR p.initiatedAt >= :from)
              AND (:to IS NULL OR p.initiatedAt <= :to)
            """)
    Page<Payment> findWithFilters(
            @Param("payer") String payer,
            @Param("status") PaymentStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    List<Payment> findByPayerVpaOrPayeeVpa(String payerVpa, String payeeVpa);

    @Query("""
            SELECT COUNT(p) FROM Payment p
            WHERE p.status = :status
              AND p.initiatedAt BETWEEN :from AND :to
            """)
    long countByStatusAndPeriod(
            @Param("status") PaymentStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.initiatedAt BETWEEN :from AND :to
            """)
    BigDecimal sumAmountBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COALESCE(AVG(p.amount), 0) FROM Payment p
            WHERE p.status = 'SUCCESS' AND p.initiatedAt BETWEEN :from AND :to
            """)
    BigDecimal avgSuccessAmountBetween(@Param("from") Instant from, @Param("to") Instant to);
}
