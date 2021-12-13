package com.husky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class HuskyFeignProviderTwoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuskyFeignProviderTwoApplication.class, args);
    }

}
