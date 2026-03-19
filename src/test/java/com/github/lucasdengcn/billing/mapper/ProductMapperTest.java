package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequest;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.model.response.ProductFeatureResponse;
import com.github.lucasdengcn.billing.model.response.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    private Product testProduct;
    private ProductFeature testFeature;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = Product.builder()
                .id(1L)
                .title("Premium Plan")
                .description("{\"tier\":\"premium\",\"support\":\"24/7\"}")
                .baseMonthlyFee(new BigDecimal("59.99"))
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now().minusHours(1))
                .features(new ArrayList<>())
                .build();

        // Create test feature
        testFeature = ProductFeature.builder()
                .id(100L)
                .title("API Access")
                .description("Access to premium API endpoints")
                .quota(10000)
                .createdAt(OffsetDateTime.now().minusHours(2))
                .updatedAt(OffsetDateTime.now().minusMinutes(30))
                .build();
    }

    @Test
    void toEntity_FromProductRequest_ShouldMapCorrectly() {
        // Given
        ProductRequest request = new ProductRequest();
        request.setTitle("Basic Plan");
        request.setDescription("{\"tier\":\"basic\"}");
        request.setBaseMonthlyFee(new BigDecimal("29.99"));
        request.setDiscountRate(new BigDecimal("1.00"));
        request.setDiscountStatus(DiscountStatus.INACTIVE);

        // When
        Product product = productMapper.toEntity(request);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull(); // Should be ignored
        assertThat(product.getTitle()).isEqualTo("Basic Plan");
        assertThat(product.getDescription()).isEqualTo("{\"tier\":\"basic\"}");
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(product.getDiscountRate()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE);
        
        // Verify ignored fields are null
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
        assertThat(product.getFeatures()).isNull();
        assertThat(product.getSubscriptions()).isNull();
        assertThat(product.getBillDetails()).isNull();
    }

    @Test
    void toEntity_FromProductRequestWithMinimalFields_ShouldMapCorrectly() {
        // Given
        ProductRequest request = new ProductRequest();
        request.setTitle("Free Plan");
        request.setBaseMonthlyFee(BigDecimal.ZERO); // Required field

        // When
        Product product = productMapper.toEntity(request);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getTitle()).isEqualTo("Free Plan");
        assertThat(product.getBaseMonthlyFee()).isEqualByComparingTo(BigDecimal.ZERO);
        // Default values verification
        assertThat(product.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE); // Default value
        assertThat(product.getDiscountStatus()).isEqualTo(DiscountStatus.INACTIVE); // Default value
        assertThat(product.getDescription()).isNull(); // Optional field
    }

    @Test
    void toResponse_FromProductEntity_ShouldMapCorrectly() {
        // Given - testProduct is already set up in setUp()

        // When
        ProductResponse response = productMapper.toResponse(testProduct);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Premium Plan");
        assertThat(response.getDescription()).isEqualTo("{\"tier\":\"premium\",\"support\":\"24/7\"}");
        assertThat(response.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("59.99"));
        assertThat(response.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(response.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(testProduct.getCreatedAt());
    }

    @Test
    void toResponse_FromProductEntityWithNullFields_ShouldMapCorrectly() {
        // Given
        Product productWithNulls = Product.builder()
                .id(2L)
                .title("Null Fields Product")
                .description(null)
                .baseMonthlyFee(new BigDecimal("19.99"))
                .discountRate(null)
                .discountStatus(null)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        // When
        ProductResponse response = productMapper.toResponse(productWithNulls);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getTitle()).isEqualTo("Null Fields Product");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("19.99"));
        assertThat(response.getDiscountRate()).isNull();
        assertThat(response.getDiscountStatus()).isNull();
        assertThat(response.getCreatedAt()).isEqualTo(productWithNulls.getCreatedAt());
    }

    @Test
    void updateEntity_FromProductRequest_ShouldUpdateCorrectly() {
        // Given
        Product existingProduct = testProduct; // Original product from setUp
        ProductRequest request = new ProductRequest();
        request.setTitle("Updated Premium Plan");
        request.setDescription("{\"tier\":\"premium_plus\",\"support\":\"priority\"}");
        request.setBaseMonthlyFee(new BigDecimal("79.99"));
        request.setDiscountRate(new BigDecimal("0.85"));
        request.setDiscountStatus(DiscountStatus.ACTIVE);

        // When
        productMapper.updateEntity(request, existingProduct);

        // Then
        assertThat(existingProduct).isNotNull();
        assertThat(existingProduct.getId()).isEqualTo(1L); // Should not change
        assertThat(existingProduct.getTitle()).isEqualTo("Updated Premium Plan");
        assertThat(existingProduct.getDescription()).isEqualTo("{\"tier\":\"premium_plus\",\"support\":\"priority\"}");
        assertThat(existingProduct.getBaseMonthlyFee()).isEqualByComparingTo(new BigDecimal("79.99"));
        assertThat(existingProduct.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.85"));
        assertThat(existingProduct.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
        
        // Verify that ignored fields remain unchanged
        assertThat(existingProduct.getCreatedAt()).isEqualTo(testProduct.getCreatedAt()); // Unchanged
        assertThat(existingProduct.getFeatures()).isEqualTo(testProduct.getFeatures()); // Ignored
        assertThat(existingProduct.getSubscriptions()).isEqualTo(testProduct.getSubscriptions()); // Ignored
        assertThat(existingProduct.getBillDetails()).isEqualTo(testProduct.getBillDetails()); // Ignored
    }

    @Test
    void toEntity_FromProductFeatureRequest_ShouldMapCorrectly() {
        // Given
        ProductFeatureRequest request = new ProductFeatureRequest();
        request.setProductId(1L);
        request.setTitle("Storage Space");
        request.setDescription("Additional cloud storage");
        request.setQuota(5000);

        // When
        ProductFeature feature = productMapper.toEntity(request);

        // Then
        assertThat(feature).isNotNull();
        assertThat(feature.getId()).isNull(); // Should be ignored
        assertThat(feature.getTitle()).isEqualTo("Storage Space");
        assertThat(feature.getDescription()).isEqualTo("Additional cloud storage");
        assertThat(feature.getQuota()).isEqualTo(5000);
        
        // Verify ignored fields are null
        assertThat(feature.getProduct()).isNull(); // Should be ignored
        assertThat(feature.getCreatedAt()).isNull(); // Should be ignored
        assertThat(feature.getUpdatedAt()).isNull(); // Should be ignored
        assertThat(feature.getSubscriptionFeatures()).isNull(); // Should be ignored
        assertThat(feature.getAccessLogs()).isNull(); // Should be ignored
        assertThat(feature.getUsageStats()).isNull(); // Should be ignored
        assertThat(feature.getBillDetails()).isNull(); // Should be ignored
    }

    @Test
    void toEntity_FromProductFeatureRequestWithMinimalFields_ShouldMapCorrectly() {
        // Given
        ProductFeatureRequest request = new ProductFeatureRequest();
        request.setProductId(2L);
        request.setTitle("Basic Feature");
        request.setQuota(100); // Required field

        // When
        ProductFeature feature = productMapper.toEntity(request);

        // Then
        assertThat(feature).isNotNull();
        assertThat(feature.getId()).isNull();
        assertThat(feature.getTitle()).isEqualTo("Basic Feature");
        assertThat(feature.getDescription()).isNull(); // Optional field
        assertThat(feature.getQuota()).isEqualTo(100);
    }

    @Test
    void toResponse_FromProductFeatureEntity_ShouldMapCorrectly() {
        // Given - testFeature is already set up in setUp()
        testFeature.setProduct(testProduct); // Set up the relationship

        // When
        ProductFeatureResponse response = productMapper.toResponse(testFeature);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getProductId()).isEqualTo(1L); // From the product relationship
        assertThat(response.getTitle()).isEqualTo("API Access");
        assertThat(response.getDescription()).isEqualTo("Access to premium API endpoints");
        assertThat(response.getQuota()).isEqualTo(10000);
        assertThat(response.getCreatedAt()).isEqualTo(testFeature.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(testFeature.getUpdatedAt());
    }

    @Test
    void toResponse_FromProductFeatureEntityWithNullProduct_ShouldMapProductIdAsNull() {
        // Given
        ProductFeature featureWithoutProduct = ProductFeature.builder()
                .id(200L)
                .title("Standalone Feature")
                .description("Feature without product association")
                .quota(250)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        // Note: product is not set, so it will be null

        // When
        ProductFeatureResponse response = productMapper.toResponse(featureWithoutProduct);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getProductId()).isNull(); // Because product is null
        assertThat(response.getTitle()).isEqualTo("Standalone Feature");
        assertThat(response.getDescription()).isEqualTo("Feature without product association");
        assertThat(response.getQuota()).isEqualTo(250);
    }

    @Test
    void toResponse_FromProductFeatureEntityWithNullFields_ShouldMapCorrectly() {
        // Given
        ProductFeature featureWithNulls = ProductFeature.builder()
                .id(300L)
                .title("Feature With Nulls")
                .description(null)
                .quota(0)
                .createdAt(null)
                .updatedAt(null)
                .build();
        featureWithNulls.setProduct(testProduct); // Set product to test productId mapping

        // When
        ProductFeatureResponse response = productMapper.toResponse(featureWithNulls);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(300L);
        assertThat(response.getProductId()).isEqualTo(1L); // From the product relationship
        assertThat(response.getTitle()).isEqualTo("Feature With Nulls");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getQuota()).isEqualTo(0);
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
    }
}