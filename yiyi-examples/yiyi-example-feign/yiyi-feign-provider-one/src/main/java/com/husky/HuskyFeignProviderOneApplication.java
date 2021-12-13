package com.husky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class HuskyFeignProviderOneApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuskyFeignProviderOneApplication.class, args);
    }

}
