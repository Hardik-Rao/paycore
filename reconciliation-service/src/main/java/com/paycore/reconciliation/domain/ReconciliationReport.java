package com.paycore.reconciliation.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "reconciliation_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReconciliationReport {
    @Id private UUID id;
    @Column(name = "report_date", nullable = false, unique = true) private LocalDate reportDate;
    @Column(name = "total_payments", nullable = false) private long totalPayments;
    @Column(name = "total_volume", nullable = false) private BigDecimal totalVolume;
    @Column(name = "success_count", nullable = false) private long successCount;
    @Column(name = "failed_count", nullable = false) private long failedCount;
    @Column(name = "reversed_count", nullable = false) private long reversedCount;
    @Column(name = "fraud_blocked_count", nullable = false) private long fraudBlockedCount;
    @Column(name = "mismatch_count", nullable = false) private long mismatchCount;
    @Column(nullable = false) private String status;
    @Column(name = "generated_at", nullable = false) private Instant generatedAt;
}
