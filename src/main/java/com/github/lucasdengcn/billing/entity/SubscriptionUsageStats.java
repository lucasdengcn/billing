package com.github.lucasdengcn.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_usage_stats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscription_id", "product_feature_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUsageStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    @ToString.Exclude
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    @ToString.Exclude
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_feature_id", nullable = false)
    @ToString.Exclude
    private ProductFeature productFeature;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @Column(name = "total_usage", nullable = false)
    @Builder.Default
    private Integer totalUsage = 0;

    @Column(name = "last_usage_time")
    private OffsetDateTime lastUsageTime;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
