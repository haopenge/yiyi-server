package com.peppa.common.handler.fallback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnoFallback {
    String key() default "Fallback";

    String value() default "Fallback";
}

