package com.paycore.payment.service;

import com.paycore.payment.domain.Account;
import com.paycore.payment.dto.*;
import com.paycore.payment.exception.PaymentException;
import com.paycore.payment.repository.AccountRepository;
import com.paycore.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        if (accountRepository.existsByVpa(request.vpa())) {
            throw new PaymentException("CONFLICT", "VPA already exists");
        }
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .vpa(request.vpa())
                .accountHolder(request.accountHolder())
                .accountType(request.accountType())
                .kycStatus("VERIFIED")
                .active(true)
                .createdAt(Instant.now())
                .build();
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse getByVpa(String vpa) {
        return accountRepository.findByVpa(vpa)
                .map(this::toResponse)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Account not found"));
    }

    @Transactional
    public AccountResponse deactivate(String vpa) {
        Account account = accountRepository.findByVpa(vpa)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Account not found"));
        account.setActive(false);
        return toResponse(accountRepository.save(account));
    }

    public List<com.paycore.payment.dto.PaymentResponse> paymentsForVpa(String vpa) {
        accountRepository.findByVpa(vpa)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "Account not found"));
        return paymentRepository.findByPayerVpaOrPayeeVpa(vpa, vpa).stream()
                .map(p -> new com.paycore.payment.dto.PaymentResponse(
                        p.getId(), p.getIdempotencyKey(), p.getPayerVpa(), p.getPayeeVpa(),
                        p.getAmount(), p.getCurrency(), p.getStatus(), p.getFraudScore(),
                        p.getFailureReason(), p.getReversalReason(), p.getMetadata(),
                        p.getInitiatedAt(), p.getProcessedAt(), p.getUpdatedAt()))
                .toList();
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getVpa(), a.getAccountHolder(),
                a.getAccountType(), a.getKycStatus(), a.isActive(), a.getCreatedAt());
    }
}
