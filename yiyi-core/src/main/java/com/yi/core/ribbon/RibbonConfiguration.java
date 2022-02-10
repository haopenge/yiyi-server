package com.yi.core.ribbon;


import com.netflix.loadbalancer.IRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RibbonConfiguration {

    @Bean
    @ConditionalOnProperty(name = "enable_gray_env")
    public IRule ribbonRule(){
        return new GrayEnvRuler();
    }
}
