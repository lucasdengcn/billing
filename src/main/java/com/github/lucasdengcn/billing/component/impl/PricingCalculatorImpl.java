package com.github.lucasdengcn.billing.component.impl;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.pricing.PricingStrategy;
import com.github.lucasdengcn.billing.pricing.strategy.PricingStrategyFactory;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of the PricingCalculator interface that uses strategy pattern
 * to handle fee calculations for products and subscriptions based on their pricing type.
 */
@Component
public class PricingCalculatorImpl implements PricingCalculator {
    
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 4; // Match database precision (19, 4)
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Override
    public BigDecimal calculateProductTotalFee(Product product) {
        if (product == null || product.getBasePrice() == null) {
            throw new IllegalArgumentException("Product and basePrice cannot be null");
        }
        
        // Use strategy pattern to calculate based on product type
        PricingStrategy strategy = PricingStrategyFactory.getStrategy(product.getPriceType());
        return strategy.calculateProductPrice(product);
    }
    
    @Override
    public BigDecimal calculateSubscriptionTotalFee(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        // Use strategy pattern to calculate based on subscription's product type
        PricingStrategy strategy = PricingStrategyFactory.getStrategy(
            subscription.getProduct() != null ? subscription.getProduct().getPriceType() : null);
        return strategy.calculateSubscriptionPrice(subscription);
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