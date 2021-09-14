package com.husky.intf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.husky.intf"})
@SpringBootApplication
public class HuskyFeignConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuskyFeignConsumerApplication.class, args);
    }

}
