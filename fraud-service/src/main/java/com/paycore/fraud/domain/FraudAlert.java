package com.paycore.fraud.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "fraud_alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudAlert {
    @Id private UUID id;
    @Column(name = "payment_id", nullable = false) private UUID paymentId;
    @Column(name = "rule_id") private UUID ruleId;
    @Column(name = "fraud_score", nullable = false) private int fraudScore;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "triggered_rules", columnDefinition = "jsonb", nullable = false)
    private List<String> triggeredRules;
    @Column(name = "action_taken", nullable = false) private String actionTaken;
    @Column(name = "reviewed_by") private String reviewedBy;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    private String resolution;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
}
