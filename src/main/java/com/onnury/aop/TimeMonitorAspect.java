package com.onnury.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class TimeMonitorAspect {

    // joinPoint.proceed() 로 Around 전 / 후를 구분한다.
    // 즉, joinPoint.proceed를 하기 전 작성된 코드가 aop를 적용할 함수가 시작되기 이전에 실행되고, joinPoint.proceed 를 작성한 이후의 코드가 함수 실행 완료 후 실행된다.

    // 함수 실행 시간 로깅 aop
    @Around("@annotation(com.onnury.aop.TimeMonitor)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable{
        log.info("[AOP] 함수 실행 시간 로깅");

        // 함수 첫 실행 시간
        long startTime = System.currentTimeMillis();

        // 전 / 후 구분점 적용
        Object proceed = joinPoint.proceed();

        // 함수 실행 완료 시간 계산
        long executionTime = System.currentTimeMillis() - startTime;

        // 함수 실행 완료 시간 로깅
        log.info(joinPoint.getSignature() + " 실행 시간 : " + executionTime + " ms");
        return proceed;
    }
}
