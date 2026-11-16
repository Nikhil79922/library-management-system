package com.nikkssy.library_management.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    // execution(* returnType packagePath..className.methodName(methodArguments))
//           *       com.nikkssy.library_management.service..*       *       (..)
//           |                    |                                  |        |
//           |                    |                                  |        └── Any number/type of arguments
//           |                    |                                  └────────── Any method name
//           |                    └───────────────────────────────────────────── Any class in service package/subpackages
//           └────────────────────────────────────────────────────────────────── Any return type

    @Around("execution(* com.nikkssy.library_management.service..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint)
            throws Throwable {

        String className = joinPoint.getSignature()
                .getDeclaringType()
                .getSimpleName();

        String methodName = joinPoint.getSignature().getName();

        long startTime = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.nanoTime() - startTime;

            long executionTimeMs =
                    TimeUnit.NANOSECONDS.toMillis(duration);

            log.info(
                    "Service: {} | Method: {} | Execution Time: {} ms",
                    className,
                    methodName,
                    executionTimeMs
            );
        }
    }

    @AfterThrowing(
            pointcut = "execution(* com.nikkssy.library_management.services..*.*(..))",
            throwing = "exception"
    )
    public void logServiceException(
            JoinPoint joinPoint,
            Throwable exception
    ) {

        log.error(
                "Service exception | class={} | method={} | cause={}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                exception.getMessage(),
                exception
        );
    }
}