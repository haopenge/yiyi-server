package com.yiyi.common.handler.controller;

import com.netflix.discovery.EurekaClient;
import com.yiyi.common.handler.monitor.YiyiHealth;
import com.yiyi.common.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
public class DiscoveryManagerController {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryManagerController.class);

    @Autowired
    private EurekaClient eurekaClient;

    @Value("${yiyi.killsleep:65}")
    private Integer killsleep;

    @Resource
    private ServiceRegistry serviceRegistry;

    @Resource
    private Registration registration;

    @RequestMapping(value = {"/offline_h"}, method = {RequestMethod.GET})
    public Response offLine(String pw) {
        Response responseInfo = (new Response()).success();
        if ("vfoiyes230pljafwnsfsswfWRoafa12445401afanncafdmcaarnbspqlkjhgfdsaqwertyuiopmnbvcxz567".equals(pw)) {
            (new Thread(new Runnable() {
                public void run() {
                    DiscoveryManagerController.log.info("clean eureka list start");
                    if (DiscoveryManagerController.this.eurekaClient != null) {
                        DiscoveryManagerController.this.eurekaClient.shutdown();
                        YiyiHealth.isEurekadown = true;
                    }
                    DiscoveryManagerController.log.info("eureka offline!");
                    DiscoveryManagerController.log.info("{} seconds later server will suicide!", DiscoveryManagerController.this.killsleep);
                    try {
                        Thread.sleep((DiscoveryManagerController.this.killsleep.intValue() * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DiscoveryManagerController.log.info("server suicide!");
                    System.exit(0);
                }
            })).start();
        } else {
            responseInfo = (new Response()).failure("密钥不合法");
        }
        return responseInfo;
    }


    @RequestMapping(value = {"/actuator/arc_op/ignite"}, method = {RequestMethod.GET})
    public Response ignite(String pw) {
        if (this.serviceRegistry == null || this.registration == null) {
            log.error("serviceRegistry is null : {}, registration is null : {}", Boolean.valueOf((this.serviceRegistry == null)), Boolean.valueOf((this.registration == null)));
            return (new Response()).failure("serviceRegistry or registration is null");
        }
        if ("vfoiyes230pljafwnsfsswfWRoafa12445401afanncafdmcaarnbspqlkjhgfdsaqwertyuiopmnbvcxz567".equals(pw)) {
            this.serviceRegistry.setStatus(this.registration, "UP");
            log.info("Eureka status updated to UP!");
        } else {
            return (new Response()).failure("密钥不合法");
        }
        return (new Response()).success();
    }


    @RequestMapping(value = {"/actuator/arc_op/flameout"}, method = {RequestMethod.GET})
    public Response flameout(String pw) {
        if (this.serviceRegistry == null || this.registration == null) {
            log.error("serviceRegistry is null : {}, registration is null : {}", Boolean.valueOf((this.serviceRegistry == null)), Boolean.valueOf((this.registration == null)));
            return (new Response()).failure("serviceRegistry or registration is null");
        }
        if ("vfoiyes230pljafwnsfsswfWRoafa12445401afanncafdmcaarnbspqlkjhgfdsaqwertyuiopmnbvcxz567".equals(pw)) {
            this.serviceRegistry.setStatus(this.registration, "OUT_OF_SERVICE");
            log.info("Eureka status updated to OUT_OF_SERVICE!");
        } else {
            return (new Response()).failure("密钥不合法");
        }
        return (new Response()).success();
    }


    @RequestMapping(value = {"/actuator/arc_op/current_state"}, method = {RequestMethod.GET})
    public Response currentState(String pw) {
        if (this.serviceRegistry == null || this.registration == null) {
            log.error("serviceRegistry is null : {}, registration is null : {}", Boolean.valueOf((this.serviceRegistry == null)), Boolean.valueOf((this.registration == null)));
            return (new Response()).failure("serviceRegistry or registration is null");
        }
        Object status = null;
        if ("vfoiyes230pljafwnsfsswfWRoafa12445401afanncafdmcaarnbspqlkjhgfdsaqwertyuiopmnbvcxz567".equals(pw)) {
            status = this.serviceRegistry.getStatus(this.registration);
            log.info("Eureka status updated to UP!");
        } else {
            return (new Response()).failure("密钥不合法");
        }
        return (new Response()).data(status).success();
    }
}
