package com.paycore.reconciliation.repository;

import com.paycore.reconciliation.domain.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    Page<Dispute> findByStatus(String status, Pageable pageable);
}
