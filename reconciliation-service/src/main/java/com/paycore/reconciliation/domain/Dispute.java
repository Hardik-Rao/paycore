package com.paycore.reconciliation.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name = "disputes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dispute {
    @Id private UUID id;
    @Column(name = "payment_id", nullable = false) private UUID paymentId;
    @Column(name = "raised_by", nullable = false) private String raisedBy;
    @Column(nullable = false) private String reason;
    @Column(nullable = false) private String status;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private Map<String, Object> evidence;
    @Column(name = "raised_at", nullable = false) private Instant raisedAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolved_by") private String resolvedBy;
    @Column(name = "resolution_notes") private String resolutionNotes;
    @Column(name = "refund_amount") private BigDecimal refundAmount;
}
