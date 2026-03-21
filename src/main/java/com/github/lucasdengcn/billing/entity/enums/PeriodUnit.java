package com.github.lucasdengcn.billing.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the unit of time for a subscription period.
 */
public enum PeriodUnit {
    DAYS("days"),
    WEEKS("weeks"),
    MONTHS("months"),
    YEARS("years");
    
    private final String value;
    
    PeriodUnit(String value) {
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