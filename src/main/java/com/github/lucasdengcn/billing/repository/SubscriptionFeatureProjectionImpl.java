package com.github.lucasdengcn.billing.repository;

public record SubscriptionFeatureProjectionImpl(Long id, String trackId, Long subscriptionId, Long productFeatureId,
                                                Long deviceId, String title, Integer quota, Integer accessed,
                                                Integer balance) implements SubscriptionFeatureProjection {
}