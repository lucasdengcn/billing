package com.github.lucasdengcn.billing.service.impl;

import java.util.List;
import java.util.Optional;
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
    private final DeviceService deviceService;
    private final SubscriptionRepository subscriptionRepository;

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

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureAccessLog> getFeatureUsageLogs(String deviceNo, String productNo, String featureNo, Pageable pageable) {
        // Find the subscription feature to get the IDs needed for filtering logs
        SubscriptionFeature subscriptionFeature = subscriptionService.
                findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo(deviceNo, featureNo, productNo);
        
        if (subscriptionFeature == null) {
            throw new ResourceNotFoundException("Subscription feature not found for device: " + deviceNo + 
                    ", feature: " + featureNo + ", product: " + productNo);
        }
        
        // Query logs by the subscription ID and product feature ID, ordered by access time descending
        return logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(
                subscriptionFeature.getSubscription().getId(), 
                subscriptionFeature.getProductFeature().getId(), 
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureAccessLog> getFeatureUsageLogsByDevice(String deviceNo, Pageable pageable) {
        // Find device by device number
        Device device = deviceService.findByDeviceNo(deviceNo);
        if (device == null) {
            throw new ResourceNotFoundException("Device not found: " + deviceNo);
        }
        
        // Query logs by device, ordered by access time descending
        return logRepository.findByDeviceIdOrderByAccessTimeDesc(device.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureAccessLog> getFeatureUsageLogsBySubscription(Long subscriptionId, Pageable pageable) {
        // Find subscription by ID
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
        if (subscriptionOpt.isEmpty()) {
            throw new ResourceNotFoundException("Subscription not found: " + subscriptionId);
        }
        
        // Query logs by subscription ID, ordered by access time descending
        return logRepository.findBySubscriptionIdOrderByAccessTimeDesc(subscriptionId, pageable);
    }
}