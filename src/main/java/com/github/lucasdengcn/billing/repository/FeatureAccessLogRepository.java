package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.FeatureAccessLog;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.AccessDetailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface FeatureAccessLogRepository extends JpaRepository<FeatureAccessLog, Long> {
    Page<FeatureAccessLog> findBySubscriptionId(Long subscriptionId, Pageable pageable);
    
    Page<FeatureAccessLog> findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(Long subscriptionId, Long productFeatureId, Pageable pageable);
    
    Page<FeatureAccessLog> findByDeviceIdOrderByAccessTimeDesc(Long deviceId, Pageable pageable);
    
    Page<FeatureAccessLog> findBySubscriptionIdOrderByAccessTimeDesc(Long subscriptionId, Pageable pageable);
}