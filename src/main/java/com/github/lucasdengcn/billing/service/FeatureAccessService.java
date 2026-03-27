package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingByTrackIdRequest;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FeatureAccessService {
    /**
     * Track feature usage by device number, product number, and feature number
     */
    FeatureAccessLog trackFeatureUsage(FeatureUsageTrackingRequest request);
    /**
     * Get feature usage logs by device number, product number, and feature number
     */
    Page<FeatureAccessLog> getFeatureUsageLogs(String deviceNo, String productNo, String featureNo, Pageable pageable);
    
    /**
     * Get feature usage logs by device number
     */
    Page<FeatureAccessLog> getFeatureUsageLogsByDevice(String deviceNo, Pageable pageable);
    
    /**
     * Get feature usage logs by subscription ID
     */
    Page<FeatureAccessLog> getFeatureUsageLogsBySubscription(Long subscriptionId, Pageable pageable);
    
    /**
     * Track feature usage by track ID
     */
    FeatureAccessLog trackFeatureUsageByTrackId(String trackId, FeatureUsageTrackingByTrackIdRequest request);
    /**
     * Get feature usage logs by track ID
     */
    Page<FeatureAccessLog> getFeatureUsageLogsByTrackId(String trackId, Pageable pageable);
}