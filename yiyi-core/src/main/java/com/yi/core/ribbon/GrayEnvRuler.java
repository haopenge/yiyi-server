package com.yi.core.ribbon;


import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GrayEnvRuler extends ZoneAvoidanceRule {

    private Logger logger = LoggerFactory.getLogger(GrayEnvRuler.class);

    /**
     * 开启灰度环境
     */
    @Value("${enable_gray_env:false}")
    private Boolean enableGrayEnv;

    /**
     * 当前环境
     */
    @Value("${env}")
    private String env;

    /**
     * 允许灰度的环境
     */
    @Value("${gray_envs}")
    private String grayEnvs;

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }

    @Override
    public Server choose(Object key) {
        if(!enableGrayEnv){
            return super.choose(key);
        }

        // 不在允许的环境，走默认轮询
        List<String> enableGrayEnvList = Arrays.asList(grayEnvs.split(","));
        if (!enableGrayEnvList.contains(env)) {
            return super.choose(key);
        }

        ILoadBalancer lb = getLoadBalancer();
        if (Objects.isNull(lb)) {
            return null;
        }

        // 获取已激活的服务
        List<Server> serverList = lb.getReachableServers();
        // 获取入口环境
        String podEnv = EnvHolder.getEnv(Constants.POD_ENV);

        if(StringUtils.isNotEmpty(podEnv)){
            EnvHolder.clear();
        }

        for (Server server : serverList) {
            String instanceId = server.getMetaInfo().getInstanceId();
            List<String> instanceIdList = Arrays.asList(instanceId.split(":"));
            if (instanceIdList.size() != 3) {
                continue;
            }
            // id 格式： eureka.instance.instance-id=${spring.cloud.client.ipaddress}:${server.port}:${pod_env}
            String instancePodEnv = instanceIdList.get(2);

            if (StringUtils.equals(instancePodEnv, podEnv)) {
                // 匹配到对应的 业务环境服务
                return server;
            }
        }
        return super.choose(key);
    }
}
