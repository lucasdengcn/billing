package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:product-feature-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class ProductFeatureRepositoryTest {

    @Autowired
    private ProductFeatureRepository productFeatureRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct1;
    private Product testProduct2;
    private ProductFeature testFeature1;
    private ProductFeature testFeature2;
    private ProductFeature testFeature3;
    private ProductFeature testFeature4;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        productFeatureRepository.deleteAll();
        
        // Create test products
        testProduct1 = Product.builder()
                .title("Basic Plan")
                .description("Basic service plan")
                .baseMonthlyFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        testProduct2 = Product.builder()
                .title("Premium Plan")
                .description("Premium service plan")
                .baseMonthlyFee(new BigDecimal("59.99"))
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        // Create test features
        testFeature1 = ProductFeature.builder()
                .title("Feature 1")
                .description("First feature for basic plan")
                .quota(100)
                .build();

        testFeature2 = ProductFeature.builder()
                .title("Feature 2")
                .description("Second feature for basic plan")
                .quota(200)
                .build();

        testFeature3 = ProductFeature.builder()
                .title("Feature 3")
                .description("Feature for premium plan")
                .quota(500)
                .build();

        testFeature4 = ProductFeature.builder()
                .title("Feature 4")
                .description("Another feature for premium plan")
                .quota(1000)
                .build();
    }

    @Test
    void findById_WhenFeatureExists_ShouldReturnFeature() {
        // Given
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testFeature1.setProduct(testProduct1);
        ProductFeature savedFeature = entityManager.persistAndFlush(testFeature1);
        Long featureId = savedFeature.getId();

        // When
        Optional<ProductFeature> foundFeature = productFeatureRepository.findById(featureId);

        // Then
        assertThat(foundFeature).isPresent();
        assertThat(foundFeature.get().getTitle()).isEqualTo("Feature 1");
        assertThat(foundFeature.get().getQuota()).isEqualTo(100);
        assertThat(foundFeature.get().getDescription()).isEqualTo("First feature for basic plan");
        assertThat(foundFeature.get().getProduct().getTitle()).isEqualTo("Basic Plan");
    }

    @Test
    void findById_WhenFeatureNotExists_ShouldReturnEmpty() {
        // When
        Optional<ProductFeature> foundFeature = productFeatureRepository.findById(999L);

        // Then
        assertThat(foundFeature).isEmpty();
    }

    @Test
    void findByProduct_WhenFeaturesExist_ShouldReturnAllFeaturesForProduct() {
        // Given
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        
        testFeature1.setProduct(testProduct1);
        testFeature2.setProduct(testProduct1);
        testFeature3.setProduct(testProduct2);
        testFeature4.setProduct(testProduct2);
        
        entityManager.persistAndFlush(testFeature1); // Belongs to Basic Plan
        entityManager.persistAndFlush(testFeature2); // Belongs to Basic Plan
        entityManager.persistAndFlush(testFeature3); // Belongs to Premium Plan
        entityManager.persistAndFlush(testFeature4); // Belongs to Premium Plan

        // When
        List<ProductFeature> featuresForProduct1 = productFeatureRepository.findByProduct(testProduct1);

        // Then
        assertThat(featuresForProduct1).hasSize(2);
        assertThat(featuresForProduct1).extracting(ProductFeature::getTitle)
                .containsExactlyInAnyOrder("Feature 1", "Feature 2");
        assertThat(featuresForProduct1).extracting(ProductFeature::getQuota)
                .containsOnly(100, 200);
        assertThat(featuresForProduct1).extracting(f -> f.getProduct().getTitle())
                .containsOnly("Basic Plan");
    }

    @Test
    void findByProduct_WhenNoFeaturesExist_ShouldReturnEmptyList() {
        // Given
        Product newProduct = Product.builder()
                .title("New Plan")
                .description("New service plan")
                .baseMonthlyFee(new BigDecimal("39.99"))
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        newProduct = entityManager.persistAndFlush(newProduct);

        // When
        List<ProductFeature> features = productFeatureRepository.findByProduct(newProduct);

        // Then
        assertThat(features).isEmpty();
    }

    @Test
    void save_ShouldPersistFeatureCorrectly() {
        // Given
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        ProductFeature newFeature = ProductFeature.builder()
                .title("New Feature")
                .description("Brand new feature")
                .quota(300)
                .build();
        newFeature.setProduct(testProduct1);

        // When
        ProductFeature savedFeature = productFeatureRepository.save(newFeature);

        // Then
        assertThat(savedFeature.getId()).isNotNull();
        assertThat(savedFeature.getTitle()).isEqualTo("New Feature");
        assertThat(savedFeature.getQuota()).isEqualTo(300);
        assertThat(savedFeature.getCreatedAt()).isNotNull();
        assertThat(savedFeature.getUpdatedAt()).isNotNull();
        assertThat(savedFeature.getProduct().getTitle()).isEqualTo("Basic Plan");

        // Verify it can be retrieved
        Optional<ProductFeature> retrievedFeature = productFeatureRepository.findById(savedFeature.getId());
        assertThat(retrievedFeature).isPresent();
        assertThat(retrievedFeature.get().getTitle()).isEqualTo("New Feature");
    }

    @Test
    void save_WhenFeatureHasNoDescription_ShouldPersistCorrectly() {
        // Given
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        ProductFeature featureWithoutDescription = ProductFeature.builder()
                .title("Feature Without Description")
                .quota(50)
                .build();
        featureWithoutDescription.setProduct(testProduct1);

        // When
        ProductFeature savedFeature = productFeatureRepository.save(featureWithoutDescription);

        // Then
        assertThat(savedFeature.getId()).isNotNull();
        assertThat(savedFeature.getTitle()).isEqualTo("Feature Without Description");
        assertThat(savedFeature.getQuota()).isEqualTo(50);
        assertThat(savedFeature.getDescription()).isNull();
    }

    @Test
    void findAll_WhenMultipleFeaturesExist_ShouldReturnAllFeatures() {
        // Given
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        
        testFeature1.setProduct(testProduct1);
        testFeature2.setProduct(testProduct1);
        testFeature3.setProduct(testProduct2);
        
        entityManager.persistAndFlush(testFeature1);
        entityManager.persistAndFlush(testFeature2);
        entityManager.persistAndFlush(testFeature3);

        // When
        List<ProductFeature> allFeatures = productFeatureRepository.findAll();

        // Then
        assertThat(allFeatures).hasSize(3);
        assertThat(allFeatures).extracting(ProductFeature::getTitle)
                .containsExactlyInAnyOrder("Feature 1", "Feature 2", "Feature 3");
    }

    @Test
    void deleteById_WhenFeatureExists_ShouldDeleteFeature() {
        // Given
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testFeature1.setProduct(testProduct1);
        ProductFeature savedFeature = entityManager.persistAndFlush(testFeature1);
        Long featureId = savedFeature.getId();

        // When
        productFeatureRepository.deleteById(featureId);

        // Then
        Optional<ProductFeature> deletedFeature = productFeatureRepository.findById(featureId);
        assertThat(deletedFeature).isEmpty();
    }

    @Test
    void existsById_WhenFeatureExists_ShouldReturnTrue() {
        // Given
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testFeature1.setProduct(testProduct1);
        ProductFeature savedFeature = entityManager.persistAndFlush(testFeature1);
        Long featureId = savedFeature.getId();

        // When
        boolean exists = productFeatureRepository.existsById(featureId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenFeatureDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = productFeatureRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByProduct_WhenProductHasManyFeatures_ShouldReturnAllFeatures() {
        // Given - Create a product with multiple features
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        
        // Create several features for the same product
        ProductFeature feature1 = ProductFeature.builder()
                .title("API Access")
                .description("API access quota")
                .quota(1000)
                .build();
        feature1.setProduct(testProduct1);
        
        ProductFeature feature2 = ProductFeature.builder()
                .title("Storage")
                .description("Storage space in MB")
                .quota(5000)
                .build();
        feature2.setProduct(testProduct1);
        
        ProductFeature feature3 = ProductFeature.builder()
                .title("Bandwidth")
                .description("Monthly bandwidth allowance")
                .quota(10000)
                .build();
        feature3.setProduct(testProduct1);
        
        ProductFeature feature4 = ProductFeature.builder()
                .title("Users")
                .description("Number of allowed users")
                .quota(10)
                .build();
        feature4.setProduct(testProduct1);
        
        entityManager.persistAndFlush(feature1);
        entityManager.persistAndFlush(feature2);
        entityManager.persistAndFlush(feature3);
        entityManager.persistAndFlush(feature4);

        // When
        List<ProductFeature> features = productFeatureRepository.findByProduct(testProduct1);

        // Then
        assertThat(features).hasSize(4);
        assertThat(features).extracting(ProductFeature::getTitle)
                .containsExactlyInAnyOrder("API Access", "Storage", "Bandwidth", "Users");
        assertThat(features).extracting(ProductFeature::getQuota)
                .containsOnly(1000, 5000, 10000, 10);
    }
}