package com.github.lucasdengcn.billing.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PriceType {
    MONTHLY("monthly"),
    YEARLY("yearly"),
    ONE_TIME("one_time"),
    USAGE_BASED("usage_based"),
    CUSTOM("custom");
    
    private final String value;
    
    PriceType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value.toLowerCase();
    }
    
    @Override
    public String toString() {
        return value.toLowerCase();
    }
}