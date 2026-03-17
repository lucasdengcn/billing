package com.github.lucasdengcn.billing.entity;

import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private Customer customer;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_no", unique = true, nullable = false)
    private String deviceNo;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.INACTIVE;

    @Column(name = "last_activity_at")
    private OffsetDateTime lastActivityAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "device")
    @ToString.Exclude
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "device")
    @ToString.Exclude
    private List<SubscriptionFeature> subscriptionFeatures;

    @OneToMany(mappedBy = "device")
    @ToString.Exclude
    private List<SubscriptionRenewal> subscriptionRenewals;

    @OneToMany(mappedBy = "device")
    @ToString.Exclude
    private List<FeatureAccessLog> featureAccessLogs;

    @OneToMany(mappedBy = "device")
    @ToString.Exclude
    private List<SubscriptionUsageStats> usageStats;
}
