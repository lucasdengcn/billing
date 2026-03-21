package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {
    Subscription findSubscriptionById(Long id);
    List<Subscription> findSubscriptionsByCustomer(@NonNull Customer customer);
    void deleteSubscriptionById(Long id);
    void createSubscriptionFeaturesFromProduct(@NonNull Subscription subscription);
    Subscription createSubscription(@NonNull SubscriptionRequest request);

    List<SubscriptionFeature> findFeaturesBySubscription(@NonNull Subscription subscription);

    SubscriptionRenewal saveRenewal(@NonNull SubscriptionRenewal renewal);
    List<SubscriptionRenewal> findRenewalsBySubscription(@NonNull Subscription subscription);
}