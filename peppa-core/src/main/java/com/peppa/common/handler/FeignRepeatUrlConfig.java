package com.peppa.common.handler;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@Configuration
public class FeignRepeatUrlConfig implements WebMvcRegistrations {
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new MyRequestMappingHandlerMapping();
    }

    public class MyRequestMappingHandlerMapping
            extends RequestMappingHandlerMapping {
        protected boolean isHandler(Class<?> beanType) {
            return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) || (
                    AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class) && !beanType.isInterface()));
        }
    }
}
