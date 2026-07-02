package com.paycore.ledger.repository;

import com.paycore.ledger.domain.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, UUID> {
    Optional<AccountBalance> findByVpa(String vpa);
}
