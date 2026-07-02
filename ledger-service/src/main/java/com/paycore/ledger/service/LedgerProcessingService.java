package com.paycore.ledger.service;

import com.paycore.common.PaymentEvent;
import com.paycore.common.PaymentStatus;
import com.paycore.ledger.domain.*;
import com.paycore.ledger.exception.LedgerException;
import com.paycore.ledger.repository.AccountBalanceRepository;
import com.paycore.ledger.repository.DailyLedgerSummaryRepository;
import com.paycore.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerProcessingService {

    private final AccountBalanceRepository accountBalanceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final DailyLedgerSummaryRepository dailySummaryRepository;

    @Transactional
    public void processPaymentEvent(PaymentEvent event) {
        if (ledgerEntryRepository.findByPaymentId(event.id()).stream()
                .anyMatch(e -> matchesStatus(event.status(), e.getEntryType()))) {
            log.info("Ledger entries already exist for payment {}", event.id());
            return;
        }

        if (event.status() == PaymentStatus.SUCCESS) {
            postEntry(event, event.payerVpa(), EntryType.DEBIT, "Payment debit");
            postEntry(event, event.payeeVpa(), EntryType.CREDIT, "Payment credit");
        } else if (event.status() == PaymentStatus.REVERSED) {
            postEntry(event, event.payerVpa(), EntryType.CREDIT, "Reversal credit to payer");
            postEntry(event, event.payeeVpa(), EntryType.DEBIT, "Reversal debit from payee");
        }
    }

    private boolean matchesStatus(PaymentStatus status, EntryType type) {
        return (status == PaymentStatus.SUCCESS && type == EntryType.DEBIT)
                || (status == PaymentStatus.REVERSED && type == EntryType.CREDIT);
    }

    private void postEntry(PaymentEvent event, String vpa, EntryType type, String description) {
        int retries = 3;
        for (int i = 0; i < retries; i++) {
            try {
                AccountBalance account = getOrCreateAccount(vpa);
                BigDecimal newBalance = type == EntryType.DEBIT
                        ? account.getBalance().subtract(event.amount())
                        : account.getBalance().add(event.amount());
                account.setBalance(newBalance);
                account.setLastUpdated(Instant.now());
                accountBalanceRepository.save(account);

                LedgerEntry entry = LedgerEntry.builder()
                        .id(UUID.randomUUID())
                        .paymentId(event.id())
                        .accountId(account.getAccountId())
                        .entryType(type)
                        .amount(event.amount())
                        .currency(event.currency())
                        .balanceAfter(newBalance)
                        .description(description)
                        .createdAt(Instant.now())
                        .build();
                ledgerEntryRepository.save(entry);
                updateDailySummary(account, entry);
                verifyBalance(account.getAccountId());
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (i == retries - 1) {
                    throw new LedgerException("CONFLICT", "Failed to update balance due to concurrent modification");
                }
            }
        }
    }

    private AccountBalance getOrCreateAccount(String vpa) {
        return accountBalanceRepository.findByVpa(vpa).orElseGet(() -> {
            AccountBalance balance = AccountBalance.builder()
                    .accountId(UUID.randomUUID())
                    .vpa(vpa)
                    .balance(BigDecimal.ZERO)
                    .version(0L)
                    .lastUpdated(Instant.now())
                    .build();
            return accountBalanceRepository.save(balance);
        });
    }

    private void updateDailySummary(AccountBalance account, LedgerEntry entry) {
        LocalDate today = LocalDate.now();
        DailyLedgerSummary summary = dailySummaryRepository
                .findByAccountIdAndSummaryDate(account.getAccountId(), today)
                .orElse(DailyLedgerSummary.builder()
                        .id(UUID.randomUUID())
                        .accountId(account.getAccountId())
                        .summaryDate(today)
                        .openingBalance(account.getBalance())
                        .closingBalance(account.getBalance())
                        .totalDebits(BigDecimal.ZERO)
                        .totalCredits(BigDecimal.ZERO)
                        .entryCount(0)
                        .build());

        if (entry.getEntryType() == EntryType.DEBIT) {
            summary.setTotalDebits(summary.getTotalDebits().add(entry.getAmount()));
        } else {
            summary.setTotalCredits(summary.getTotalCredits().add(entry.getAmount()));
        }
        summary.setClosingBalance(account.getBalance());
        summary.setEntryCount(summary.getEntryCount() + 1);
        dailySummaryRepository.save(summary);
    }

    private void verifyBalance(UUID accountId) {
        AccountBalance stored = accountBalanceRepository.findById(accountId)
                .orElseThrow(() -> new LedgerException("NOT_FOUND", "Account not found"));
        BigDecimal debits = ledgerEntryRepository.sumDebits(accountId);
        BigDecimal credits = ledgerEntryRepository.sumCredits(accountId);
        BigDecimal computed = credits.subtract(debits);
        if (stored.getBalance().compareTo(computed) != 0) {
            log.error("Balance discrepancy for account {}: stored={}, computed={}",
                    accountId, stored.getBalance(), computed);
        }
    }
}
