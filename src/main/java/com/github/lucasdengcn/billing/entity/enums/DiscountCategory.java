package com.github.lucasdengcn.billing.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing different categories or types of discounts.
 * This complements the discount rate which is stored as a BigDecimal value.
 */
public enum DiscountCategory {
    PROMOTIONAL("promotional"),
    SEASONAL("seasonal"),
    LOYALTY("loyalty"),
    BULK_PURCHASE("bulk_purchase"),
    REFERRAL("referral"),
    EARLY_BIRD("early_bird"),
    STUDENT("student"),
    NON_PROFIT("non_profit"),
    CORPORATE("corporate"),
    COMPETITIVE("competitive"),
    RETENTION("retention"),
    CUSTOM("custom");
    
    private final String value;
    
    DiscountCategory(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }

    @JsonValue
    public String toJson() {
        return this.getValue();
    }

}