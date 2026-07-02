package com.smartquantify.common.exception;

/**
 * 风控异常
 * 当订单触发风控规则时抛出
 */
public class RiskException extends RuntimeException {

    /**
     * 构造函数
     * @param message 错误信息
     */
    public RiskException(String message) {
        super(message);
    }
}