package com.onnury.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Slf4j
@Component
public class MethodCallMonitorAspect {

    // 호출 함수 확인 aop
    @Around("@annotation(com.onnury.aop.MethodCallMonitor)")
    public Object methodCallLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[AOP] 호출 함수 확인");

        // 호출 함수 명 및 파라미터 확인 로그 처리
        log.info("함수 : {}, 파라미터 : {}",
                joinPoint.getSignature().getName(),
                Arrays.toString(((CodeSignature) joinPoint.getSignature()).getParameterNames()));

        // aop 전 / 후 구분점 적용
        Object proceed = joinPoint.proceed();

        // 호출 함수 정상 반환 데이터 타입 로그 처리
        log.info("{} 함수 정상 반환 데이터 : {}",
                joinPoint.getSignature().getName(),
                ((MethodSignature)joinPoint.getSignature()).getReturnType().toGenericString());

        return proceed;
    }
}
