package com.yiyi.common.handler.fallback;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;


@Component
@Aspect
public class FallbackAspect {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Before("@within(com.yiyi.common.handler.fallback.AnnoFallback)")
    void before(JoinPoint joinPoint) {
        this.logger.debug("拦截AnnoFallback方法========{}", joinPoint.getTarget().getClass());
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            AnnoFallback annoFallback = joinPoint.getTarget().getClass().<AnnoFallback>getAnnotation(AnnoFallback.class);
            if (annoFallback == null) {
                annoFallback = (AnnoFallback) AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), AnnoFallback.class);
            }

            String key = annoFallback.key();
            String value = annoFallback.value();
            MDC.put(key, value);
            this.logger.warn("Fallback method start:{}", methodSignature.getMethod());
        } catch (Exception e) {
            this.logger.error("拦截AnnoFallback方法", e);
        }
    }
}

