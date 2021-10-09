package com.yiyi.common.handler.controller.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class JarFileUtils {
    private static final Logger log = LoggerFactory.getLogger(JarFileUtils.class);

    public static List<String> readJarFile(String jarname) {
        List<String> retlist = new ArrayList<>();
        JarFile jarFile = null;
        BufferedReader br = null;


        try {
            String jarFilePath = ClassUtils.getDefaultClassLoader().getResource("").getPath().replace("!/BOOT-INF/classes!/", "");
            if (jarFilePath.startsWith("file")) {
                jarFilePath = jarFilePath.substring(5);
            }
            if (!jarFilePath.endsWith(".jar")) {
                retlist.add("不支持调试时使用，请在jar包发布模式使用。");
            }
            log.info("jarFilePath:" + jarFilePath);


            jarFile = new JarFile(jarFilePath);


            if (jarname != null && jarname.trim().length() > 0) {
                Enumeration<JarEntry> enumeration = jarFile.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = enumeration.nextElement();
                    if (jarEntry.getName().indexOf(jarname) >= 0) {
                        retlist.add(jarEntry.getName());
                    }
                }


                return retlist;
            }

            retlist.add(getMeta(jarFile));
            return retlist;

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
                if (null != jarFile) {
                    jarFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        retlist.add("未找到MANIFEST.MF文件");
        return retlist;
    }


    private static void inputStream2File(InputStream is, File file) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];

            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            os.close();
            is.close();
        }
    }


    public static String getMeta(JarFile jarFile) throws IOException {
        JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");

        if (entry != null) {
            InputStream in = jarFile.getInputStream(entry);

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "<br>");
            }
            return sb.toString();
        }
        return "未找到MANIFEST.MF文件";
    }
}


