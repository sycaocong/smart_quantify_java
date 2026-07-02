package com.smartquantify.common.exception;

/**
 * 熔断器异常
 * 当服务熔断器打开时抛出
 */
public class CircuitBreakerException extends RuntimeException {

    /**
     * 服务名称
     */
    private final String serviceName;

    /**
     * 构造函数
     * @param message 错误信息
     * @param serviceName 服务名称
     */
    public CircuitBreakerException(String message, String serviceName) {
        super(message);
        this.serviceName = serviceName;
    }

    /**
     * 构造函数
     * @param message 错误信息
     * @param cause 原始异常
     * @param serviceName 服务名称
     */
    public CircuitBreakerException(String message, Throwable cause, String serviceName) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    /**
     * 获取服务名称
     * @return 服务名称
     */
    public String getServiceName() {
        return serviceName;
    }
}
