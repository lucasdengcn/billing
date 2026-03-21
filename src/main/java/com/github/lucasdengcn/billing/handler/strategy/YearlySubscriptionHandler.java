package com.github.lucasdengcn.billing.handler.strategy;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
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

        // Set default periods if not provided
        if (null == subscription.getStartDate()){
            subscription.setStartDate(OffsetDateTime.now());
        }
        if (null == subscription.getEndDate()) {
            subscription.setEndDate(subscription.getStartDate().plusYears(1));
            subscription.setPeriods(1);
            subscription.setPeriodUnit(PeriodUnit.YEARS);
        } else {
            long totalDays = Duration.between(subscription.getStartDate(), subscription.getEndDate()).toDays();
            int years = (int) (totalDays / 365);
            subscription.setPeriods(years);
            subscription.setPeriodUnit(PeriodUnit.YEARS);
        }

        // Validate date logic
        if (subscription.getStartDate().isAfter(subscription.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        subscription.setBaseFee(product.getBasePrice());
        subscription.setDiscountRate(product.getDiscountRate());
        // Calculate total fee
        BigDecimal totalPrice = pricingCalculator.calculateSubscriptionTotalFee(product, subscription);
        subscription.setTotalFee(totalPrice);
    }
}