package com.paycore.payment.web.api;

import com.paycore.payment.dto.*;
import com.paycore.payment.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping("/{vpa}")
    public AccountResponse get(@PathVariable String vpa) {
        return accountService.getByVpa(vpa);
    }

    @PatchMapping("/{vpa}/deactivate")
    public AccountResponse deactivate(@PathVariable String vpa) {
        return accountService.deactivate(vpa);
    }

    @GetMapping("/{vpa}/payments")
    public List<PaymentResponse> payments(@PathVariable String vpa) {
        return accountService.paymentsForVpa(vpa);
    }
}
