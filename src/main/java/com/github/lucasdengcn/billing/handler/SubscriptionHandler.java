package com.github.lucasdengcn.billing.handler;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionRenewal;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;

public interface SubscriptionHandler {
    /**
     * Handle a new subscription.
     * @param product
     * @param subscription
     */
    void handleNew(Product product, Subscription subscription);
    
    /**
     * Handle subscription renewal.
     * @param product
     * @param subscription
     * @param renewal subscription renewal details
     */
    void handleRenewal(Product product, Subscription subscription, SubscriptionRenewal renewal);
}