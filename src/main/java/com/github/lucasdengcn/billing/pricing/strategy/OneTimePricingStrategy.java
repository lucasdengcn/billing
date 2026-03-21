package com.github.lucasdengcn.billing.pricing.strategy;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.pricing.PricingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing strategy for one-time payment products and subscriptions.
 */
public class OneTimePricingStrategy implements PricingStrategy {
    
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 4;
    
    @Override
    public BigDecimal calculateProductPrice(Product product) {
        if (product == null || product.getBasePrice() == null) {
            throw new IllegalArgumentException("Product and basePrice cannot be null");
        }
        
        // Ensure this strategy is used for one-time products
        if (product.getPriceType() != PriceType.ONE_TIME && product.getPriceType() != null) {
            throw new IllegalArgumentException("OneTimePricingStrategy should only be used for ONE_TIME products");
        }
        
        BigDecimal basePrice = product.getBasePrice();
        BigDecimal discountRate = product.getDiscountRate() != null ? product.getDiscountRate() : DEFAULT_DISCOUNT_RATE;
        
        return basePrice.multiply(discountRate).setScale(SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateSubscriptionPrice(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        BigDecimal baseFee = subscription.getBaseFee() != null ? subscription.getBaseFee() : ZERO;
        BigDecimal discountRate = subscription.getDiscountRate() != null ? subscription.getDiscountRate() : DEFAULT_DISCOUNT_RATE;
        
        // For one-time subscriptions, the fee is charged once regardless of periods
        return baseFee.multiply(discountRate).setScale(SCALE, RoundingMode.HALF_UP);
    }
}