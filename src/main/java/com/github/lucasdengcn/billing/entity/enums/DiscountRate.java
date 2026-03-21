package com.github.lucasdengcn.billing.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;

/**
 * Enum representing common discount rate categories.
 * While discount rates are stored as BigDecimal values (0.0000 to 1.0000),
 * this enum provides predefined common discount percentages for convenience.
 */
public enum DiscountRate {
    NO_DISCOUNT(new BigDecimal("1.0000")),           // 0% discount
    FIVE_PERCENT_OFF(new BigDecimal("0.9500")),       // 5% discount
    TEN_PERCENT_OFF(new BigDecimal("0.9000")),        // 10% discount
    FIFTEEN_PERCENT_OFF(new BigDecimal("0.8500")),    // 15% discount
    TWENTY_PERCENT_OFF(new BigDecimal("0.8000")),     // 20% discount
    TWENTY_FIVE_PERCENT_OFF(new BigDecimal("0.7500")), // 25% discount
    THIRTY_PERCENT_OFF(new BigDecimal("0.7000")),     // 30% discount
    FORTY_PERCENT_OFF(new BigDecimal("0.6000")),      // 40% discount
    FIFTY_PERCENT_OFF(new BigDecimal("0.5000")),      // 50% discount
    SEVENTY_FIVE_PERCENT_OFF(new BigDecimal("0.2500")), // 75% discount
    HUNDRED_PERCENT_OFF(new BigDecimal("0.0000"));    // 100% discount (free)
    
    private final BigDecimal rate;
    
    DiscountRate(BigDecimal rate) {
        this.rate = rate;
    }
    
    @JsonValue
    public BigDecimal getRate() {
        return rate;
    }
    
    public static DiscountRate fromRate(BigDecimal rate) {
        for (DiscountRate discountRate : DiscountRate.values()) {
            if (discountRate.getRate().compareTo(rate) == 0) {
                return discountRate;
            }
        }
        throw new IllegalArgumentException("No DiscountRate found for rate: " + rate);
    }
    
    @Override
    public String toString() {
        return rate.toString();
    }
}