package com.yiyi.service.gateway.auth;

import java.io.Serializable;

public class TokenUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer accountId;
    private Integer employeeId;
    private String name;
    private String username;
    private String clientId;

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String toString() {
        return "TokenUser(accountId=" + getAccountId() + ", employeeId=" + getEmployeeId() + ", name=" + getName() + ", username=" + getUsername() + ", clientId=" + getClientId() + ")";
    }


    public Integer getAccountId() {
        return this.accountId;
    }

    public Integer getEmployeeId() {
        return this.employeeId;
    }

    public String getName() {
        return this.name;
    }

    public String getUsername() {
        return this.username;
    }

    public String getClientId() {
        return this.clientId;
    }
}
