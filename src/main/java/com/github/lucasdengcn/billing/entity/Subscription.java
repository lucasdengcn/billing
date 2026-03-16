package com.github.lucasdengcn.billing.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    @ToString.Exclude
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "period_days", nullable = false)
    private Integer periodDays;

    @Column(name = "base_fee", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal baseFee = BigDecimal.ZERO;

    @Column(name = "discount_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ONE;

    @Column(name = "total_fee", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalFee = BigDecimal.ZERO;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<SubscriptionFeature> subscriptionFeatures;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<SubscriptionRenewal> subscriptionRenewals;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<FeatureAccessLog> accessLogs;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<SubscriptionUsageStats> usageStats;
}
