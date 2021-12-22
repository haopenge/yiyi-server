package com.yi.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "msg")
public class MsgProperties {

    private String accessId;

    private String accessSecret;

}
