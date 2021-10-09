package com.yiyi.service.gateway.gray.factory;

import com.yiyi.service.gateway.gray.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;


public class StrategyFactory {
    private ApplicationContext applicationContext;
    private static HashMap<String, Strategy> strategyHashMap = new HashMap<>();
    private static List<Strategy> strategyList = new ArrayList<>();
    final Logger logger = LoggerFactory.getLogger(getClass());

    public StrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void init() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(AnnoStrategy.class);
        Set<Map.Entry<String, Object>> entries = beans.entrySet();
        Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> map = iterator.next();
            Object bean = map.getValue();

            Class<?> aClass = map.getValue().getClass();
            this.logger.info("Strategy.aClass" + aClass);
            if (map.getValue() instanceof Strategy) {
                Strategy strategy = (Strategy) bean;
                String strategyname = strategy.getName();
                this.logger.info("Strategy. strategyname：" + strategyname);
                strategyHashMap.put(strategyname, strategy);
                strategyList.add(strategy);
            }
        }
        Collections.sort(strategyList);
        this.logger.info("StrategyFactory.strategyHashMap.size" + strategyHashMap.size());
    }

    public static Strategy getStrategy(String name) {
        return strategyHashMap.get(name);
    }


    public static List<Strategy> getAllStrategy() {
        return strategyList;
    }
}


