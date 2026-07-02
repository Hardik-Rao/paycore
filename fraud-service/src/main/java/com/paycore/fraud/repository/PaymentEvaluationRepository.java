package com.paycore.fraud.repository;

import com.paycore.fraud.domain.PaymentEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentEvaluationRepository extends JpaRepository<PaymentEvaluation, UUID> {}
