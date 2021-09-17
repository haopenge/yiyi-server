
package com.peppa.common.feign.nosleuth;


import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional({ConditionOnNoSleuth.NoSleuthConditional.class})
public @interface ConditionOnNoSleuth {
    public static class NoSleuthConditional
            extends AnyNestedCondition {
        public NoSleuthConditional() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(name = {"spring.sleuth.feign.enabled"}, havingValue = "false")
        static class OnProperties {
        }

        @ConditionalOnMissingClass({"org.springframework.cloud.sleuth.instrument.web.client.feign.TraceLoadBalancerFeignClient"})
        static class OnMissClass {
        }
    }
}
