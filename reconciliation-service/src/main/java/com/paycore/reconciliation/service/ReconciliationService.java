package com.paycore.reconciliation.service;

import com.paycore.common.KafkaTopics;
import com.paycore.common.PaymentEvent;
import com.paycore.reconciliation.domain.*;
import com.paycore.reconciliation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationReportRepository reportRepository;
    private final ReconciliationMismatchRepository mismatchRepository;
    private final DisputeRepository disputeRepository;
    private final KafkaTemplate<String, Map<String, Object>> kafka;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${paycore.ledger-service-url}") private String ledgerUrl;

    private final List<PaymentEvent> recentPayments = Collections.synchronizedList(new ArrayList<>());

    public void trackPayment(PaymentEvent event) {
        recentPayments.add(event);
        if (recentPayments.size() > 10000) recentPayments.remove(0);
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void scheduledRun() { run(LocalDate.now().minusDays(1)); }

    @Transactional
    public ReconciliationReport run(LocalDate date) {
        List<PaymentEvent> dayPayments = recentPayments.stream()
                .filter(p -> p.initiatedAt() != null && p.initiatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(date))
                .toList();

        long success = dayPayments.stream().filter(p -> p.status().name().equals("SUCCESS")).count();
        long failed = dayPayments.stream().filter(p -> p.status().name().equals("FAILED")).count();
        long reversed = dayPayments.stream().filter(p -> p.status().name().equals("REVERSED")).count();
        BigDecimal volume = dayPayments.stream().map(PaymentEvent::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        ReconciliationReport report = ReconciliationReport.builder()
                .id(UUID.randomUUID()).reportDate(date)
                .totalPayments(dayPayments.size()).totalVolume(volume)
                .successCount(success).failedCount(failed).reversedCount(reversed)
                .fraudBlockedCount(0).mismatchCount(0).status("COMPLETED")
                .generatedAt(Instant.now()).build();

        long mismatches = 0;
        for (PaymentEvent p : dayPayments) {
            if ("SUCCESS".equals(p.status().name())) {
                int entries = countLedgerEntries(p.id());
                if (entries != 2) {
                    createMismatch(report.getId(), p.id(), "ENTRY_COUNT", "2", String.valueOf(entries));
                    mismatches++;
                }
            }
            if ("REVERSED".equals(p.status().name())) {
                int entries = countLedgerEntries(p.id());
                if (entries != 4) {
                    createMismatch(report.getId(), p.id(), "ENTRY_COUNT", "4", String.valueOf(entries));
                    mismatches++;
                }
            }
        }
        report.setMismatchCount(mismatches);
        reportRepository.save(report);
        if (mismatches > 0) {
            kafka.send(KafkaTopics.RECONCILIATION_ALERTS, report.getId().toString(),
                    Map.of("reportDate", date.toString(), "mismatchCount", mismatches));
        }
        return report;
    }

    private int countLedgerEntries(UUID paymentId) {
        try {
            List<?> entries = restTemplate.getForObject(ledgerUrl + "/api/v1/ledger/entries/" + paymentId, List.class);
            return entries != null ? entries.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void createMismatch(UUID reportId, UUID paymentId, String type, String expected, String actual) {
        mismatchRepository.save(ReconciliationMismatch.builder()
                .id(UUID.randomUUID()).reportId(reportId).paymentId(paymentId)
                .mismatchType(type).expectedValue(expected).actualValue(actual).build());
    }

    public List<ReconciliationReport> listReports() { return reportRepository.findAll(); }

    public ReconciliationReport getReport(LocalDate date) {
        return reportRepository.findByReportDate(date).orElseThrow();
    }

    public List<ReconciliationMismatch> unresolved() { return mismatchRepository.findByResolvedAtIsNull(); }

    @Transactional
    public ReconciliationMismatch resolve(UUID id, String notes) {
        ReconciliationMismatch m = mismatchRepository.findById(id).orElseThrow();
        m.setResolvedAt(Instant.now());
        m.setResolutionNotes(notes);
        return mismatchRepository.save(m);
    }

    @Transactional
    public Dispute raiseDispute(Dispute dispute) {
        dispute.setId(UUID.randomUUID());
        dispute.setStatus("OPEN");
        dispute.setRaisedAt(Instant.now());
        return disputeRepository.save(dispute);
    }

    public Page<Dispute> listDisputes(String status, Pageable pageable) {
        return status != null ? disputeRepository.findByStatus(status, pageable) : disputeRepository.findAll(pageable);
    }

    public Dispute getDispute(UUID id) { return disputeRepository.findById(id).orElseThrow(); }

    @Transactional
    public Dispute reviewDispute(UUID id, String status, String resolvedBy, String notes) {
        Dispute d = getDispute(id);
        d.setStatus(status);
        d.setResolvedBy(resolvedBy);
        d.setResolutionNotes(notes);
        d.setResolvedAt(Instant.now());
        return disputeRepository.save(d);
    }

    public Map<String, Object> settlement(LocalDate date) {
        ReconciliationReport report = reportRepository.findByReportDate(date).orElseThrow();
        BigDecimal fee = report.getTotalVolume().multiply(BigDecimal.valueOf(0.002)).setScale(2, RoundingMode.HALF_UP);
        return Map.of("date", date, "totalVolume", report.getTotalVolume(), "feeRate", "0.2%",
                "totalFees", fee, "netSettlement", report.getTotalVolume().subtract(fee));
    }
}
