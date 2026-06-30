package com.smartquantify.common.exception;

public class RateLimitException extends RuntimeException {
    private final long retryAfter;

    public RateLimitException(String message) {
        super(message);
        this.retryAfter = 60;
    }

    public RateLimitException(String message, long retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public long getRetryAfter() {
        return retryAfter;
    }
}
