package com.smartquantify.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的各类异常，返回标准化的错误响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理资源未找到异常
     * @param ex 资源未找到异常
     * @return 404错误响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 处理参数校验异常
     * @param ex 参数校验异常
     * @return 400错误响应
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 处理风控异常
     * @param ex 风控异常
     * @return 403错误响应
     */
    @ExceptionHandler(RiskException.class)
    public ResponseEntity<Map<String, Object>> handleRiskException(RiskException ex) {
        log.warn("Risk check failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * 处理熔断器异常
     * @param ex 熔断器异常
     * @return 503错误响应，包含重试建议
     */
    @ExceptionHandler(CircuitBreakerException.class)
    public ResponseEntity<Map<String, Object>> handleCircuitBreakerException(CircuitBreakerException ex) {
        log.warn("Circuit breaker open for service: {}", ex.getServiceName());
        Map<String, Object> body = buildErrorBody(HttpStatus.SERVICE_UNAVAILABLE, 
                "Service temporarily unavailable, please try again later");
        body.put("service", ex.getServiceName());
        body.put("retryAfter", 30);
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * 处理限流异常
     * @param ex 限流异常
     * @return 429错误响应，包含重试建议
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitException(RateLimitException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        Map<String, Object> body = buildErrorBody(HttpStatus.TOO_MANY_REQUESTS, 
                "Rate limit exceeded, please try again later");
        body.put("retryAfter", ex.getRetryAfter());
        return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * 处理服务不可用异常
     * @param ex 服务不可用异常
     * @return 503错误响应，包含服务名和重试建议
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailableException(ServiceUnavailableException ex) {
        log.warn("Service unavailable: {}", ex.getServiceName());
        Map<String, Object> body = buildErrorBody(HttpStatus.SERVICE_UNAVAILABLE, 
                "Service unavailable, falling back to cached data");
        body.put("service", ex.getServiceName());
        body.put("retryAfter", 10);
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * 处理方法参数校验异常
     * @param ex 方法参数校验异常
     * @return 400错误响应，包含详细校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * 处理未知异常
     * @param ex 未知异常
     * @return 500错误响应，不暴露详细错误信息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    /**
     * 构建错误响应
     * @param status HTTP状态码
     * @param message 错误信息
     * @return 错误响应实体
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(buildErrorBody(status, message), status);
    }

    /**
     * 构建错误响应体
     * @param status HTTP状态码
     * @param message 错误信息
     * @return 错误响应体Map
     */
    private Map<String, Object> buildErrorBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", status.value());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.name());
        return body;
    }
}
