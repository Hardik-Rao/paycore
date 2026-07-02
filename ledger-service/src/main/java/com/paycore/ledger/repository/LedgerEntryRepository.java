package com.paycore.ledger.repository;

import com.paycore.ledger.domain.EntryType;
import com.paycore.ledger.domain.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByPaymentId(UUID paymentId);

    @Query("""
            SELECT e FROM LedgerEntry e
            JOIN AccountBalance a ON e.accountId = a.accountId
            WHERE a.vpa = :vpa
              AND (:from IS NULL OR e.createdAt >= :from)
              AND (:to IS NULL OR e.createdAt <= :to)
              AND (:type IS NULL OR e.entryType = :type)
            ORDER BY e.createdAt DESC
            """)
    Page<LedgerEntry> findStatement(
            @Param("vpa") String vpa,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("type") EntryType type,
            Pageable pageable);

    @Query("""
            SELECT e FROM LedgerEntry e
            JOIN AccountBalance a ON e.accountId = a.accountId
            WHERE a.vpa = :vpa
            ORDER BY e.createdAt DESC
            """)
    List<LedgerEntry> findTop10ByVpa(@Param("vpa") String vpa, Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN e.entryType = 'DEBIT' THEN e.amount ELSE 0 END), 0) FROM LedgerEntry e WHERE e.accountId = :accountId")
    java.math.BigDecimal sumDebits(@Param("accountId") UUID accountId);

    @Query("SELECT COALESCE(SUM(CASE WHEN e.entryType = 'CREDIT' THEN e.amount ELSE 0 END), 0) FROM LedgerEntry e WHERE e.accountId = :accountId")
    java.math.BigDecimal sumCredits(@Param("accountId") UUID accountId);
}
