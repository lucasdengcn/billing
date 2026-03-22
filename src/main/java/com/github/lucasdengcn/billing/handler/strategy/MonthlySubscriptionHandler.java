package com.github.lucasdengcn.billing.handler.strategy;

import com.github.lucasdengcn.billing.component.PricingCalculator;
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
    
    @Override
    public void handleRenewal(Product product, Subscription subscription, SubscriptionRenewal renewal) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        if (renewal == null) {
            throw new IllegalArgumentException("Renewal cannot be null");
        }
        
        // Validate renewal period unit is compatible (should be month-based)
        PeriodUnit renewalPeriodUnit = renewal.getRenewalPeriodUnit();
        if (renewalPeriodUnit != PeriodUnit.MONTHS && renewalPeriodUnit != PeriodUnit.DAYS && 
            renewalPeriodUnit != PeriodUnit.WEEKS) {
            throw new IllegalArgumentException("Invalid period unit for monthly subscription renewal");
        }
        
        // Use renewal periods from renewal object or default to 1
        int periods = (renewal.getRenewalPeriods() != null && renewal.getRenewalPeriods() > 0) ? renewal.getRenewalPeriods() : 1;
        
        // Extend the subscription end date from the current end date
        OffsetDateTime currentEndDate = subscription.getEndDate();
        if (currentEndDate.isBefore(OffsetDateTime.now())) {
            currentEndDate = OffsetDateTime.now();
        }
        OffsetDateTime newEndDate = calculateRenewalEndDate(currentEndDate, periods, renewalPeriodUnit);
        
        // Update subscription with new end date
        subscription.setEndDate(newEndDate);
        subscription.setPeriods(subscription.getPeriods() + translatePeriodsToMonths(periods, renewalPeriodUnit));
        
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
    
    private OffsetDateTime calculateRenewalEndDate(OffsetDateTime currentEndDate, int periods, PeriodUnit periodUnit) {
        switch (periodUnit) {
            case DAYS:
                return currentEndDate.plusDays(periods);
            case WEEKS:
                return currentEndDate.plusWeeks(periods);
            case MONTHS:
                return currentEndDate.plusMonths(periods);
            case YEARS:
                return currentEndDate.plusYears(periods);
            default:
                return currentEndDate.plusMonths(periods); // Default to months
        }
    }

    private int translatePeriodsToMonths(int periods, PeriodUnit periodUnit) {
        switch (periodUnit) {
            case YEARS:
                return periods * 12;
            default:
                return periods;
        }
    }

}