package com.github.lucasdengcn.billing.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.entity.FeatureAccessLog;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionUsageStats;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.repository.FeatureAccessLogRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionUsageStatsRepository;
import com.github.lucasdengcn.billing.service.FeatureAccessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeatureAccessServiceImpl implements FeatureAccessService {

    private final FeatureAccessLogRepository logRepository;
    private final SubscriptionUsageStatsRepository statsRepository;

    @Override
    public FeatureAccessLog logAccess(FeatureAccessLog accessLog) {
        log.info("Logging access for subscription: {} and feature: {}",
                accessLog.getSubscription().getId(), accessLog.getProductFeature().getId());
        return logRepository.save(accessLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureAccessLog> findLogsBySubscription(Subscription subscription, Pageable pageable) {
        log.debug("Finding access logs for subscription: {}", subscription.getId());
        return logRepository.findBySubscription(subscription, pageable);
    }

    @Override
    public SubscriptionUsageStats updateUsageStats(SubscriptionUsageStats stats) {
        log.info("Updating usage stats for subscription: {} and feature: {}",
                stats.getSubscription().getId(), stats.getProductFeature().getId());
        return statsRepository.save(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionUsageStats findStatsBySubscriptionAndFeature(Subscription subscription, ProductFeature feature) {
        log.debug("Finding usage stats for subscription: {} and feature: {}", subscription.getId(), feature.getId());
        return statsRepository.findBySubscriptionAndProductFeature(subscription, feature)
                .orElseThrow(() -> new ResourceNotFoundException("Usage stats not found for subscription: "
                        + subscription.getId() + " and feature: " + feature.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionUsageStats> findStatsBySubscription(Subscription subscription) {
        log.debug("Finding all usage stats for subscription: {}", subscription.getId());
        return statsRepository.findBySubscription(subscription);
    }
}
