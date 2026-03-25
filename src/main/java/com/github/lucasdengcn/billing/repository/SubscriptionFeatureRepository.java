package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionFeature;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.github.lucasdengcn.billing.repository.SubscriptionFeatureProjection;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureProjectionImpl;

@Repository
public interface SubscriptionFeatureRepository extends JpaRepository<SubscriptionFeature, Long> {

    List<SubscriptionFeature> findBySubscription(Subscription subscription);

    Optional<SubscriptionFeature> findBySubscriptionAndProductFeature(Subscription subscription, ProductFeature productFeature);
    
    @Query("SELECT sf FROM SubscriptionFeature sf LEFT JOIN FETCH sf.subscription s LEFT JOIN FETCH sf.productFeature pf LEFT JOIN FETCH sf.device d WHERE sf.trackId = :trackId")
    Optional<SubscriptionFeature> findByTrackIdWithRelatedEntities(String trackId);
    
    @Query("SELECT sf FROM SubscriptionFeature sf LEFT JOIN FETCH sf.subscription s LEFT JOIN FETCH sf.productFeature pf LEFT JOIN FETCH sf.device d WHERE sf.trackId = :trackId")
    SubscriptionFeature findByTrackId(String trackId);
    
    @Query("SELECT sf FROM SubscriptionFeature sf LEFT JOIN FETCH sf.subscription s LEFT JOIN FETCH sf.productFeature pf LEFT JOIN FETCH sf.device d WHERE sf.trackId = :trackId")
    Optional<SubscriptionFeature> findByTrackIdOptional(String trackId);
    
    @Modifying
    @Query("UPDATE SubscriptionFeature sf SET sf.balance = sf.balance - :usageAmount, sf.accessed = sf.accessed + :usageAmount WHERE sf.trackId = :trackId")
    int updateBalanceAndAccessed(@Param("trackId") String trackId, @Param("usageAmount") Integer usageAmount);

    @Query("SELECT new com.github.lucasdengcn.billing.repository.SubscriptionFeatureProjectionImpl(sf.id, sf.trackId, sf.subscription.id, sf.productFeature.id, sf.device.id, sf.title, sf.quota, sf.accessed, sf.balance) FROM SubscriptionFeature sf WHERE sf.trackId = :trackId")
    Optional<SubscriptionFeatureProjection> findProjectionByTrackId(String trackId);
}