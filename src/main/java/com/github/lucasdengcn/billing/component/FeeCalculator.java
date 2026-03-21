package com.github.lucasdengcn.billing.component;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;

import java.math.BigDecimal;

/**
 * Component responsible for calculating fees for products and subscriptions.
 */
public interface FeeCalculator {
    
    /**
     * Calculates the discounted price for a product based on its base price and discount rate.
     * 
     * @param product The product to calculate the fee for
     * @return The calculated discounted price
     */
    BigDecimal calculateProductTotalFee(Product product);
    
    /**
     * Calculates the total fee for a subscription based on the subscription request and associated product.
     * 
     * @param request The subscription request containing fee and discount information
     * @param product The product associated with the subscription
     * @return The calculated total fee for the subscription
     */
    BigDecimal calculateSubscriptionTotalFee(SubscriptionRequest request, Product product);
    
    /**
     * Calculates the total fee for an existing subscription based on its base fee and discount rate.
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