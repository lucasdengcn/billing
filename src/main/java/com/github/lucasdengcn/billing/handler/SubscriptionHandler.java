package com.github.lucasdengcn.billing.handler;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;

public interface SubscriptionHandler {
    /**
     * Handle a new subscription.
     * @param product
     * @param subscription
     * @return
     */
    void handleNew(Product product, Subscription subscription);
}