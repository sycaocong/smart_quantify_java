package com.smartquantify.common.exception;

/**
 * 资源未找到异常
 * 当请求的资源不存在时抛出
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 构造函数
     * @param message 错误信息
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * @param resourceType 资源类型
     * @param id 资源ID
     */
    public ResourceNotFoundException(String resourceType, String id) {
        super(String.format("%s not found with id: %s", resourceType, id));
    }
}