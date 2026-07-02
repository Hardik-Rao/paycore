package com.paycore.payment.web.admin;

import com.paycore.payment.repository.AccountRepository;
import com.paycore.payment.repository.PaymentRepository;
import com.paycore.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentService paymentService;

    @GetMapping
    public String overview(Model model) {
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        model.addAttribute("stats", paymentService.stats(from, Instant.now()));
        model.addAttribute("recent", paymentRepository.findAll(PageRequest.of(0, 10)));
        return "admin/overview";
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentRepository.findAll(PageRequest.of(0, 50)));
        return "admin/payments";
    }

    @GetMapping("/payments/{id}")
    public String paymentDetail(@PathVariable UUID id, Model model) {
        model.addAttribute("payment", paymentService.getById(id));
        model.addAttribute("audit", paymentService.auditTrail(id));
        return "admin/payment-detail";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", accountRepository.findAll());
        return "admin/accounts";
    }
}
