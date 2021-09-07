package com.peppa.common.grayconfig.Strategy;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@Aspect
public class StrategyAspect {
    private static final Logger logger = LoggerFactory.getLogger(StrategyAspect.class);

    @Pointcut("execution(* com.peppa.common.grayconfig.Strategy.Strategy.getServer(..))")
    public void pointCutStrategy() {
    }

    @Before("pointCutStrategy()")
    public void pointCutGetServerHandler(JoinPoint point) {
        Strategy strategy = (Strategy) point.getTarget();
        logger.debug("=====策略" + strategy.getName() + "启动");
    }
}
