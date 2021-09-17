package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.husky.intf"})
public class PeppaExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeppaExampleApplication.class, args);
    }
}
