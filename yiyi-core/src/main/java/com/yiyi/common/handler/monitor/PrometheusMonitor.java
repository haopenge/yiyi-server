package com.yiyi.common.handler.monitor;

import com.alibaba.fastjson.JSONObject;
import com.yiyi.common.util.LoadGitPropertiesUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class PrometheusMonitor {
    private static final Logger log = LoggerFactory.getLogger(PrometheusMonitor.class);


    @Value("${spring.application.name:unknown}")
    String applicationName;


    @Bean
    public MeterRegistryCustomizer<MeterRegistry> gitInfoConfigurer() {
        try {
            log.info("init git data start");
            LoadGitPropertiesUtil loadGitPropertiesUtil = new LoadGitPropertiesUtil();
            String gitDate = loadGitPropertiesUtil.readGitProperties();
            JSONObject jsonObject = JSONObject.parseObject(gitDate);
            String branch = jsonObject.getString("git.branch");
            String time = jsonObject.getString("git.commit.time");
            String commitId = jsonObject.getString("git.commit.id.abbrev");
            String commitName = jsonObject.getString("git.commit.user.name");
            String host = jsonObject.getString("git.build.host");
            String userName = jsonObject.getString("git.build.user.name");
            String bv = jsonObject.getString("git.build.version");
            String bt = jsonObject.getString("git.build.time");
            String cmsg = jsonObject.getString("git.commit.message.full");
            Tag id = Tag.of("git.commit.id", commitId);
            Tag bch = Tag.of("git.branch", branch);
            Tag tm = Tag.of("git.commit.time", time);
            Tag cn = Tag.of("git.commit.user.name", commitName);
            Tag application = Tag.of("application", this.applicationName);
            Tag h = Tag.of("build.host", host);
            Tag un = Tag.of("build.user.name", userName);
            Tag bbv = Tag.of("build.version", bv);
            Tag bbt = Tag.of("build.time", bt);
            Tag msg = Tag.of("git.commit.message", cmsg);
            List<Tag> tags = new ArrayList<>();
            tags.add(id);
            tags.add(bch);
            tags.add(tm);
            tags.add(cn);
            tags.add(h);
            tags.add(un);
            tags.add(bbv);
            tags.add(bbt);
            tags.add(msg);
            tags.add(application);
            log.info("init git data end");
            return registry -> registry.gauge("git_info", tags, 1);
        } catch (Exception e) {
            log.warn("初始化项目名称及git相关监控数据失败");

            return null;
        }
    }
}

