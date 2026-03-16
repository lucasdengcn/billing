package com.github.lucasdengcn.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_renewals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRenewal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    @ToString.Exclude
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    @ToString.Exclude
    private Subscription subscription;

    @Column(name = "previous_end_date")
    private OffsetDateTime previousEndDate;

    @Column(name = "new_end_date", nullable = false)
    private OffsetDateTime newEndDate;

    @Column(name = "renewal_period_days", nullable = false)
    private Integer renewalPeriodDays;

    @Column(name = "base_fee", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal baseFee = BigDecimal.ZERO;

    @Column(name = "discount_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ONE;

    @Column(name = "total_fee", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalFee = BigDecimal.ZERO;

    @Column(name = "fee_paid", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal feePaid = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
