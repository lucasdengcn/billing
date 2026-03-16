package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionRenewal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRenewalRepository extends JpaRepository<SubscriptionRenewal, Long> {
    List<SubscriptionRenewal> findBySubscription(Subscription subscription);
}
