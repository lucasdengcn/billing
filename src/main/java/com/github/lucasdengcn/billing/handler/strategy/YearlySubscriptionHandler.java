package com.github.lucasdengcn.billing.handler.strategy;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.component.TimeDurations;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionRenewal;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.exception.InvalidSubscriptionDateRangeException;
import com.github.lucasdengcn.billing.handler.SubscriptionHandler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Pricing strategy for yearly-based products and subscriptions.
 */
public class YearlySubscriptionHandler implements SubscriptionHandler {
    
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 4;

    private final PricingCalculator pricingCalculator;

    public YearlySubscriptionHandler(PricingCalculator pricingCalculator) {
        this.pricingCalculator = pricingCalculator;
    }
    
    @Override
    public void handleNew(Product product, Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        subscription.setPeriodUnit(PeriodUnit.YEARS);
        // Set default periods if not provided
        if (null == subscription.getStartDate()){
            subscription.setStartDate(OffsetDateTime.now());
        }

        if (null == subscription.getEndDate()) {
            subscription.setEndDate(subscription.getStartDate().plusYears(1));
        }

        // Validate date logic
        if (subscription.getStartDate().isAfter(subscription.getEndDate())) {
            throw new InvalidSubscriptionDateRangeException();
        }

        subscription.setBaseFee(product.getBasePrice());
        subscription.setDiscountRate(product.getDiscountRate());
        // Calculate total fee
        int amount = TimeDurations.translateDurationToUnits(subscription);
        BigDecimal totalPrice = pricingCalculator.calculateSubscriptionTotalFee(product, subscription, amount);
        subscription.setTotalFee(totalPrice);
    }
    
    @Override
    public void handleRenewal(Product product, Subscription subscription, SubscriptionRenewal renewal) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        if (renewal == null) {
            throw new IllegalArgumentException("Renewal cannot be null");
        }
        
        // Validate renewal period unit is compatible (should be year-based)
        PeriodUnit renewalPeriodUnit = renewal.getRenewalPeriodUnit();
        if (renewalPeriodUnit != PeriodUnit.YEARS && renewalPeriodUnit != PeriodUnit.MONTHS) {
            throw new IllegalArgumentException("Invalid period unit");
        }
        
        // Use renewal periods from renewal object or default to 1
        int periods = (renewal.getRenewalPeriods() != null && renewal.getRenewalPeriods() > 0) ? renewal.getRenewalPeriods() : 1;
        
        // Extend the subscription end date from the current end date
        OffsetDateTime currentEndDate = subscription.getEndDate();
        OffsetDateTime newEndDate = TimeDurations.renewalEndDate(currentEndDate, periods, renewalPeriodUnit);
        
        // Update subscription with new end date
        subscription.setEndDate(newEndDate);
        
        // Recalculate fees for renewal
        subscription.setBaseFee(product.getBasePrice());
        subscription.setDiscountRate(product.getDiscountRate());
        
        // Calculate renewal fee based on renewal period
        BigDecimal renewalFee = pricingCalculator.calculateRenewalTotalFee(product, renewal);
        subscription.setTotalFee(renewalFee.add(subscription.getTotalFee()));
        
        // Update renewal object with calculated values
        renewal.setNewEndDate(newEndDate);
        renewal.setTotalFee(renewalFee);
    }

}