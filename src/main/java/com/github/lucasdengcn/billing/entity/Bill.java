package com.github.lucasdengcn.billing.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.github.lucasdengcn.billing.entity.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private Customer customer;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(name = "total_fees", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalFees = BigDecimal.ZERO;

    @Column(name = "base_fees", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal baseFees = BigDecimal.ZERO;

    @Column(name = "usage_fees", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal usageFees = BigDecimal.ZERO;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<BillDetail> details;
}
