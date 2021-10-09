/*    */
package com.yiyi.common.handler.controller.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.stream.Stream;


@RestController
public class JarVersionController {
    private static final Logger log = LoggerFactory.getLogger(JarVersionController.class);


    @RequestMapping(value = {"/checkJarVersion"}, method = {RequestMethod.GET})
    public Object checkJarVersion(String classNames) {
        Set<String> set = new HashSet<>();
        for (String name : classNames.split(";")) {
            try {
                Class<?> aClass = Class.forName(name);
                ProtectionDomain protectionDomain = aClass.getProtectionDomain();
                CodeSource cs = protectionDomain.getCodeSource();
                String urlInfo = cs.getLocation().toString();
                Stream<String> any = Arrays.stream(urlInfo.split("/"));
                Stream<String[]> stream = (Stream) any.map(st -> st.split("!"));
                String[] jar = new String[1];
                stream.forEach(a -> {
                    Optional<String> first = Arrays.<String>stream(a).findFirst();
                    jar[0] = first.get();
                });
                set.add(jar[0]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (set.size() > 0) {
            return set;
        }
        return "not used";
    }


    @RequestMapping({"/actuator/jarinfo"})
    @ResponseBody
    public List<String> jarinfo(@RequestParam("jarname") String jarname) {
        return JarFileUtils.readJarFile(jarname);
    }

    @RequestMapping({"/actuator/podenv"})
    @ResponseBody
    public Map podenv() {
        String podenv = System.getProperty("podenv");
        if (podenv == null) {
            podenv = System.getenv("podenv");
        }
        String cellid = System.getProperty("cellid");
        if (cellid == null) {
            cellid = System.getenv("cellid");
        }
        Map<Object, Object> map = new HashMap<>();
        map.put("podenv", podenv);
        map.put("cellid", cellid);

        return map;
    }
}


