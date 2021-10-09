package com.yiyi.common.handler;

import brave.Tracer;
import io.micrometer.core.instrument.util.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;


@Aspect
@Component
@ConditionalOnClass({Tracer.class})
public class BraveTraceEnhancer {
    @Autowired(required = false)
    private Tracer tracer;

    @Around("execution(* org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory.create(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            String serverName = (String) joinPoint.getArgs()[0];
            if (this.tracer != null && StringUtils.isNotBlank(serverName) &&
                    this.tracer.currentSpan() != null) {
                this.tracer.currentSpan().remoteServiceName(serverName);
            }
        } catch (Exception exception) {
        }

        return joinPoint.proceed();
    }
}


