package com.paycore.payment.repository;

import com.paycore.payment.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByVpa(String vpa);
    boolean existsByVpa(String vpa);
}
