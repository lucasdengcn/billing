package com.github.lucasdengcn.billing.entity;

import com.github.lucasdengcn.billing.entity.enums.FeatureType;
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

    @Column(name = "feature_no", nullable = false, unique = true)
    private String featureNo;

    @Column(columnDefinition = "json")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", length = 50)
    private FeatureType featureType;

    @Column(nullable = false)
    @Builder.Default
    private Integer quota = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

}