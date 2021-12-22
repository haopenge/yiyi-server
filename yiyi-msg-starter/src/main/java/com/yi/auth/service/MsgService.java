package com.yi.auth.service;


import com.yi.auth.config.MsgProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class MsgService {

    private Logger logger = LoggerFactory.getLogger(MsgService.class);

    private MsgProperties properties;

    public MsgService() {
    }

    public MsgService(MsgProperties properties) {
        this.properties = properties;
    }

    /**
     * 发送短信
     * @param templateCode  短信模板编码
     * @param phone 手机号
     * @param params 替换字符数组
     */
    public void sendMessage(String templateCode,String phone,String... params){
        sendMessageOnXxPlatform(templateCode,phone,params);

    }

    /**
     * 通过 xx 平台  发送短信
     */
    public void sendMessageOnXxPlatform(String templateCode,String phone,String... params){
        // 模拟第三方短信服务
        if(StringUtils.hasText(properties.getAccessId()) && StringUtils.hasText(properties.getAccessSecret())){
            logger.info("send msg by xx platform success templateCode = {} , phone = {} , params : {}",templateCode,phone,params);
        }
    }

}
