package com.paycore.fraud.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "payment_evaluations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentEvaluation {
    @Id @Column(name = "payment_id") private UUID paymentId;
    @Column(name = "fraud_score", nullable = false) private int fraudScore;
    @Column(nullable = false) private String action;
    @Column(name = "evaluated_at", nullable = false) private Instant evaluatedAt;
}
