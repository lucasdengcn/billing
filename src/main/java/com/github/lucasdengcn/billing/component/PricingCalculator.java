package com.github.lucasdengcn.billing.component;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionRenewal;

import java.math.BigDecimal;

/**
 * Component responsible for calculating fees for products and subscriptions using strategy pattern.
 */
public interface PricingCalculator {
    
    /**
     * Calculates the discounted price for a product based on its base price and discount rate,
     * using the appropriate pricing strategy based on the product type.
     * 
     * @param product The product to calculate the fee for
     * @return The calculated discounted price
     */
    BigDecimal calculateProductTotalFee(Product product);
    
    /**
     * Calculates the total fee for an existing subscription based on its base fee and discount rate,
     * using the appropriate pricing strategy based on the subscription's product type.
     * 
     * @param subscription The subscription to calculate the fee for
     * @return The calculated total fee for the subscription
     */
    BigDecimal calculateSubscriptionTotalFee(Product product, Subscription subscription);
    
    /**
     * Calculates the total fee for a subscription renewal based on the renewal periods and unit,
     * using the appropriate pricing strategy based on the subscription's product type.
     * 
     * @param product The product associated with the subscription
     * @param renewal The subscription renewal details
     * @return The calculated total fee for the renewal
     */
    BigDecimal calculateRenewalTotalFee(Product product, SubscriptionRenewal renewal);
}