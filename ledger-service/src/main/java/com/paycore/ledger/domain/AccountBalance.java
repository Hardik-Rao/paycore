package com.paycore.ledger.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalance {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(nullable = false, unique = true)
    private String vpa;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Version
    private Long version;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
}
