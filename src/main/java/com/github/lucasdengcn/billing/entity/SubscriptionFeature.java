package com.github.lucasdengcn.billing.entity;

import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_features", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscription_id", "product_feature_id"})
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionFeature {

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

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "json")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", length = 50)
    private FeatureType featureType;

    @Column(nullable = false)
    @Builder.Default
    private Integer quota = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer accessed = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
    
    @PrePersist
    @PreUpdate
    private void populateFeatureType() {
        if (this.productFeature != null && this.productFeature.getFeatureType() != null && this.featureType == null) {
            this.featureType = this.productFeature.getFeatureType();
        }
    }
}