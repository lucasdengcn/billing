package com.github.lucasdengcn.billing.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.github.lucasdengcn.billing.entity.enums.DiscountRate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
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

    @Column(name = "periods", nullable = false)
    @Builder.Default
    private Integer periods = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_unit", nullable = false)
    @Builder.Default
    private PeriodUnit periodUnit = PeriodUnit.MONTHS;

    @Column(name = "base_fee", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal baseFee = BigDecimal.ZERO;

    // 1 means no discount
    @Column(name = "discount_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountRate = DiscountRate.NO_DISCOUNT.getRate();

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