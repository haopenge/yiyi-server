package com.yiyi.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.husky.intf"})
public class YiyiExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(YiyiExampleApplication.class, args);
    }
}
