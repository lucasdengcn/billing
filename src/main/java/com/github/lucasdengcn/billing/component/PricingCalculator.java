package com.github.lucasdengcn.billing.component;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;

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
    BigDecimal calculateSubscriptionTotalFee(Subscription subscription);
    
    /**
     * Calculates a custom total fee based on provided base fee and discount rate.
     * 
     * @param baseFee The base fee amount
     * @param discountRate The discount rate to apply
     * @return The calculated total fee
     */
    BigDecimal calculateCustomTotalFee(BigDecimal baseFee, BigDecimal discountRate);
}