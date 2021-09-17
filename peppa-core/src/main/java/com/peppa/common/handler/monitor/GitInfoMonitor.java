package com.peppa.common.handler.monitor;


import com.peppa.common.util.LoadGitPropertiesUtil;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.io.IOException;


@Endpoint(id = "git")
public class GitInfoMonitor {
    @ReadOperation
    public String gitInfo() {
        LoadGitPropertiesUtil loadGitPropertiesUtil = new LoadGitPropertiesUtil();
        String gitDate = "";
        try {
            gitDate = loadGitPropertiesUtil.readGitProperties();
        } catch (IOException iOException) {
        }

        return gitDate;
    }
}

