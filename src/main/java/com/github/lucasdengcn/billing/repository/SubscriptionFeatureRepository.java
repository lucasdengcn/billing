package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionFeatureRepository extends JpaRepository<SubscriptionFeature, Long> {
    List<SubscriptionFeature> findBySubscription(Subscription subscription);
    Optional<SubscriptionFeature> findBySubscriptionAndProductFeature(Subscription subscription, ProductFeature productFeature);
}
