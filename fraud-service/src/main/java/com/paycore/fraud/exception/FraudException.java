package com.paycore.fraud.exception;

public class FraudException extends RuntimeException {
    private final String errorCode;
    public FraudException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
    public String getErrorCode() { return errorCode; }
}
