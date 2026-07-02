package com.paycore.reconciliation.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "reconciliation_mismatches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReconciliationMismatch {
    @Id private UUID id;
    @Column(name = "report_id", nullable = false) private UUID reportId;
    @Column(name = "payment_id") private UUID paymentId;
    @Column(name = "mismatch_type", nullable = false) private String mismatchType;
    @Column(name = "expected_value") private String expectedValue;
    @Column(name = "actual_value") private String actualValue;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolution_notes") private String resolutionNotes;
}
