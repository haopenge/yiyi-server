package com.eurake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class PeppaEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeppaEurekaApplication.class, args);
    }

}
