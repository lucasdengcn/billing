package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FeatureAccessService {
    FeatureAccessLog logAccess(FeatureAccessLog log);
    Page<FeatureAccessLog> findLogsBySubscription(Subscription subscription, Pageable pageable);

    SubscriptionUsageStats updateUsageStats(SubscriptionUsageStats stats);
    Optional<SubscriptionUsageStats> findStatsBySubscriptionAndFeature(Subscription subscription, ProductFeature feature);
    List<SubscriptionUsageStats> findStatsBySubscription(Subscription subscription);
}
