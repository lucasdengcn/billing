package com.github.lucasdengcn.billing.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import com.github.lucasdengcn.billing.service.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.repository.FeatureAccessLogRepository;
import com.github.lucasdengcn.billing.repository.ProductFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionUsageStatsRepository;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.FeatureAccessService;
import com.github.lucasdengcn.billing.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeatureAccessServiceImpl implements FeatureAccessService {

    private final FeatureAccessLogRepository logRepository;
    private final SubscriptionUsageStatsRepository statsRepository;
    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public FeatureAccessLog trackFeatureUsage(FeatureUsageTrackingRequest request) {
        SubscriptionFeature subscriptionFeature = subscriptionService.
                findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo(request.getDeviceNo(),
                        request.getFeatureNo(),
                        request.getProductNo());
        if (subscriptionFeature == null) {
            throw new ResourceNotFoundException("Subscription feature not found for device: " + request.getDeviceNo() + ", feature: " + request.getFeatureNo() + ", product: " + request.getProductNo());
        }
        // Create feature access log
        FeatureAccessLog accessLog = FeatureAccessLog.builder()
                .subscription(subscriptionFeature.getSubscription())
                .productFeature(subscriptionFeature.getProductFeature())
                .device(subscriptionFeature.getDevice())
                .usageAmount(request.getUsageAmount())
                .detailValue(request.getDetailValue())
                .build();

        // Log the access
        return logRepository.save(accessLog);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<FeatureAccessLog> trackFeatureUsageAsync(FeatureUsageTrackingRequest request) {
        try {
            FeatureAccessLog savedLog = trackFeatureUsage(request);
            return CompletableFuture.completedFuture(savedLog);
        } catch (Exception e) {
            log.error("Error tracking feature usage asynchronously", e);
            throw e;
        }
    }
}