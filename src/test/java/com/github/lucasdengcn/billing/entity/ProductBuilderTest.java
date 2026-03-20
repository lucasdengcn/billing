package com.github.lucasdengcn.billing.entity;

import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductBuilderTest {

    @Test
    void builder_WithAllFields_ShouldCreateProductCorrectly() {
        // Given
        List<ProductFeature> features = Arrays.asList(
                ProductFeature.builder().id(1L).title("Feature 1").build(),
                ProductFeature.builder().id(2L).title("Feature 2").build()
        );
        
        OffsetDateTime now = OffsetDateTime.now();
        
        // When
        Product product = Product.builder()
                .id(100L)
                .title("Premium Plan")
                .description("{\"tier\":\"premium\"}")
                .baseMonthlyFee(new BigDecimal("59.99"))
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .features(features)
                .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(100L);
        assertThat(product.getTitle()).isEqualTo("Premium Plan");
        assertThat(product.getDescription()).isEqualTo("{\"tier\":\"premium\"}");
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("59.99"));
        assertThat(product.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
        assertThat(product.getFeatures()).hasSize(2);
        assertThat(product.getSubscriptions()).isNull();
        assertThat(product.getBillDetails()).isNull();
    }

    @Test
    void builder_WithMinimalFields_ShouldCreateProductWithDefaults() {
        // When
        Product product = Product.builder()
                .title("Basic Plan")
                .baseMonthlyFee(new BigDecimal("29.99"))
                .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getTitle()).isEqualTo("Basic Plan");
        assertThat(product.getDescription()).isNull();
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(product.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE); // Default value from @Builder.Default
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE); // Default value from @Builder.Default
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
        assertThat(product.getFeatures()).isNull();
    }

    @Test
    void builder_WithNoFields_ShouldCreateProductWithNullsAndDefaults() {
        // When
        Product product = Product.builder().build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getTitle()).isNull();
        assertThat(product.getDescription()).isNull();
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(BigDecimal.ZERO); // Default value from @Builder.Default
        assertThat(product.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE); // Default value from @Builder.Default
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE); // Default value from @Builder.Default
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
        assertThat(product.getFeatures()).isNull();
        assertThat(product.getSubscriptions()).isNull();
        assertThat(product.getBillDetails()).isNull();
    }

    @Test
    void builder_WithSpecificDiscountStatus_ShouldRespectProvidedStatus() {
        // When
        Product productWithActiveStatus = Product.builder()
                .title("Active Product")
                .baseMonthlyFee(BigDecimal.TEN)
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        Product productWithInactiveStatus = Product.builder()
                .title("Inactive Product")
                .baseMonthlyFee(BigDecimal.TEN)
                .discountStatus(DiscountStatus.INACTIVE)
                .build();

        // Then
        assertThat(productWithActiveStatus.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
        assertThat(productWithInactiveStatus.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE);
    }

    @Test
    void builder_WithDifferentBaseMonthlyFees_ShouldSetCorrectly() {
        // When
        Product freeProduct = Product.builder()
                .title("Free Plan")
                .baseMonthlyFee(BigDecimal.ZERO)
                .build();
        
        Product lowCostProduct = Product.builder()
                .title("Low Cost Plan")
                .baseMonthlyFee(new BigDecimal("9.99"))
                .build();
        
        Product highCostProduct = Product.builder()
                .title("High Cost Plan")
                .baseMonthlyFee(new BigDecimal("99.99"))
                .build();

        // Then
        assertThat(freeProduct.getBaseMonthlyFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(lowCostProduct.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("9.99"));
        assertThat(highCostProduct.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void builder_WithDifferentDiscountRates_ShouldSetCorrectly() {
        // When
        Product noDiscountProduct = Product.builder()
                .title("No Discount Plan")
                .baseMonthlyFee(BigDecimal.TEN)
                .discountRate(BigDecimal.ONE) // No discount (1.0 = 100%)
                .build();
        
        Product tenPercentDiscountProduct = Product.builder()
                .title("10% Discount Plan")
                .baseMonthlyFee(BigDecimal.TEN)
                .discountRate(new BigDecimal("0.90")) // 10% discount
                .build();
        
        Product fiftyPercentDiscountProduct = Product.builder()
                .title("50% Discount Plan")
                .baseMonthlyFee(BigDecimal.TEN)
                .discountRate(new BigDecimal("0.50")) // 50% discount
                .build();

        // Then
        assertThat(noDiscountProduct.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(tenPercentDiscountProduct.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(fiftyPercentDiscountProduct.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.50"));
    }

    @Test
    void builder_WithVariousTitles_ShouldSetCorrectly() {
        // When
        Product product1 = Product.builder()
                .title("Premium Plan")
                .baseMonthlyFee(BigDecimal.TEN)
                .build();
        
        Product product2 = Product.builder()
                .title("Basic Plan")
                .baseMonthlyFee(BigDecimal.TEN)
                .build();
        
        Product product3 = Product.builder()
                .title("Enterprise Solution")
                .baseMonthlyFee(BigDecimal.TEN)
                .build();

        // Then
        assertThat(product1.getTitle()).isEqualTo("Premium Plan");
        assertThat(product2.getTitle()).isEqualTo("Basic Plan");
        assertThat(product3.getTitle()).isEqualTo("Enterprise Solution");
    }

    @Test
    void builder_WithJsonDescriptions_ShouldSetCorrectly() {
        // When
        Product product1 = Product.builder()
                .title("Plan 1")
                .description("{\"feature\":\"value\"}")
                .baseMonthlyFee(BigDecimal.TEN)
                .build();
        
        Product product2 = Product.builder()
                .title("Plan 2")
                .description("{\"tier\":\"standard\",\"support\":\"email\"}")
                .baseMonthlyFee(BigDecimal.TEN)
                .build();
        
        Product product3 = Product.builder()
                .title("Plan 3")
                .baseMonthlyFee(BigDecimal.TEN)
                .build(); // No description

        // Then
        assertThat(product1.getDescription()).isEqualTo("{\"feature\":\"value\"}");
        assertThat(product2.getDescription()).isEqualTo("{\"tier\":\"standard\",\"support\":\"email\"}");
        assertThat(product3.getDescription()).isNull();
    }

    @Test
    void builder_WithProductFeatures_ShouldSetCorrectly() {
        // Given
        List<ProductFeature> features = Arrays.asList(
                ProductFeature.builder().id(1L).title("API Access").build(),
                ProductFeature.builder().id(2L).title("Storage").build(),
                ProductFeature.builder().id(3L).title("Support").build()
        );
        
        // When
        Product product = Product.builder()
                .title("Feature-Rich Plan")
                .baseMonthlyFee(BigDecimal.TEN)
                .features(features)
                .build();

        // Then
        assertThat(product.getFeatures()).hasSize(3);
        assertThat(product.getFeatures()).extracting(ProductFeature::getTitle)
                .containsExactly("API Access", "Storage", "Support");
    }

    @Test
    void builder_DefaultValuesShouldBeCorrect() {
        // When
        Product product = Product.builder()
                .title("Test Product")
                .baseMonthlyFee(BigDecimal.TEN)
                .build();

        // Then
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(BigDecimal.TEN); // Provided value
        assertThat(product.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE); // Default value
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE); // Default value
    }

    @Test
    void builder_CanOverrideDefaultValues() {
        // When
        Product product = Product.builder()
                .title("Special Product")
                .baseMonthlyFee(new BigDecimal("49.99"))
                .discountRate(new BigDecimal("0.75"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        // Then
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("49.99"));
        assertThat(product.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.75"));
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
    }

    @Test
    void builder_WithChainedCalls_ShouldWorkCorrectly() {
        // Given
        List<ProductFeature> features = Arrays.asList(
                ProductFeature.builder().id(100L).title("Chained Feature").build()
        );
        
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        
        // When
        Product product = Product.builder()
                .id(1000L)
                .title("Chained Product")
                .description("{\"chained\":true}")
                .baseMonthlyFee(new BigDecimal("79.99"))
                .discountRate(new BigDecimal("0.85"))
                .discountStatus(DiscountStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .features(features)
                .build();

        // Then
        assertThat(product.getId()).isEqualTo(1000L);
        assertThat(product.getTitle()).isEqualTo("Chained Product");
        assertThat(product.getDescription()).isEqualTo("{\"chained\":true}");
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("79.99"));
        assertThat(product.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.85"));
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
        assertThat(product.getCreatedAt()).isEqualTo(createdAt);
        assertThat(product.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(product.getFeatures()).hasSize(1);
        assertThat(product.getFeatures().get(0).getTitle()).isEqualTo("Chained Feature");
    }
}