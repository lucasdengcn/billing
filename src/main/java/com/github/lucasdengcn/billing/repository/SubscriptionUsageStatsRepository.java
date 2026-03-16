package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionUsageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionUsageStatsRepository extends JpaRepository<SubscriptionUsageStats, Long> {
    List<SubscriptionUsageStats> findBySubscription(Subscription subscription);
    Optional<SubscriptionUsageStats> findBySubscriptionAndProductFeature(Subscription subscription, ProductFeature productFeature);
}
