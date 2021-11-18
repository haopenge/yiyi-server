package com.yiyi.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@EnableApolloConfig
public class ApolloConfig {

    @Value("${yiyi.eat:}")
    private String test;

    public String getTest(){
        return test;
    }
}
