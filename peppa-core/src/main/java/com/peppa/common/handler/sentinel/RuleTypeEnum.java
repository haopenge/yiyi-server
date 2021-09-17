package com.peppa.common.handler.sentinel;


public enum RuleTypeEnum {
    RULE_TYPE_FLOW(1,"flow"),

    RULE_TYPE_PARAMFLOW(2,"paramflow"),

    RULE_TYPE_DEGRADE(3,"degrade"),

    RULE_TYPE_SYS(4,"system"),

    RULE_TYPE_AUTH(5,"auth");


    private int value;
    private String str;

    RuleTypeEnum(int value, String str) {
        this.value = value;
        this.str = str;
    }

    public String getString() {
        return this.str;
    }
}

