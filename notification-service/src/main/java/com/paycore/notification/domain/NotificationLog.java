package com.paycore.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "notification_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id private UUID id;
    @Column(name = "payment_id") private UUID paymentId;
    @Column(nullable = false) private String channel;
    @Column(nullable = false) private String recipient;
    @Column(nullable = false) private String status;
    @Column(name = "sent_at", nullable = false) private Instant sentAt;
    @Column(name = "error_message") private String errorMessage;
}
