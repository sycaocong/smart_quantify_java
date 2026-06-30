package com.smartquantify.common.aspect;

import com.smartquantify.common.config.DataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("execution(* com.smartquantify.*.repository.*.find*(..)) || " +
              "execution(* com.smartquantify.*.repository.*.get*(..)) || " +
              "execution(* com.smartquantify.*.repository.*.query*(..)) || " +
              "execution(* com.smartquantify.*.repository.*.read*(..))")
    public void readMethods() {}

    @Pointcut("execution(* com.smartquantify.*.repository.*.save*(..)) || " +
              "execution(* com.smartquantify.*.repository.*.insert*(..)) || " +
              "execution(* com.smartquantify.*.repository.*.update*(..)) || " +
              "execution(* com.smartquantify.*.repository.*.delete*(..)) || " +
              "execution(* com.smartquantify.*.repository.*.create*(..))")
    public void writeMethods() {}

    @Around("readMethods()")
    public Object read(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            DataSourceConfig.useSlave();
            return joinPoint.proceed();
        } finally {
            DataSourceConfig.clear();
        }
    }

    @Around("writeMethods()")
    public Object write(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            DataSourceConfig.useMaster();
            return joinPoint.proceed();
        } finally {
            DataSourceConfig.clear();
        }
    }
}
