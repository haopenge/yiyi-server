package com.peppa.grayconfig;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ThreadAttributes {
    private static ThreadLocal<Map<String, Object>> threadAttributes = new ThreadLocal<Map<String, Object>>() {
        protected synchronized Map<String, Object> initialValue() {
            return new HashMap();
        }
    };

    public ThreadAttributes() {
    }

    public static Object getThreadAttribute(String name) {
        return ((Map) threadAttributes.get()).get(name);
    }

    public static Object setThreadAttribute(String name, Object value) {
        return ((Map) threadAttributes.get()).put(name, value);
    }

    public static void remove() {
        threadAttributes.remove();
    }

    public static String getHeaderValue(String header_key) {
        String headerValue = (String) getThreadAttribute("huohua-".concat(header_key));
        return headerValue != null && !headerValue.trim().equals("") ? headerValue.trim() : null;
    }

}
