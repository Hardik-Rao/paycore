package com.paycore.reconciliation.repository;

import com.paycore.reconciliation.domain.ReconciliationMismatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReconciliationMismatchRepository extends JpaRepository<ReconciliationMismatch, UUID> {
    List<ReconciliationMismatch> findByResolvedAtIsNull();
}
