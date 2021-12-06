package com.yiyi.ribbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

@RibbonClient(name = "eat")
@SpringBootApplication
public class YiyiRibbonConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(YiyiRibbonConsumerApplication.class, args);
	}

}
