package com.peppa.common.grayconfig;

import com.xxl.job.core.handler.annotation.JobHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Aspect
@Component
@ConditionalOnClass({JobHandler.class})
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
@Order(0)
public class XxjobGrayAspect {
    @Value("${podenv:}")
    private String podenv;

    @Pointcut("@target(com.xxl.job.core.handler.annotation.JobHandler) && execution (* *.execute(java.lang.String))")
    public void xxlcut() {
    }

    @Around("xxlcut()")
    public Object addPodenvHeader(ProceedingJoinPoint pjp) throws Throwable {
        if (this.podenv != null && this.podenv.trim().length() > 0 && !this.podenv.equals("qa") && !this.podenv.equals("dev") && !this.podenv.equals("sim")) {
            ThreadAttributes.setThreadAttribute("huohua-".concat("podenv"), this.podenv);
        }

        Object retObj = pjp.proceed();
        return retObj;
    }
}
