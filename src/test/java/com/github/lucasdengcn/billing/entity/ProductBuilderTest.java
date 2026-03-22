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
                .productNo("PREMIUM_PLAN_001")
                .title("Premium Plan")
                .description("{\"tier\":\"premium\"}")
                .basePrice(new BigDecimal("59.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .features(features)
                .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(100L);
        assertThat(product.getProductNo()).isEqualTo("PREMIUM_PLAN_001");
        assertThat(product.getTitle()).isEqualTo("Premium Plan");
        assertThat(product.getDescription()).isEqualTo("{\"tier\":\"premium\"}");
        assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("59.99"));
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
                .productNo("BASIC_PLAN_001")
                .title("Basic Plan")
                .basePrice(new BigDecimal("29.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getProductNo()).isEqualTo("BASIC_PLAN_001");
        assertThat(product.getTitle()).isEqualTo("Basic Plan");
        assertThat(product.getDescription()).isNull();
        assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(product.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE); // Default value from @Builder.Default
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE); // Default value from @Builder.Default
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
        assertThat(product.getFeatures()).isNull();
    }

    @Test
    void builder_WithNoFields_ShouldCreateProductWithNullsAndDefaults() {
        // When
        Product product = Product.builder()
                .productNo("NO_FIELDS_PLAN_001")
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.ONE_TIME)
                .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getProductNo()).isEqualTo("NO_FIELDS_PLAN_001");
        assertThat(product.getTitle()).isNull();
        assertThat(product.getDescription()).isNull();
        assertThat(product.getBasePrice()).isEqualByComparingTo(BigDecimal.ZERO); // Default value from @Builder.Default
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
                .productNo("ACTIVE_PRODUCT_001")
                .title("Active Product")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        Product productWithInactiveStatus = Product.builder()
                .productNo("INACTIVE_PRODUCT_001")
                .title("Inactive Product")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .discountStatus(DiscountStatus.INACTIVE)
                .build();

        // Then
        assertThat(productWithActiveStatus.getProductNo()).isEqualTo("ACTIVE_PRODUCT_001");
        assertThat(productWithActiveStatus.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
        assertThat(productWithInactiveStatus.getProductNo()).isEqualTo("INACTIVE_PRODUCT_001");
        assertThat(productWithInactiveStatus.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE);
    }

    @Test
    void builder_WithDifferentBaseMonthlyFees_ShouldSetCorrectly() {
        // When
        Product freeProduct = Product.builder()
                .productNo("FREE_PLAN_001")
                .title("Free Plan")
                .basePrice(BigDecimal.ZERO)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.ONE_TIME)
                .build();
        
        Product lowCostProduct = Product.builder()
                .productNo("LOW_COST_PLAN_001")
                .title("Low Cost Plan")
                .basePrice(new BigDecimal("9.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .build();
        
        Product highCostProduct = Product.builder()
                .productNo("HIGH_COST_PLAN_001")
                .title("High Cost Plan")
                .basePrice(new BigDecimal("99.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .build();

        // Then
        assertThat(freeProduct.getProductNo()).isEqualTo("FREE_PLAN_001");
        assertThat(freeProduct.getBasePrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(lowCostProduct.getProductNo()).isEqualTo("LOW_COST_PLAN_001");
        assertThat(lowCostProduct.getBasePrice()).isEqualByComparingTo(new BigDecimal("9.99"));
        assertThat(highCostProduct.getProductNo()).isEqualTo("HIGH_COST_PLAN_001");
        assertThat(highCostProduct.getBasePrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void builder_WithDifferentDiscountRates_ShouldSetCorrectly() {
        // When
        Product noDiscountProduct = Product.builder()
                .productNo("NO_DISCOUNT_PLAN_001")
                .title("No Discount Plan")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .discountRate(BigDecimal.ONE) // No discount (1.0 = 100%)
                .build();
        
        Product tenPercentDiscountProduct = Product.builder()
                .productNo("TEN_PERCENT_DISCOUNT_PLAN_001")
                .title("10% Discount Plan")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .discountRate(new BigDecimal("0.90")) // 10% discount
                .build();
        
        Product fiftyPercentDiscountProduct = Product.builder()
                .productNo("FIFTY_PERCENT_DISCOUNT_PLAN_001")
                .title("50% Discount Plan")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.ONE_TIME)
                .discountRate(new BigDecimal("0.50")) // 50% discount
                .build();

        // Then
        assertThat(noDiscountProduct.getProductNo()).isEqualTo("NO_DISCOUNT_PLAN_001");
        assertThat(noDiscountProduct.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(tenPercentDiscountProduct.getProductNo()).isEqualTo("TEN_PERCENT_DISCOUNT_PLAN_001");
        assertThat(tenPercentDiscountProduct.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(fiftyPercentDiscountProduct.getProductNo()).isEqualTo("FIFTY_PERCENT_DISCOUNT_PLAN_001");
        assertThat(fiftyPercentDiscountProduct.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.50"));
    }

    @Test
    void builder_WithVariousTitles_ShouldSetCorrectly() {
        // When
        Product product1 = Product.builder()
                .productNo("PREMIUM_PLAN_TEST_001")
                .title("Premium Plan")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .build();
        
        Product product2 = Product.builder()
                .productNo("BASIC_PLAN_TEST_001")
                .title("Basic Plan")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .build();
        
        Product product3 = Product.builder()
                .productNo("ENTERPRISE_SOLUTION_TEST_001")
                .title("Enterprise Solution")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.ONE_TIME)
                .build();

        // Then
        assertThat(product1.getProductNo()).isEqualTo("PREMIUM_PLAN_TEST_001");
        assertThat(product1.getTitle()).isEqualTo("Premium Plan");
        assertThat(product2.getProductNo()).isEqualTo("BASIC_PLAN_TEST_001");
        assertThat(product2.getTitle()).isEqualTo("Basic Plan");
        assertThat(product3.getProductNo()).isEqualTo("ENTERPRISE_SOLUTION_TEST_001");
        assertThat(product3.getTitle()).isEqualTo("Enterprise Solution");
    }

    @Test
    void builder_WithJsonDescriptions_ShouldSetCorrectly() {
        // When
        Product product1 = Product.builder()
                .productNo("PLAN_1_DESC_001")
                .title("Plan 1")
                .description("{\"feature\":\"value\"}")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .build();
        
        Product product2 = Product.builder()
                .productNo("PLAN_2_DESC_001")
                .title("Plan 2")
                .description("{\"tier\":\"standard\",\"support\":\"email\"}")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .build();
        
        Product product3 = Product.builder()
                .productNo("PLAN_3_DESC_001")
                .title("Plan 3")
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.ONE_TIME)
                .build(); // No description

        // Then
        assertThat(product1.getProductNo()).isEqualTo("PLAN_1_DESC_001");
        assertThat(product1.getDescription()).isEqualTo("{\"feature\":\"value\"}");
        assertThat(product2.getProductNo()).isEqualTo("PLAN_2_DESC_001");
        assertThat(product2.getDescription()).isEqualTo("{\"tier\":\"standard\",\"support\":\"email\"}");
        assertThat(product3.getProductNo()).isEqualTo("PLAN_3_DESC_001");
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
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
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
                .basePrice(BigDecimal.TEN)
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .build();

        // Then
        assertThat(product.getBasePrice()).isEqualByComparingTo(BigDecimal.TEN); // Provided value
        assertThat(product.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE); // Default value
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE); // Default value
    }

    @Test
    void builder_CanOverrideDefaultValues() {
        // When
        Product product = Product.builder()
                .title("Special Product")
                .basePrice(new BigDecimal("49.99"))
                .discountRate(new BigDecimal("0.75"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        // Then
        assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("49.99"));
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
                .basePrice(new BigDecimal("79.99"))
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
        assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("79.99"));
        assertThat(product.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.85"));
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
        assertThat(product.getCreatedAt()).isEqualTo(createdAt);
        assertThat(product.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(product.getFeatures()).hasSize(1);
        assertThat(product.getFeatures().get(0).getTitle()).isEqualTo("Chained Feature");
    }
}