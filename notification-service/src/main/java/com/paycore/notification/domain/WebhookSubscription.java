package com.paycore.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "webhook_subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebhookSubscription {
    @Id private UUID id;
    @Column(name = "merchant_id", nullable = false) private String merchantId;
    @Column(name = "endpoint_url", nullable = false) private String endpointUrl;
    @Column(name = "secret_key", nullable = false) private String secretKey;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false) private List<String> events;
    @Column(name = "is_active", nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
}
