package com.paycore.reconciliation.web.api;

import com.paycore.reconciliation.domain.*;
import com.paycore.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @GetMapping("/api/v1/reconciliation/reports")
    public List<ReconciliationReport> reports() { return reconciliationService.listReports(); }

    @GetMapping("/api/v1/reconciliation/reports/{date}")
    public ReconciliationReport report(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reconciliationService.getReport(date);
    }

    @PostMapping("/api/v1/reconciliation/run")
    public ReconciliationReport run(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reconciliationService.run(date != null ? date : LocalDate.now().minusDays(1));
    }

    @GetMapping("/api/v1/reconciliation/mismatches")
    public List<ReconciliationMismatch> mismatches() { return reconciliationService.unresolved(); }

    @PostMapping("/api/v1/reconciliation/mismatches/{id}/resolve")
    public ReconciliationMismatch resolve(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return reconciliationService.resolve(id, body.get("notes"));
    }

    @PostMapping("/api/v1/disputes")
    public Dispute raise(@RequestBody Dispute dispute) { return reconciliationService.raiseDispute(dispute); }

    @GetMapping("/api/v1/disputes")
    public Page<Dispute> disputes(@RequestParam(required = false) String status, Pageable pageable) {
        return reconciliationService.listDisputes(status, pageable);
    }

    @GetMapping("/api/v1/disputes/{id}")
    public Dispute get(@PathVariable UUID id) { return reconciliationService.getDispute(id); }

    @PostMapping("/api/v1/disputes/{id}/review")
    public Dispute review(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return reconciliationService.reviewDispute(id, body.get("status"), body.get("resolvedBy"), body.get("notes"));
    }

    @GetMapping("/api/v1/reconciliation/settlement")
    public Map<String, Object> settlement(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reconciliationService.settlement(date);
    }
}
