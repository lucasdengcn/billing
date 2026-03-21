package com.github.lucasdengcn.billing.component.impl;

import com.github.lucasdengcn.billing.component.FeeCalculator;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of the FeeCalculator interface that handles fee calculations for products and subscriptions.
 */
@Component
public class FeeCalculatorImpl implements FeeCalculator {
    
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 4; // Match database precision (19, 4)
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Override
    public BigDecimal calculateProductTotalFee(Product product) {
        if (product == null || product.getBasePrice() == null) {
            throw new IllegalArgumentException("Product and basePrice cannot be null");
        }
        
        BigDecimal basePrice = product.getBasePrice();
        BigDecimal discountRate = product.getDiscountRate() != null ? product.getDiscountRate() : DEFAULT_DISCOUNT_RATE;
        
        return basePrice.multiply(discountRate).setScale(SCALE, ROUNDING_MODE);
    }
    
    @Override
    public BigDecimal calculateSubscriptionTotalFee(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        BigDecimal baseFee = subscription.getBaseFee() != null ? subscription.getBaseFee() : ZERO;
        BigDecimal discountRate = subscription.getDiscountRate() != null ? subscription.getDiscountRate() : DEFAULT_DISCOUNT_RATE;
        int periods = subscription.getPeriods() != null ? subscription.getPeriods() : 1;
        
        return baseFee.multiply(discountRate).multiply(new BigDecimal(periods)).setScale(SCALE, ROUNDING_MODE);
    }
    
    @Override
    public BigDecimal calculateCustomTotalFee(BigDecimal baseFee, BigDecimal discountRate) {
        if (baseFee == null) {
            throw new IllegalArgumentException("Base fee cannot be null");
        }
        if (discountRate == null) {
            throw new IllegalArgumentException("Discount rate cannot be null");
        }
        
        return baseFee.multiply(discountRate).setScale(SCALE, ROUNDING_MODE);
    }
}