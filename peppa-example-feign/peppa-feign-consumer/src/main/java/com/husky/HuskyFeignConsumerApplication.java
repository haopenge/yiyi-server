package com.husky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.husky.intf"})
@SpringBootApplication
public class HuskyFeignConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuskyFeignConsumerApplication.class, args);
    }

}
