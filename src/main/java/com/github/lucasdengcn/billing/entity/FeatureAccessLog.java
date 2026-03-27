package com.github.lucasdengcn.billing.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feature_access_logs")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FeatureAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "product_feature_id", nullable = false)
    private Long productFeatureId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "usage_amount", nullable = false)
    @Builder.Default
    private Integer usageAmount = 1;

    @Column(name = "access_time", nullable = false)
    @Builder.Default
    private OffsetDateTime accessTime = OffsetDateTime.now();

    @Column(name = "detail_value", columnDefinition = "TEXT")
    private String detailValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

}