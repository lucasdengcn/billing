package com.github.lucasdengcn.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "product_features")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "json")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer quota = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "productFeature")
    @ToString.Exclude
    private List<SubscriptionFeature> subscriptionFeatures;

    @OneToMany(mappedBy = "productFeature")
    @ToString.Exclude
    private List<FeatureAccessLog> accessLogs;

    @OneToMany(mappedBy = "productFeature")
    @ToString.Exclude
    private List<SubscriptionUsageStats> usageStats;

    @OneToMany(mappedBy = "productFeature")
    @ToString.Exclude
    private List<BillDetail> billDetails;
}
