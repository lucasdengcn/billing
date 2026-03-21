package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {
    Subscription saveSubscription(Subscription subscription);
    Subscription findSubscriptionById(Long id);
    List<Subscription> findSubscriptionsByCustomer(Customer customer);
    List<Subscription> findSubscriptionsByStatus(SubscriptionStatus status);
    void deleteSubscriptionById(Long id);
    void createSubscriptionFeaturesFromProduct(Subscription subscription);
    Subscription createSubscription(SubscriptionRequest request);

    SubscriptionFeature saveSubscriptionFeature(SubscriptionFeature feature);
    List<SubscriptionFeature> findFeaturesBySubscription(Subscription subscription);

    SubscriptionRenewal saveRenewal(SubscriptionRenewal renewal);
    List<SubscriptionRenewal> findRenewalsBySubscription(Subscription subscription);
}