package com.paycore.notification.repository;

import com.paycore.notification.domain.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {
    Page<WebhookDelivery> findByStatus(String status, Pageable pageable);
    @Query("SELECT d FROM WebhookDelivery d WHERE d.status = 'PENDING' AND d.nextRetryAt <= :now")
    List<WebhookDelivery> findDueRetries(Instant now);
    long countByStatus(String status);
}
