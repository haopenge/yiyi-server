package com.peppa.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LoadGitPropertiesUtil {
    public String readGitProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("git.properties");

        StringBuilder stringBuilder = new StringBuilder();
        assert resourceAsStream != null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
