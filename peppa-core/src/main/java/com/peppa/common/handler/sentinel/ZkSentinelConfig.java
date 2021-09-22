package com.peppa.common.handler.sentinel;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Configuration
@ConditionalOnProperty(name = {"spring.cloud.sentinel.enabled"}, matchIfMissing = false)
public class ZkSentinelConfig {
    @Value("${sentinel.zkurl}")
    private String zkurl;
    @Value("${spring.application.name}")
    private String appName;

    @PostConstruct
    public void loadRules() {
        String remoteAddress = this.zkurl;
        if (this.zkurl == null || this.zkurl.trim().equals("")) {
            return;
        }
        String flowpath = ZookeeperConfigUtil.getPath(this.appName, RuleTypeEnum.RULE_TYPE_FLOW);
        String degradepath = ZookeeperConfigUtil.getPath(this.appName, RuleTypeEnum.RULE_TYPE_DEGRADE);
        String paramflowpath = ZookeeperConfigUtil.getPath(this.appName, RuleTypeEnum.RULE_TYPE_PARAMFLOW);
        String syspath = ZookeeperConfigUtil.getPath(this.appName, RuleTypeEnum.RULE_TYPE_SYS);
        String authpath = ZookeeperConfigUtil.getPath(this.appName, RuleTypeEnum.RULE_TYPE_AUTH);

        ZookeeperDataSource zookeeperDataSource1 = new ZookeeperDataSource(remoteAddress, flowpath, source -> (List) JSON.parseObject((String) source, new TypeReference<List<FlowRule>>() {

        }, new com.alibaba.fastjson.parser.Feature[0]));
        FlowRuleManager.register2Property(zookeeperDataSource1.getProperty());

        ZookeeperDataSource zookeeperDataSource2 = new ZookeeperDataSource(remoteAddress, degradepath, source -> (List) JSON.parseObject((String) source, new TypeReference<List<DegradeRule>>() {

        }, new com.alibaba.fastjson.parser.Feature[0]));
        DegradeRuleManager.register2Property(zookeeperDataSource2.getProperty());

        Converter<String, List<ParamFlowRule>> converter = new Converter<String, List<ParamFlowRule>>() {
            public List<ParamFlowRule> convert(String s) {
                List<AbstractRuleEntity<ParamFlowRule>> listentity = (List<AbstractRuleEntity<ParamFlowRule>>) JSON.parseObject(s, new TypeReference<List<AbstractRuleEntity<ParamFlowRule>>>() {
                }, new com.alibaba.fastjson.parser.Feature[0]);

                return ZkSentinelConfig.convetRuleList(listentity);
            }
        };

        ZookeeperDataSource zookeeperDataSource3 = new ZookeeperDataSource(remoteAddress, paramflowpath, converter);

        ParamFlowRuleManager.register2Property(zookeeperDataSource3.getProperty());

        ZookeeperDataSource zookeeperDataSource4 = new ZookeeperDataSource(remoteAddress, syspath, source -> (List) JSON.parseObject((String) source, new TypeReference<List<SystemRule>>() {

        }, new com.alibaba.fastjson.parser.Feature[0]));
        SystemRuleManager.register2Property(zookeeperDataSource4.getProperty());

        Converter<String, List<AuthorityRule>> converterauth = new Converter<String, List<AuthorityRule>>() {
            public List<AuthorityRule> convert(String s) {
                List<AbstractRuleEntity<AuthorityRule>> listentity = (List<AbstractRuleEntity<AuthorityRule>>) JSON.parseObject(s, new TypeReference<List<AbstractRuleEntity<AuthorityRule>>>() {
                }, new com.alibaba.fastjson.parser.Feature[0]);

                return ZkSentinelConfig.convetRuleList(listentity);
            }
        };
        ZookeeperDataSource zookeeperDataSource5 = new ZookeeperDataSource(remoteAddress, authpath, converterauth);
        AuthorityRuleManager.register2Property(zookeeperDataSource5.getProperty());
    }


    static class AbstractRuleEntity<T extends AbstractRule> {
        protected Long id;
        protected String app;
        protected String ip;
        protected Integer port;

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getApp() {
            return this.app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getIp() {
            return this.ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return this.port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getMachineMode() {
            return this.machineMode;
        }

        public void setMachineMode(Integer machineMode) {
            this.machineMode = machineMode;
        }

        public T getRule() {
            return this.rule;
        }

        public void setRule(T rule) {
            this.rule = rule;
        }

        public Date getGmtCreate() {
            return this.gmtCreate;
        }

        public void setGmtCreate(Date gmtCreate) {
            this.gmtCreate = gmtCreate;
        }

        public Date getGmtModified() {
            return this.gmtModified;
        }

        public void setGmtModified(Date gmtModified) {
            this.gmtModified = gmtModified;
        }


        private Integer machineMode = Integer.valueOf(0);

        protected T rule;

        private Date gmtCreate;
        private Date gmtModified;
    }

    public static <T extends AbstractRule> List<T> convetRuleList(List<AbstractRuleEntity<T>> lists) {
        List<T> result = new ArrayList<>(lists.size());
        for (AbstractRuleEntity<T> entity : lists) {
            result.add(entity.getRule());
        }
        return result;
    }
}


