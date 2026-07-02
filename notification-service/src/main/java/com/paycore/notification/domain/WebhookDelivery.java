package com.paycore.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name = "webhook_deliveries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebhookDelivery {
    @Id private UUID id;
    @Column(name = "subscription_id", nullable = false) private UUID subscriptionId;
    @Column(name = "payment_id") private UUID paymentId;
    @Column(name = "event_type", nullable = false) private String eventType;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false) private Map<String, Object> payload;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private int attempts;
    @Column(name = "last_attempt_at") private Instant lastAttemptAt;
    @Column(name = "next_retry_at") private Instant nextRetryAt;
    @Column(name = "response_status_code") private Integer responseStatusCode;
    @Column(name = "response_body") private String responseBody;
}
