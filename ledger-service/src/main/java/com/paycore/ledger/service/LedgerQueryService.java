package com.paycore.ledger.service;

import com.paycore.ledger.domain.DailyLedgerSummary;
import com.paycore.ledger.domain.EntryType;
import com.paycore.ledger.domain.LedgerEntry;
import com.paycore.ledger.dto.*;
import com.paycore.ledger.exception.LedgerException;
import com.paycore.ledger.repository.AccountBalanceRepository;
import com.paycore.ledger.repository.DailyLedgerSummaryRepository;
import com.paycore.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerQueryService {

    private final AccountBalanceRepository accountBalanceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final DailyLedgerSummaryRepository dailySummaryRepository;

    public BalanceResponse getBalance(String vpa) {
        var account = accountBalanceRepository.findByVpa(vpa)
                .orElseThrow(() -> new LedgerException("NOT_FOUND", "Account not found"));
        return new BalanceResponse(vpa, account.getBalance(), account.getLastUpdated());
    }

    public Page<LedgerEntryResponse> getStatement(String vpa, Instant from, Instant to, EntryType type, Pageable pageable) {
        ensureAccount(vpa);
        return ledgerEntryRepository.findStatement(vpa, from, to, type, pageable).map(this::toResponse);
    }

    public String exportStatement(String vpa, Instant from, Instant to, EntryType type) {
        Page<LedgerEntryResponse> entries = getStatement(vpa, from, to, type, PageRequest.of(0, 10000));
        StringWriter writer = new StringWriter();
        writer.append("id,paymentId,type,amount,currency,balanceAfter,description,createdAt\n");
        for (LedgerEntryResponse e : entries) {
            writer.append(String.format("%s,%s,%s,%s,%s,%s,\"%s\",%s%n",
                    e.id(), e.paymentId(), e.entryType(), e.amount(), e.currency(),
                    e.balanceAfter(), e.description(), e.createdAt()));
        }
        return writer.toString();
    }

    public List<LedgerEntryResponse> getByPaymentId(UUID paymentId) {
        return ledgerEntryRepository.findByPaymentId(paymentId).stream().map(this::toResponse).toList();
    }

    public MonthlySummaryResponse getMonthlySummary(String vpa, String month) {
        ensureAccount(vpa);
        YearMonth ym = YearMonth.parse(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        var account = accountBalanceRepository.findByVpa(vpa).orElseThrow();
        List<DailyLedgerSummary> summaries = dailySummaryRepository.findByVpaAndDateRange(vpa, from, to);
        BigDecimal debits = summaries.stream().map(DailyLedgerSummary::getTotalDebits).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credits = summaries.stream().map(DailyLedgerSummary::getTotalCredits).reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = summaries.stream().mapToInt(DailyLedgerSummary::getEntryCount).sum();
        return new MonthlySummaryResponse(month, debits, credits, count, account.getBalance());
    }

    public List<DailySummaryResponse> getDailySummary(String vpa) {
        ensureAccount(vpa);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30);
        return dailySummaryRepository.findByVpaAndDateRange(vpa, from, to).stream()
                .map(s -> new DailySummaryResponse(
                        s.getSummaryDate(), s.getOpeningBalance(), s.getClosingBalance(),
                        s.getTotalDebits(), s.getTotalCredits(), s.getEntryCount()))
                .toList();
    }

    public List<LedgerEntryResponse> miniStatement(String vpa) {
        ensureAccount(vpa);
        return ledgerEntryRepository.findTop10ByVpa(vpa, PageRequest.of(0, 10)).stream()
                .map(this::toResponse).toList();
    }

    private void ensureAccount(String vpa) {
        if (!accountBalanceRepository.findByVpa(vpa).isPresent()) {
            throw new LedgerException("NOT_FOUND", "Account not found");
        }
    }

    private LedgerEntryResponse toResponse(LedgerEntry e) {
        return new LedgerEntryResponse(
                e.getId(), e.getPaymentId(), e.getEntryType(), e.getAmount(),
                e.getCurrency(), e.getBalanceAfter(), e.getDescription(), e.getCreatedAt());
    }
}
