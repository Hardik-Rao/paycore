package com.paycore.ledger.repository;

import com.paycore.ledger.domain.DailyLedgerSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyLedgerSummaryRepository extends JpaRepository<DailyLedgerSummary, UUID> {

    Optional<DailyLedgerSummary> findByAccountIdAndSummaryDate(UUID accountId, LocalDate date);

    @Query("""
            SELECT s FROM DailyLedgerSummary s
            JOIN AccountBalance a ON s.accountId = a.accountId
            WHERE a.vpa = :vpa AND s.summaryDate BETWEEN :from AND :to
            ORDER BY s.summaryDate DESC
            """)
    List<DailyLedgerSummary> findByVpaAndDateRange(
            @Param("vpa") String vpa,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
