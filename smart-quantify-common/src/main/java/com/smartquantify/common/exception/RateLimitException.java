package com.smartquantify.common.exception;

/**
 * 限流异常
 * 当请求频率超过限制时抛出
 */
public class RateLimitException extends RuntimeException {

    /**
     * 建议重试时间（秒）
     */
    private final long retryAfter;

    /**
     * 构造函数，默认重试时间60秒
     * @param message 错误信息
     */
    public RateLimitException(String message) {
        super(message);
        this.retryAfter = 60;
    }

    /**
     * 构造函数
     * @param message 错误信息
     * @param retryAfter 建议重试时间（秒）
     */
    public RateLimitException(String message, long retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    /**
     * 获取建议重试时间
     * @return 重试时间（秒）
     */
    public long getRetryAfter() {
        return retryAfter;
    }
}
