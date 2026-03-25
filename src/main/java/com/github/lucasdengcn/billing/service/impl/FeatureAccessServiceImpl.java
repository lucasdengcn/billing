package com.github.lucasdengcn.billing.service.impl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingByTrackIdRequest;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import com.github.lucasdengcn.billing.repository.*;
import com.github.lucasdengcn.billing.service.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.FeatureAccessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeatureAccessServiceImpl implements FeatureAccessService {

    private final FeatureAccessLogRepository logRepository;
    private final SubscriptionService subscriptionService;
    private final DeviceService deviceService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionFeatureRepository subscriptionFeatureRepository;

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
        // Update balance and accessed
        subscriptionFeatureRepository.updateBalanceAndAccessed(subscriptionFeature.getTrackId(), request.getUsageAmount());
        // Create feature access log
        FeatureAccessLog accessLog = FeatureAccessLog.builder()
                .subscriptionId(subscriptionFeature.getSubscription().getId())
                .productFeatureId(subscriptionFeature.getProductFeature().getId())
                .deviceId(subscriptionFeature.getDevice().getId())
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

    @Override
    @Transactional
    public FeatureAccessLog trackFeatureUsageByTrackId(String trackId, FeatureUsageTrackingByTrackIdRequest request) {
        Integer usageAmount = request.getUsageAmount();
        SubscriptionFeatureProjection subscriptionFeature = findSubscriptionFeatureByTrackId(trackId);

        // Update balance and accessed amounts using JPQL
        int rowsUpdated = subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, usageAmount);
        
        if (rowsUpdated == 0) {
            throw new IllegalArgumentException("Insufficient balance for trackId.");
        }

        // Create feature access log
        FeatureAccessLog accessLog = FeatureAccessLog.builder()
                .subscriptionId(subscriptionFeature.subscriptionId())
                .productFeatureId(subscriptionFeature.productFeatureId())
                .deviceId(subscriptionFeature.deviceId())
                .usageAmount(request.getUsageAmount())
                .detailValue(request.getDetailValue())
                .build();

        // Log the access
        return logRepository.save(accessLog);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<FeatureAccessLog> trackFeatureUsageByTrackIdAsync(String trackId, FeatureUsageTrackingByTrackIdRequest request) {
        try {
            FeatureAccessLog savedLog = trackFeatureUsageByTrackId(trackId, request);
            return CompletableFuture.completedFuture(savedLog);
        } catch (Exception e) {
            log.error("Error tracking feature usage by trackId asynchronously", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureAccessLog> getFeatureUsageLogsByTrackId(String trackId, Pageable pageable) {
        // Find subscription feature by track ID to get the subscription ID
        SubscriptionFeatureProjection subscriptionFeature = findSubscriptionFeatureByTrackId(trackId);
        
        // Query logs by subscription ID
        return logRepository.findBySubscriptionIdOrderByAccessTimeDesc(
                subscriptionFeature.subscriptionId(), pageable);
    }
    
    private SubscriptionFeatureProjection findSubscriptionFeatureByTrackId(String trackId) {
        Optional<SubscriptionFeatureProjection> subscriptionFeatureOpt = subscriptionFeatureRepository.findProjectionByTrackId(trackId);
        if (subscriptionFeatureOpt.isEmpty()) {
            throw new ResourceNotFoundException("Subscription feature not found for trackId: " + trackId);
        }
        return subscriptionFeatureOpt.get();
    }

}