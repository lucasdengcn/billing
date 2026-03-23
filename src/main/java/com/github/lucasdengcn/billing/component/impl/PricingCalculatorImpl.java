package com.github.lucasdengcn.billing.component.impl;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;

import com.github.lucasdengcn.billing.entity.SubscriptionRenewal;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * Implementation of the PricingCalculator interface that uses strategy pattern
 * to handle fee calculations for products and subscriptions based on their pricing type.
 */
@Component
public class PricingCalculatorImpl implements PricingCalculator {

    private static final int SCALE = 2; // Match database precision (19, 4)
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculateProductTotalFee(Product product) {
        if (product == null || product.getBasePrice() == null) {
            throw new IllegalArgumentException("Product and basePrice cannot be null");
        }

        BigDecimal basePrice = product.getBasePrice();
        BigDecimal discountRate = product.getDiscountRate();

        return basePrice.multiply(discountRate).setScale(SCALE, ROUNDING_MODE);
    }
    
    @Override
    public BigDecimal calculateSubscriptionTotalFee(Product product, Subscription subscription, int amount) {

        if (product == null || product.getBasePrice() == null || product.getDiscountRate() == null) {
            throw new IllegalArgumentException("Product Invalid");
        }

        if (subscription == null) {
            throw new IllegalArgumentException("Subscription Invalid");
        }

        // Set base fee and discount rate from product if not provided in request
        BigDecimal baseFee = product.getBasePrice();
        BigDecimal discountRate = product.getDiscountRate();

        // For monthly subscriptions, calculate based on number of months
        BigDecimal totalPrice = baseFee.multiply(discountRate).multiply(new BigDecimal(amount)).setScale(SCALE, ROUNDING_MODE);
        subscription.setTotalFee(totalPrice);
        return totalPrice;
    }
    
    @Override
    public BigDecimal calculateRenewalTotalFee(Product product, SubscriptionRenewal renewal) {
        if (renewal == null || renewal.getBaseFee() == null || renewal.getDiscountRate() == null) {
            throw new IllegalArgumentException("SubscriptionRenewal and required fields cannot be null");
        }
        
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        BigDecimal baseFee = renewal.getBaseFee();
        BigDecimal discountRate = renewal.getDiscountRate();
        
        // Get renewal periods from renewal object
        int renewalPeriods = renewal.getRenewalPeriods() != null ? renewal.getRenewalPeriods() : 1;
        
        // Calculate renewal fee based on renewal periods
        BigDecimal renewalFee = baseFee.multiply(discountRate).multiply(new BigDecimal(renewalPeriods))
            .setScale(SCALE, RoundingMode.HALF_UP);
        renewal.setTotalFee(renewalFee);
        return renewalFee;
    }
}