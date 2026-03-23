package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
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
        "spring.datasource.url=jdbc:h2:mem:subscription-feature-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class SubscriptionFeatureRepositoryTest {

    @Autowired
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer testCustomer1;
    private Customer testCustomer2;
    private Device testDevice1;
    private Device testDevice2;
    private Product testProduct1;
    private Product testProduct2;
    private ProductFeature testFeature1;
    private ProductFeature testFeature2;
    private ProductFeature testFeature3;
    private Subscription testSubscription1;
    private Subscription testSubscription2;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        subscriptionFeatureRepository.deleteAll();

        // Create test customers
        testCustomer1 = Customer.builder()
                .name("John Doe")
                .build();

        testCustomer2 = Customer.builder()
                .name("Jane Smith")
                .build();

        // Create test devices
        testDevice1 = Device.builder()
                .deviceName("Device 1")
                .build();

        testDevice2 = Device.builder()
                .deviceName("Device 2")
                .build();

        // Create test products
        testProduct1 = Product.builder()
                .productNo("PROD_0001")
                .title("Basic Plan")
                .description("Basic service plan")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        testProduct2 = Product.builder()
                .productNo("PROD_0002")
                .title("Premium Plan")
                .description("Premium service plan")
                .basePrice(new BigDecimal("59.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        // Create test features
        testFeature1 = ProductFeature.builder()
                .featureNo("FEAT_0001")
                .title("Storage Feature")
                .description("Additional storage capacity")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .build();

        testFeature2 = ProductFeature.builder()
                .featureNo("FEAT_0002")
                .title("Bandwidth Feature")
                .description("Additional bandwidth allocation")
                .featureType(FeatureType.BANDWIDTH_LIMIT)
                .quota(200)
                .build();

        testFeature3 = ProductFeature.builder()
                .featureNo("FEAT_0003")
                .title("API Calls Feature")
                .description("Additional API call allowance")
                .featureType(FeatureType.API_ACCESS)
                .quota(500)
                .build();

        // Create test subscriptions
        testSubscription1 = Subscription.builder()
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .baseFee(new BigDecimal("29.99"))
                .totalFee(new BigDecimal("29.99"))
                .build();

        testSubscription2 = Subscription.builder()
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .baseFee(new BigDecimal("59.99"))
                .totalFee(new BigDecimal("59.99"))
                .build();
    }

    @Test
    void findById_WhenFeatureExists_ShouldReturnFeature() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testFeature1.setProduct(testProduct1);
        testFeature1 = entityManager.persistAndFlush(testFeature1);

        SubscriptionFeature subscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("Storage Feature")
                .description("Additional storage capacity")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .accessed(0)
                .balance(100)
                .build();
        SubscriptionFeature savedFeature = entityManager.persistAndFlush(subscriptionFeature);
        Long featureId = savedFeature.getId();

        // When
        Optional<SubscriptionFeature> foundFeature = subscriptionFeatureRepository.findById(featureId);

        // Then
        assertThat(foundFeature).isPresent();
        assertThat(foundFeature.get().getId()).isEqualTo(featureId);
        assertThat(foundFeature.get().getTitle()).isEqualTo("Storage Feature");
        assertThat(foundFeature.get().getQuota()).isEqualTo(100);
        assertThat(foundFeature.get().getDescription()).isEqualTo("Additional storage capacity");
        assertThat(foundFeature.get().getSubscription().getId()).isEqualTo(testSubscription1.getId());
        assertThat(foundFeature.get().getProductFeature().getId()).isEqualTo(testFeature1.getId());
    }

    @Test
    void findById_WhenFeatureNotExists_ShouldReturnEmpty() {
        // When
        Optional<SubscriptionFeature> foundFeature = subscriptionFeatureRepository.findById(999L);

        // Then
        assertThat(foundFeature).isEmpty();
    }

    @Test
    void findBySubscription_WhenFeaturesExist_ShouldReturnAllFeaturesForSubscription() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);

        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testSubscription2.setCustomer(testCustomer2);
        testSubscription2.setProduct(testProduct2);
        testSubscription2 = entityManager.persistAndFlush(testSubscription2);

        testFeature1.setProduct(testProduct1);
        testFeature2.setProduct(testProduct1);
        testFeature3.setProduct(testProduct2);

        testFeature1 = entityManager.persistAndFlush(testFeature1); // Belongs to Basic Plan
        testFeature2 = entityManager.persistAndFlush(testFeature2); // Belongs to Basic Plan
        testFeature3 = entityManager.persistAndFlush(testFeature3); // Belongs to Premium Plan

        // Create subscription features
        SubscriptionFeature subFeature1 = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("Storage Feature")
                .description("Additional storage capacity")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .accessed(0)
                .balance(100)
                .build();

        SubscriptionFeature subFeature2 = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature2)
                .title("Bandwidth Feature")
                .description("Additional bandwidth allocation")
                .featureType(FeatureType.BANDWIDTH_LIMIT)
                .quota(200)
                .accessed(0)
                .balance(200)
                .build();

        entityManager.persistAndFlush(subFeature1);
        entityManager.persistAndFlush(subFeature2);

        // When
        List<SubscriptionFeature> featuresForSub1 = subscriptionFeatureRepository.findBySubscription(testSubscription1);

        // Then
        assertThat(featuresForSub1).hasSize(2);
        assertThat(featuresForSub1).extracting(SubscriptionFeature::getTitle)
                .containsExactlyInAnyOrder("Storage Feature", "Bandwidth Feature");
        assertThat(featuresForSub1).extracting(SubscriptionFeature::getQuota)
                .containsOnly(100, 200);
        assertThat(featuresForSub1).extracting(f -> f.getSubscription().getId())
                .containsOnly(testSubscription1.getId());
    }

    @Test
    void findBySubscription_WhenNoFeaturesExist_ShouldReturnEmptyList() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        Subscription newSubscription = entityManager.persistAndFlush(testSubscription1);

        // When
        List<SubscriptionFeature> features = subscriptionFeatureRepository.findBySubscription(newSubscription);

        // Then
        assertThat(features).isEmpty();
    }

    @Test
    void findBySubscriptionAndProductFeature_WhenMatchExists_ShouldReturnFeature() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testFeature1.setProduct(testProduct1);
        testFeature1 = entityManager.persistAndFlush(testFeature1);

        SubscriptionFeature expectedFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("Storage Feature")
                .description("Additional storage capacity")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .accessed(0)
                .balance(100)
                .build();
        entityManager.persistAndFlush(expectedFeature);

        // When
        Optional<SubscriptionFeature> foundFeature = subscriptionFeatureRepository
                .findBySubscriptionAndProductFeature(testSubscription1, testFeature1);

        // Then
        assertThat(foundFeature).isPresent();
        assertThat(foundFeature.get().getTitle()).isEqualTo("Storage Feature");
        assertThat(foundFeature.get().getQuota()).isEqualTo(100);
        assertThat(foundFeature.get().getSubscription().getId()).isEqualTo(testSubscription1.getId());
        assertThat(foundFeature.get().getProductFeature().getId()).isEqualTo(testFeature1.getId());
    }

    @Test
    void findBySubscriptionAndProductFeature_WhenNoMatchExists_ShouldReturnEmpty() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testFeature1.setProduct(testProduct1);
        testFeature1 = entityManager.persistAndFlush(testFeature1);

        // When
        Optional<SubscriptionFeature> foundFeature = subscriptionFeatureRepository
                .findBySubscriptionAndProductFeature(testSubscription1, testFeature1);

        // Then
        assertThat(foundFeature).isEmpty();
    }

    @Test
    void save_ShouldPersistFeatureCorrectly() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testFeature1.setProduct(testProduct1);
        testFeature1 = entityManager.persistAndFlush(testFeature1);

        SubscriptionFeature newFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("New Feature")
                .description("Brand new feature")
                .featureType(FeatureType.CUSTOMIZATION)
                .quota(300)
                .accessed(0)
                .balance(300)
                .build();

        // When
        SubscriptionFeature savedFeature = subscriptionFeatureRepository.save(newFeature);

        // Then
        assertThat(savedFeature.getId()).isNotNull();
        assertThat(savedFeature.getTitle()).isEqualTo("New Feature");
        assertThat(savedFeature.getQuota()).isEqualTo(300);
        assertThat(savedFeature.getCreatedAt()).isNotNull();
        assertThat(savedFeature.getAccessed()).isEqualTo(0);
        assertThat(savedFeature.getBalance()).isEqualTo(300);
        assertThat(savedFeature.getSubscription().getId()).isEqualTo(testSubscription1.getId());
        assertThat(savedFeature.getProductFeature().getId()).isEqualTo(testFeature1.getId());

        // Verify it can be retrieved
        Optional<SubscriptionFeature> retrievedFeature = subscriptionFeatureRepository.findById(savedFeature.getId());
        assertThat(retrievedFeature).isPresent();
        assertThat(retrievedFeature.get().getTitle()).isEqualTo("New Feature");
    }

    @Test
    void save_WhenFeatureHasNoDescription_ShouldPersistCorrectly() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testFeature1.setProduct(testProduct1);
        testFeature1 = entityManager.persistAndFlush(testFeature1);

        SubscriptionFeature featureWithoutDescription = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("Feature Without Description")
                .featureType(FeatureType.CUSTOMIZATION)
                .quota(50)
                .accessed(0)
                .balance(50)
                .build();

        // When
        SubscriptionFeature savedFeature = subscriptionFeatureRepository.save(featureWithoutDescription);

        // Then
        assertThat(savedFeature.getId()).isNotNull();
        assertThat(savedFeature.getTitle()).isEqualTo("Feature Without Description");
        assertThat(savedFeature.getQuota()).isEqualTo(50);
        assertThat(savedFeature.getDescription()).isNull();
    }

    @Test
    void findAll_WhenMultipleFeaturesExist_ShouldReturnAllFeatures() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);

        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testSubscription2.setCustomer(testCustomer2);
        testSubscription2.setProduct(testProduct2);
        testSubscription2 = entityManager.persistAndFlush(testSubscription2);

        testFeature1.setProduct(testProduct1);
        testFeature2.setProduct(testProduct1);
        testFeature3.setProduct(testProduct2);

        testFeature1 = entityManager.persistAndFlush(testFeature1);
        testFeature2 = entityManager.persistAndFlush(testFeature2);
        testFeature3 = entityManager.persistAndFlush(testFeature3);

        SubscriptionFeature subFeature1 = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("Feature 1")
                .featureType(FeatureType.TOKEN)
                .quota(100)
                .accessed(0)
                .balance(100)
                .build();

        SubscriptionFeature subFeature2 = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature2)
                .title("Feature 2")
                .featureType(FeatureType.API_ACCESS)
                .quota(200)
                .accessed(0)
                .balance(200)
                .build();

        SubscriptionFeature subFeature3 = SubscriptionFeature.builder()
                .subscription(testSubscription2)
                .productFeature(testFeature3)
                .title("Feature 3")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(500)
                .accessed(0)
                .balance(500)
                .build();

        entityManager.persistAndFlush(subFeature1);
        entityManager.persistAndFlush(subFeature2);
        entityManager.persistAndFlush(subFeature3);

        // When
        List<SubscriptionFeature> allFeatures = subscriptionFeatureRepository.findAll();

        // Then
        assertThat(allFeatures).hasSize(3);
        assertThat(allFeatures).extracting(SubscriptionFeature::getTitle)
                .containsExactlyInAnyOrder("Feature 1", "Feature 2", "Feature 3");
    }

    @Test
    void deleteById_WhenFeatureExists_ShouldDeleteFeature() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testFeature1.setProduct(testProduct1);
        testFeature1 = entityManager.persistAndFlush(testFeature1);

        SubscriptionFeature subscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("Storage Feature")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .accessed(0)
                .balance(100)
                .build();
        SubscriptionFeature savedFeature = entityManager.persistAndFlush(subscriptionFeature);
        Long featureId = savedFeature.getId();

        // When
        subscriptionFeatureRepository.deleteById(featureId);

        // Then
        Optional<SubscriptionFeature> deletedFeature = subscriptionFeatureRepository.findById(featureId);
        assertThat(deletedFeature).isEmpty();
    }

    @Test
    void existsById_WhenFeatureExists_ShouldReturnTrue() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1.setCustomer(testCustomer1);
        testSubscription1.setProduct(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);

        testFeature1.setProduct(testProduct1);
        testFeature1 = entityManager.persistAndFlush(testFeature1);

        SubscriptionFeature subscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .title("Storage Feature")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .accessed(0)
                .balance(100)
                .build();
        SubscriptionFeature savedFeature = entityManager.persistAndFlush(subscriptionFeature);
        Long featureId = savedFeature.getId();

        // When
        boolean exists = subscriptionFeatureRepository.existsById(featureId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenFeatureDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = subscriptionFeatureRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

}