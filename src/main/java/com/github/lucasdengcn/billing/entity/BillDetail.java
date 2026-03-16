package com.github.lucasdengcn.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bill_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    @ToString.Exclude
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_feature_id")
    @ToString.Exclude
    private ProductFeature productFeature;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(columnDefinition = "json")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
