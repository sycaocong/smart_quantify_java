package com.smartquantify.common.exception;

/**
 * 参数校验异常
 * 当请求参数不符合校验规则时抛出
 */
public class ValidationException extends RuntimeException {

    /**
     * 构造函数
     * @param message 错误信息
     */
    public ValidationException(String message) {
        super(message);
    }
}