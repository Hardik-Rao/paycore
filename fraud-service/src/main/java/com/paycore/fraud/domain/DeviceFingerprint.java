package com.paycore.fraud.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "device_fingerprints")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceFingerprint {
    @Id private UUID id;
    @Column(name = "account_id", nullable = false) private String accountId;
    @Column(name = "device_hash", nullable = false) private String deviceHash;
    @Column(name = "first_seen", nullable = false) private Instant firstSeen;
    @Column(name = "last_seen", nullable = false) private Instant lastSeen;
    @Column(name = "payment_count", nullable = false) private int paymentCount;
    @Column(name = "is_trusted", nullable = false) private boolean trusted;
}
