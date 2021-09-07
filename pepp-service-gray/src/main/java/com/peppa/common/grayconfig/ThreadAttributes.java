package com.peppa.common.grayconfig;

import com.alibaba.fastjson.JSON;
import com.peppa.common.util.okhttp.PeppaOkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class ThreadAttributes {
    private static final Logger logger = LoggerFactory.getLogger(ThreadAttributes.class);

    private static ThreadLocal<Map<String, Object>> threadAttribues = new ThreadLocal<Map<String, Object>>() {
        protected synchronized Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };


    public static Object getThreadAttribute(String name) {
        return ((Map) threadAttribues.get()).get(name);
    }


    public static Object setThreadAttribute(String name, Object value) {
        return ((Map<String, Object>) threadAttribues.get()).put(name, value);
    }

    public static void remove() {
        threadAttribues.remove();
    }


    public static String getHeaderValue(String header_key) {
        String headervalue = (String) getThreadAttribute("huohua-".concat(header_key));
        if (headervalue == null || headervalue.trim().equals("")) {
            return null;
        }
        return headervalue.trim();
    }


    public static void set3rdCallBackPodenv(String grayflag, String bizkey, String bizPrefix) throws Exception {
        if (!"true".equals(grayflag)) {
            return;
        }

        String podenv = getHeaderValue("podenv");
        Map<String, String> map = new HashMap<>();
        map.put("bizkey", bizkey);
        map.put("bizPrefix", bizPrefix);
        map.put("podenv", podenv);
        PeppaOkHttpClient.postForm("http://cell3rd.qa.huohua.cn/cell/set3rd", map);
    }

    public static void get3rdCallBackPodenv(String grayflag, String bizkey, String bizPrefix) throws Exception {
        if (!"true".equals(grayflag)) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("bizkey", bizkey);
        map.put("bizPrefix", bizPrefix);
        String ret = PeppaOkHttpClient.getByMap("http://cell3rd.qa.huohua.cn/cell/get3rd", map);
        Map maps = (Map) JSON.parse(ret);
        if (((Boolean) maps.get("success")).booleanValue()) {
            String podenv = (String) maps.get("data");
            if (podenv != null && podenv.trim().length() > 0) {
                setThreadAttribute("huohua-".concat("podenv"), podenv);
            }
        } else {
            throw new Exception("第三方调用回调独立环境支撑服务失败！");
        }
    }
}
