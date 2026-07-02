package com.paycore.payment.service;

import com.paycore.common.PaymentStatus;
import com.paycore.payment.exception.PaymentException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class PaymentStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        TRANSITIONS.put(PaymentStatus.INITIATED, Set.of(PaymentStatus.PROCESSING, PaymentStatus.CANCELLED));
        TRANSITIONS.put(PaymentStatus.PROCESSING, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED));
        TRANSITIONS.put(PaymentStatus.SUCCESS, Set.of(PaymentStatus.REVERSED));
        TRANSITIONS.put(PaymentStatus.FAILED, Set.of(PaymentStatus.PROCESSING));
        TRANSITIONS.put(PaymentStatus.REVERSED, Set.of());
        TRANSITIONS.put(PaymentStatus.CANCELLED, Set.of());
    }

    private PaymentStateMachine() {
    }

    public static void validateTransition(PaymentStatus from, PaymentStatus to) {
        if (from == to) {
            return;
        }
        Set<PaymentStatus> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new PaymentException("INVALID_STATE",
                    "Cannot transition from " + from + " to " + to);
        }
    }
}
