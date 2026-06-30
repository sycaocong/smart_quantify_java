package com.smartquantify.common.exception;

public class CircuitBreakerException extends RuntimeException {
    private final String serviceName;

    public CircuitBreakerException(String message, String serviceName) {
        super(message);
        this.serviceName = serviceName;
    }

    public CircuitBreakerException(String message, Throwable cause, String serviceName) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
