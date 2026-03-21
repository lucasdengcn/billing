package com.github.lucasdengcn.billing.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFeatureBuilderTest {

    @Test
    void builder_WithAllFields_ShouldCreateProductFeatureCorrectly() {
        // Given
        Product product = Product.builder()
                .id(1L)
                .title("Premium Plan")
                .basePrice(java.math.BigDecimal.TEN)
                .build();
        
        List<SubscriptionFeature> subscriptionFeatures = Arrays.asList(
                SubscriptionFeature.builder().id(1L).build(),
                SubscriptionFeature.builder().id(2L).build()
        );
        
        List<FeatureAccessLog> accessLogs = Arrays.asList(
                FeatureAccessLog.builder().id(1L).build()
        );
        
        List<SubscriptionUsageStats> usageStats = Arrays.asList(
                SubscriptionUsageStats.builder().id(1L).build()
        );
        
        List<BillDetail> billDetails = Arrays.asList(
                BillDetail.builder().id(1L).build()
        );
        
        OffsetDateTime now = OffsetDateTime.now();
        
        // When
        ProductFeature feature = ProductFeature.builder()
                .id(100L)
                .product(product)
                .title("Advanced Analytics")
                .description("{\"feature\":\"advanced_analytics\"}")
                .quota(1000)
                .createdAt(now)
                .updatedAt(now)
                .subscriptionFeatures(subscriptionFeatures)
                .accessLogs(accessLogs)
                .usageStats(usageStats)
                .billDetails(billDetails)
                .build();

        // Then
        assertThat(feature).isNotNull();
        assertThat(feature.getId()).isEqualTo(100L);
        assertThat(feature.getProduct()).isEqualTo(product);
        assertThat(feature.getTitle()).isEqualTo("Advanced Analytics");
        assertThat(feature.getDescription()).isEqualTo("{\"feature\":\"advanced_analytics\"}");
        assertThat(feature.getQuota()).isEqualTo(1000);
        assertThat(feature.getCreatedAt()).isEqualTo(now);
        assertThat(feature.getUpdatedAt()).isEqualTo(now);
        assertThat(feature.getSubscriptionFeatures()).hasSize(2);
        assertThat(feature.getAccessLogs()).hasSize(1);
        assertThat(feature.getUsageStats()).hasSize(1);
        assertThat(feature.getBillDetails()).hasSize(1);
    }

    @Test
    void builder_WithMinimalFields_ShouldCreateProductFeatureWithDefaults() {
        // Given
        Product product = Product.builder()
                .id(1L)
                .title("Basic Plan")
                .basePrice(java.math.BigDecimal.valueOf(29.99))
                .build();
        
        // When
        ProductFeature feature = ProductFeature.builder()
                .product(product)
                .title("Basic Feature")
                .build();

        // Then
        assertThat(feature).isNotNull();
        assertThat(feature.getId()).isNull();
        assertThat(feature.getProduct()).isEqualTo(product);
        assertThat(feature.getTitle()).isEqualTo("Basic Feature");
        assertThat(feature.getDescription()).isNull();
        assertThat(feature.getQuota()).isEqualTo(0); // Default value from @Builder.Default
        assertThat(feature.getCreatedAt()).isNull();
        assertThat(feature.getUpdatedAt()).isNull();
        assertThat(feature.getSubscriptionFeatures()).isNull();
        assertThat(feature.getAccessLogs()).isNull();
        assertThat(feature.getUsageStats()).isNull();
        assertThat(feature.getBillDetails()).isNull();
    }

    @Test
    void builder_WithNoFields_ShouldCreateProductFeatureWithNullsAndDefaults() {
        // When
        ProductFeature feature = ProductFeature.builder().build();

        // Then
        assertThat(feature).isNotNull();
        assertThat(feature.getId()).isNull();
        assertThat(feature.getProduct()).isNull();
        assertThat(feature.getTitle()).isNull();
        assertThat(feature.getDescription()).isNull();
        assertThat(feature.getQuota()).isEqualTo(0); // Default value from @Builder.Default
        assertThat(feature.getCreatedAt()).isNull();
        assertThat(feature.getUpdatedAt()).isNull();
        assertThat(feature.getSubscriptionFeatures()).isNull();
        assertThat(feature.getAccessLogs()).isNull();
        assertThat(feature.getUsageStats()).isNull();
        assertThat(feature.getBillDetails()).isNull();
    }

    @Test
    void builder_WithSpecificQuota_ShouldRespectProvidedQuota() {
        // Given
        Product product = Product.builder()
                .id(1L)
                .title("Test Plan")
                .basePrice(java.math.BigDecimal.valueOf(19.99))
                .build();
        
        // When
        ProductFeature featureWithZeroQuota = ProductFeature.builder()
                .product(product)
                .title("Zero Quota Feature")
                .quota(0)
                .build();
        
        ProductFeature featureWithPositiveQuota = ProductFeature.builder()
                .product(product)
                .title("Positive Quota Feature")
                .quota(1000)
                .build();
        
        ProductFeature featureWithLargeQuota = ProductFeature.builder()
                .product(product)
                .title("Large Quota Feature")
                .quota(1000000)
                .build();

        // Then
        assertThat(featureWithZeroQuota.getQuota()).isEqualTo(0);
        assertThat(featureWithPositiveQuota.getQuota()).isEqualTo(1000);
        assertThat(featureWithLargeQuota.getQuota()).isEqualTo(1000000);
    }

    @Test
    void builder_WithDifferentTitles_ShouldSetCorrectly() {
        // Given
        Product product = Product.builder()
                .id(1L)
                .title("Test Plan")
                .basePrice(java.math.BigDecimal.valueOf(19.99))
                .build();
        
        // When
        ProductFeature feature1 = ProductFeature.builder()
                .product(product)
                .title("API Access")
                .build();
        
        ProductFeature feature2 = ProductFeature.builder()
                .product(product)
                .title("Storage Space")
                .build();
        
        ProductFeature feature3 = ProductFeature.builder()
                .product(product)
                .title("Support Hours")
                .build();

        // Then
        assertThat(feature1.getTitle()).isEqualTo("API Access");
        assertThat(feature2.getTitle()).isEqualTo("Storage Space");
        assertThat(feature3.getTitle()).isEqualTo("Support Hours");
    }

    @Test
    void builder_WithJsonDescriptions_ShouldSetCorrectly() {
        // Given
        Product product = Product.builder()
                .id(1L)
                .title("Test Plan")
                .basePrice(java.math.BigDecimal.valueOf(19.99))
                .build();
        
        // When
        ProductFeature feature1 = ProductFeature.builder()
                .product(product)
                .title("Feature 1")
                .description("{\"type\":\"analytics\",\"level\":\"premium\"}")
                .build();
        
        ProductFeature feature2 = ProductFeature.builder()
                .product(product)
                .title("Feature 2")
                .description("{\"type\":\"storage\",\"unit\":\"GB\",\"size\":100}")
                .build();
        
        ProductFeature feature3 = ProductFeature.builder()
                .product(product)
                .title("Feature 3")
                .build(); // No description

        // Then
        assertThat(feature1.getDescription()).isEqualTo("{\"type\":\"analytics\",\"level\":\"premium\"}");
        assertThat(feature2.getDescription()).isEqualTo("{\"type\":\"storage\",\"unit\":\"GB\",\"size\":100}");
        assertThat(feature3.getDescription()).isNull();
    }

    @Test
    void builder_WithProductRelationship_ShouldSetCorrectly() {
        // Given
        Product product1 = Product.builder()
                .id(1L)
                .title("Product 1")
                .basePrice(java.math.BigDecimal.valueOf(19.99))
                .build();
        
        Product product2 = Product.builder()
                .id(2L)
                .title("Product 2")
                .basePrice(java.math.BigDecimal.valueOf(29.99))
                .build();
        
        // When
        ProductFeature feature1 = ProductFeature.builder()
                .product(product1)
                .title("Feature for Product 1")
                .build();
        
        ProductFeature feature2 = ProductFeature.builder()
                .product(product2)
                .title("Feature for Product 2")
                .build();

        // Then
        assertThat(feature1.getProduct()).isEqualTo(product1);
        assertThat(feature2.getProduct()).isEqualTo(product2);
    }

    @Test
    void builder_WithVariousRelationshipLists_ShouldSetCorrectly() {
        // Given
        Product product = Product.builder()
                .id(1L)
                .title("Test Plan")
                .basePrice(java.math.BigDecimal.valueOf(19.99))
                .build();
        
        List<SubscriptionFeature> subscriptionFeatures = Arrays.asList(
                SubscriptionFeature.builder().id(1L).build(),
                SubscriptionFeature.builder().id(2L).build(),
                SubscriptionFeature.builder().id(3L).build()
        );
        
        List<FeatureAccessLog> accessLogs = Arrays.asList(
                FeatureAccessLog.builder().id(10L).build(),
                FeatureAccessLog.builder().id(11L).build()
        );
        
        // When
        ProductFeature feature = ProductFeature.builder()
                .product(product)
                .title("Feature with Relationships")
                .subscriptionFeatures(subscriptionFeatures)
                .accessLogs(accessLogs)
                .build();

        // Then
        assertThat(feature.getSubscriptionFeatures()).hasSize(3);
        assertThat(feature.getAccessLogs()).hasSize(2);
        assertThat(feature.getUsageStats()).isNull(); // Not set
        assertThat(feature.getBillDetails()).isNull(); // Not set
    }

    @Test
    void builder_DefaultQuotaShouldBeZero() {
        // When
        ProductFeature feature = ProductFeature.builder()
                .build();

        // Then
        assertThat(feature.getQuota()).isEqualTo(0);
    }

    @Test
    void builder_CanOverrideDefaultQuota() {
        // When
        ProductFeature featureWithCustomQuota = ProductFeature.builder()
                .quota(500)
                .build();
        
        ProductFeature featureWithZeroQuota = ProductFeature.builder()
                .quota(0)
                .build();
        
        ProductFeature featureWithLargeQuota = ProductFeature.builder()
                .quota(10000)
                .build();

        // Then
        assertThat(featureWithCustomQuota.getQuota()).isEqualTo(500);
        assertThat(featureWithZeroQuota.getQuota()).isEqualTo(0);
        assertThat(featureWithLargeQuota.getQuota()).isEqualTo(10000);
    }

    @Test
    void builder_WithChainedCalls_ShouldWorkCorrectly() {
        // Given
        Product product = Product.builder()
                .id(100L)
                .title("Chained Plan")
                .basePrice(java.math.BigDecimal.valueOf(49.99))
                .build();
        
        List<SubscriptionFeature> subscriptionFeatures = Arrays.asList(
                SubscriptionFeature.builder().id(1000L).build()
        );
        
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        
        // When
        ProductFeature feature = ProductFeature.builder()
                .id(200L)
                .product(product)
                .title("Chained Feature")
                .description("{\"chained\":true}")
                .quota(2500)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .subscriptionFeatures(subscriptionFeatures)
                .build();

        // Then
        assertThat(feature.getId()).isEqualTo(200L);
        assertThat(feature.getProduct().getId()).isEqualTo(100L);
        assertThat(feature.getTitle()).isEqualTo("Chained Feature");
        assertThat(feature.getDescription()).isEqualTo("{\"chained\":true}");
        assertThat(feature.getQuota()).isEqualTo(2500);
        assertThat(feature.getCreatedAt()).isEqualTo(createdAt);
        assertThat(feature.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(feature.getSubscriptionFeatures()).hasSize(1);
    }
}