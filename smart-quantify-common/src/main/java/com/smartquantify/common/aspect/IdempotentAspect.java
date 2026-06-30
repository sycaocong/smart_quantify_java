package com.smartquantify.common.aspect;

import com.smartquantify.common.annotation.Idempotent;
import com.smartquantify.common.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(com.smartquantify.common.annotation.Idempotent)")
    public Object checkIdempotent(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        String requestId = extractRequestId(idempotent);
        String cacheKey = idempotent.prefix() + requestId;

        Boolean exists = redisTemplate.hasKey(cacheKey);
        if (Boolean.TRUE.equals(exists)) {
            log.warn("Idempotent check failed: duplicate request with key {}", cacheKey);
            throw new RateLimitException(idempotent.errorMessage(), idempotent.expireTime());
        }

        redisTemplate.opsForValue().set(cacheKey, "processed", 
                idempotent.expireTime(), idempotent.timeUnit());

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            redisTemplate.delete(cacheKey);
            throw e;
        }
    }

    private String extractRequestId(Idempotent idempotent) {
        if (!idempotent.key().isEmpty()) {
            return idempotent.key();
        }

        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String requestId = request.getHeader("X-Request-Id");
            if (requestId != null && !requestId.isEmpty()) {
                return requestId;
            }

            String clientOrderId = request.getParameter("clientOrderId");
            if (clientOrderId != null && !clientOrderId.isEmpty()) {
                return clientOrderId;
            }
        }

        return UUID.randomUUID().toString();
    }
}
