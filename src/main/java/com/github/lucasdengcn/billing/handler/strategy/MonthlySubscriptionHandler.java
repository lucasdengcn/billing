package com.github.lucasdengcn.billing.handler.strategy;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.exception.InvalidSubscriptionDateRangeException;
import com.github.lucasdengcn.billing.handler.SubscriptionHandler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Pricing strategy for monthly-based products and subscriptions.
 */
public class MonthlySubscriptionHandler implements SubscriptionHandler {

    private final PricingCalculator pricingCalculator;

    public MonthlySubscriptionHandler(PricingCalculator pricingCalculator) {
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
            subscription.setEndDate(subscription.getStartDate().plusMonths(1));
            subscription.setPeriods(1);
            subscription.setPeriodUnit(PeriodUnit.MONTHS);
        } else {
            long totalDays = Duration.between(subscription.getStartDate(), subscription.getEndDate()).toDays();
            int months = (int) (totalDays / 30);
            subscription.setPeriods(months);
            subscription.setPeriodUnit(PeriodUnit.MONTHS);
        }

        // Validate date logic
        if (subscription.getStartDate().isAfter(subscription.getEndDate())) {
            throw new InvalidSubscriptionDateRangeException();
        }

        subscription.setBaseFee(product.getBasePrice());
        subscription.setDiscountRate(product.getDiscountRate());
        // Calculate total fee
        BigDecimal totalPrice = pricingCalculator.calculateSubscriptionTotalFee(product, subscription);
        subscription.setTotalFee(totalPrice);
    }
}