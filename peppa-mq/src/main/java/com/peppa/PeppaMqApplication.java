package com.peppa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PeppaMqApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PeppaMqApplication.class, args);
    }



}
