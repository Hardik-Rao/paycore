package com.paycore.ledger.web.api;

import com.paycore.ledger.domain.EntryType;
import com.paycore.ledger.dto.*;
import com.paycore.ledger.service.LedgerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerQueryService ledgerQueryService;

    @GetMapping("/accounts/{vpa}/balance")
    public BalanceResponse balance(@PathVariable String vpa) {
        return ledgerQueryService.getBalance(vpa);
    }

    @GetMapping("/accounts/{vpa}/statement")
    public Page<LedgerEntryResponse> statement(
            @PathVariable String vpa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) EntryType type,
            Pageable pageable) {
        return ledgerQueryService.getStatement(vpa, from, to, type, pageable);
    }

    @GetMapping(value = "/accounts/{vpa}/statement/export", produces = "text/csv")
    public ResponseEntity<String> export(
            @PathVariable String vpa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) EntryType type) {
        String csv = ledgerQueryService.exportStatement(vpa, from, to, type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement-" + vpa + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/entries/{paymentId}")
    public List<LedgerEntryResponse> byPayment(@PathVariable UUID paymentId) {
        return ledgerQueryService.getByPaymentId(paymentId);
    }

    @GetMapping("/accounts/{vpa}/summary")
    public MonthlySummaryResponse monthly(@PathVariable String vpa, @RequestParam String month) {
        return ledgerQueryService.getMonthlySummary(vpa, month);
    }

    @GetMapping("/accounts/{vpa}/daily-summary")
    public List<DailySummaryResponse> daily(@PathVariable String vpa) {
        return ledgerQueryService.getDailySummary(vpa);
    }

    @PostMapping("/accounts/{vpa}/mini-statement")
    public List<LedgerEntryResponse> mini(@PathVariable String vpa) {
        return ledgerQueryService.miniStatement(vpa);
    }
}
