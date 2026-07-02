package com.paycore.fraud.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "fraud_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudRule {
    @Id private UUID id;
    @Column(name = "rule_name", nullable = false) private String ruleName;
    @Column(name = "rule_type", nullable = false) private String ruleType;
    private BigDecimal threshold;
    @Column(name = "window_seconds") private Integer windowSeconds;
    @Column(nullable = false) private String action;
    @Column(name = "is_active", nullable = false) private boolean active;
}
