package com.github.lucasdengcn.billing.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Description;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "json")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    @Builder.Default
    private PriceType priceType = PriceType.MONTHLY;

    // Discount rate applied to base price (1.0 = no discount), fee = basePrice * discountRate
    @Column(name = "discount_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ONE;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "discount_status")
    @Builder.Default
    private DiscountStatus discountStatus = DiscountStatus.INACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ProductFeature> features;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    private List<BillDetail> billDetails;
}