package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.SubscriptionRenewalRequest;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionWithFeaturesResponse;
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

    Subscription cancelSubscription(Long customerId, Long deviceId, Long productId);
    
    List<Subscription> findSubscriptionsByDeviceNo(String deviceNo);

    SubscriptionWithFeaturesResponse findSubscriptionByDeviceNoAndProductNo(String deviceNo, String productNo);

    SubscriptionWithFeaturesResponse findSubscriptionWithFeaturesById(Long id);

    SubscriptionFeature findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo(String deviceNo, String featureNo, String productNo);

    Subscription renewSubscription(@NonNull SubscriptionRenewalRequest request);
}