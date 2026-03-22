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
        "spring.datasource.url=jdbc:h2:mem:product-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;
    private Product testProduct4;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        productRepository.deleteAll();
        
        // Create test products with different discount statuses
        testProduct1 = Product.builder()
                .productNo("BASIC_PLAN_001")
                .title("Basic Plan")
                .description("Basic service plan")
                .basePrice(new BigDecimal("29.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        testProduct2 = Product.builder()
                .productNo("PREMIUM_PLAN_001")
                .title("Premium Plan")
                .description("Premium service plan")
                .basePrice(new BigDecimal("59.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        testProduct3 = Product.builder()
                .productNo("ENTERPRISE_PLAN_001")
                .title("Enterprise Plan")
                .description("Enterprise service plan")
                .basePrice(new BigDecimal("99.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.ONE_TIME)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.INACTIVE)
                .build();

        testProduct4 = Product.builder()
                .productNo("STUDENT_PLAN_001")
                .title("Student Plan")
                .description("Discounted student plan")
                .basePrice(new BigDecimal("9.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.5000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
    }

    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct1);
        Long productId = savedProduct.getId();

        // When
        Optional<Product> foundProduct = productRepository.findById(productId);

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getTitle()).isEqualTo("Basic Plan");
        assertThat(foundProduct.get().getBasePrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(foundProduct.get().getPriceType()).isEqualTo(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY);
        assertThat(foundProduct.get().getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);
    }

    @Test
    void findById_WhenProductNotExists_ShouldReturnEmpty() {
        // When
        Optional<Product> foundProduct = productRepository.findById(999L);

        // Then
        assertThat(foundProduct).isEmpty();
    }

    @Test
    void findByDiscountStatus_WhenProductsExist_ShouldReturnMatchingProducts() {
        // Given
        entityManager.persistAndFlush(testProduct1); // ACTIVE
        entityManager.persistAndFlush(testProduct2); // ACTIVE
        entityManager.persistAndFlush(testProduct3); // INACTIVE
        entityManager.persistAndFlush(testProduct4); // ACTIVE

        // When
        List<Product> activeProducts = productRepository.findByDiscountStatus(DiscountStatus.ACTIVE);

        // Then
        assertThat(activeProducts).hasSize(3);
        assertThat(activeProducts).extracting(Product::getTitle)
                .containsExactlyInAnyOrder("Basic Plan", "Premium Plan", "Student Plan");
        assertThat(activeProducts).extracting(Product::getDiscountStatus)
                .containsOnly(DiscountStatus.ACTIVE);
    }

    @Test
    void findByDiscountStatus_WhenNoProductsMatch_ShouldReturnEmptyList() {
        // Given
        entityManager.persistAndFlush(testProduct3); // INACTIVE

        // When
        List<Product> activeProducts = productRepository.findByDiscountStatus(DiscountStatus.ACTIVE);

        // Then
        assertThat(activeProducts).isEmpty();
    }

    @Test
    void save_ShouldPersistProductCorrectly() {
        // Given
        Product newProduct = Product.builder()
                .productNo("NEW_PLAN_001")
                .title("New Plan")
                .description("Brand new service plan")
                .basePrice(new BigDecimal("79.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        // When
        Product savedProduct = productRepository.save(newProduct);

        // Then
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getProductNo()).isEqualTo("NEW_PLAN_001");
        assertThat(savedProduct.getTitle()).isEqualTo("New Plan");
        assertThat(savedProduct.getBasePrice()).isEqualByComparingTo(new BigDecimal("79.99"));
        assertThat(savedProduct.getPriceType()).isEqualTo(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY);
        assertThat(savedProduct.getCreatedAt()).isNotNull();
        assertThat(savedProduct.getUpdatedAt()).isNotNull();
        assertThat(savedProduct.getDiscountStatus()).isEqualTo(DiscountStatus.ACTIVE);

        // Verify it can be retrieved
        Optional<Product> retrievedProduct = productRepository.findById(savedProduct.getId());
        assertThat(retrievedProduct).isPresent();
        assertThat(retrievedProduct.get().getTitle()).isEqualTo("New Plan");
        assertThat(retrievedProduct.get().getProductNo()).isEqualTo("NEW_PLAN_001");
    }

    @Test
    void save_WhenProductHasFeatures_ShouldPersistProductWithFeatures() {
        // Given
        Product product = Product.builder()
                .productNo("FEATURED_PLAN_001")
                .title("Plan With Features")
                .description("A plan with several features")
                .basePrice(new BigDecimal("49.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.ONE_TIME)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        // Create features for the product
        ProductFeature feature1 = ProductFeature.builder()
                .title("Feature 1")
                .description("First feature")
                .quota(100)
                .build();
        
        ProductFeature feature2 = ProductFeature.builder()
                .title("Feature 2")
                .description("Second feature")
                .quota(200)
                .build();
        
        // Associate features with product
        feature1.setProduct(product);
        feature2.setProduct(product);
        
        product.setFeatures(List.of(feature1, feature2));

        // When
        Product savedProduct = productRepository.save(product);

        // Then
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getProductNo()).isEqualTo("FEATURED_PLAN_001");
        assertThat(savedProduct.getTitle()).isEqualTo("Plan With Features");
        assertThat(savedProduct.getFeatures()).hasSize(2);
        assertThat(savedProduct.getFeatures()).extracting(ProductFeature::getTitle)
                .containsExactlyInAnyOrder("Feature 1", "Feature 2");
    }

    @Test
    void findAll_WhenMultipleProductsExist_ShouldReturnAllProducts() {
        // Given
        entityManager.persistAndFlush(testProduct1);
        entityManager.persistAndFlush(testProduct2);
        entityManager.persistAndFlush(testProduct3);

        // When
        List<Product> allProducts = productRepository.findAll();

        // Then
        assertThat(allProducts).hasSize(3);
        assertThat(allProducts).extracting(Product::getTitle)
                .containsExactlyInAnyOrder("Basic Plan", "Premium Plan", "Enterprise Plan");
    }

    @Test
    void deleteById_WhenProductExists_ShouldDeleteProduct() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct1);
        Long productId = savedProduct.getId();

        // When
        productRepository.deleteById(productId);

        // Then
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    void existsById_WhenProductExists_ShouldReturnTrue() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct1);
        Long productId = savedProduct.getId();

        // When
        boolean exists = productRepository.existsById(productId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenProductDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = productRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByProductNo_WhenProductExists_ShouldReturnProduct() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct1);

        // When
        Optional<Product> foundProduct = productRepository.findByProductNo("BASIC_PLAN_001");

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
        assertThat(foundProduct.get().getProductNo()).isEqualTo("BASIC_PLAN_001");
        assertThat(foundProduct.get().getTitle()).isEqualTo("Basic Plan");
    }

    @Test
    void findByProductNo_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Product> foundProduct = productRepository.findByProductNo("NONEXISTENT_PLAN_001");

        // Then
        assertThat(foundProduct).isEmpty();
    }

    @Test
    void existsByProductNo_WhenProductExists_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testProduct1);

        // When
        boolean exists = productRepository.existsByProductNo("BASIC_PLAN_001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByProductNo_WhenProductDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = productRepository.existsByProductNo("NONEXISTENT_PLAN_001");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByProductNo_WithDifferentProductNumbers_ShouldReturnCorrectProduct() {
        // Given
        entityManager.persistAndFlush(testProduct1); // BASIC_PLAN_001
        entityManager.persistAndFlush(testProduct2); // PREMIUM_PLAN_001
        entityManager.persistAndFlush(testProduct3); // ENTERPRISE_PLAN_001

        // When
        Optional<Product> basicProduct = productRepository.findByProductNo("BASIC_PLAN_001");
        Optional<Product> premiumProduct = productRepository.findByProductNo("PREMIUM_PLAN_001");
        Optional<Product> enterpriseProduct = productRepository.findByProductNo("ENTERPRISE_PLAN_001");

        // Then
        assertThat(basicProduct).isPresent();
        assertThat(basicProduct.get().getTitle()).isEqualTo("Basic Plan");
        
        assertThat(premiumProduct).isPresent();
        assertThat(premiumProduct.get().getTitle()).isEqualTo("Premium Plan");
        
        assertThat(enterpriseProduct).isPresent();
        assertThat(enterpriseProduct.get().getTitle()).isEqualTo("Enterprise Plan");
    }
}