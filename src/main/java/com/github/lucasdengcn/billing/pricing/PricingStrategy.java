package com.github.lucasdengcn.billing.pricing;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;

import java.math.BigDecimal;

/**
 * Interface defining the contract for different pricing strategies.
 */
public interface PricingStrategy {
    
    /**
     * Calculate the price for a product.
     * 
     * @param product The product to calculate the price for
     * @return The calculated price
     */
    BigDecimal calculateProductPrice(Product product);
    
    /**
     * Calculate the price for a subscription.
     * 
     * @param subscription The subscription to calculate the price for
     * @return The calculated price
     */
    BigDecimal calculateSubscriptionPrice(Subscription subscription);
}