package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.repository.FeatureAccessLogRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionUsageStatsRepository;
import com.github.lucasdengcn.billing.service.FeatureAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FeatureAccessServiceImpl implements FeatureAccessService {

    private final FeatureAccessLogRepository logRepository;
    private final SubscriptionUsageStatsRepository statsRepository;

    @Override
    public FeatureAccessLog logAccess(FeatureAccessLog log) {
        return logRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureAccessLog> findLogsBySubscription(Subscription subscription, Pageable pageable) {
        return logRepository.findBySubscription(subscription, pageable);
    }

    @Override
    public SubscriptionUsageStats updateUsageStats(SubscriptionUsageStats stats) {
        return statsRepository.save(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionUsageStats> findStatsBySubscriptionAndFeature(Subscription subscription, ProductFeature feature) {
        return statsRepository.findBySubscriptionAndProductFeature(subscription, feature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionUsageStats> findStatsBySubscription(Subscription subscription) {
        return statsRepository.findBySubscription(subscription);
    }
}
