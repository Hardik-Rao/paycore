package com.paycore.common;

public final class KafkaTopics {

    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String PAYMENT_SUCCESS = "payment-success";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String PAYMENT_REVERSED = "payment-reversed";
    public static final String FRAUD_EVENTS = "fraud-events";
    public static final String FRAUD_ALERTS = "fraud-alerts";
    public static final String NOTIFICATION_EVENTS = "notification-events";
    public static final String RECONCILIATION_ALERTS = "reconciliation-alerts";

    private KafkaTopics() {
    }
}
