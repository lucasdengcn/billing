package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
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
        "spring.datasource.url=jdbc:h2:mem:subscription-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer testCustomer1;
    private Customer testCustomer2;
    private Device testDevice1;
    private Device testDevice2;
    private Product testProduct1;
    private Product testProduct2;
    private Subscription testSubscription1;
    private Subscription testSubscription2;
    private Subscription testSubscription3;
    private Subscription testSubscription4;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        subscriptionRepository.deleteAll();

        // Create test customers
        testCustomer1 = Customer.builder()
                .name("John Doe")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();

        testCustomer2 = Customer.builder()
                .name("Jane Smith")
                .customerNo("CUST002")
                .mobileNo("0987654321")
                .build();

        // Create test devices
        testDevice1 = Device.builder()
                .customer(testCustomer1)
                .deviceName("Device 1")
                .deviceNo("DEV001")
                .deviceType("Mobile")
                .build();

        testDevice2 = Device.builder()
                .customer(testCustomer2)
                .deviceName("Device 2")
                .deviceNo("DEV002")
                .deviceType("Desktop")
                .build();

        // Create test products
        testProduct1 = Product.builder()
                .title("Basic Plan")
                .description("Basic service plan")
                .basePrice(new BigDecimal("29.99"))
                .build();

        testProduct2 = Product.builder()
                .title("Premium Plan")
                .description("Premium service plan")
                .basePrice(new BigDecimal("59.99"))
                .build();

        // Create test subscriptions
        testSubscription1 = Subscription.builder()
                .customer(testCustomer1)
                .device(testDevice1)
                .product(testProduct1)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusDays(30))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .totalFee(new BigDecimal("29.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        testSubscription2 = Subscription.builder()
                .customer(testCustomer1)
                .device(testDevice1)
                .product(testProduct2)
                .startDate(OffsetDateTime.now().minusDays(2))
                .endDate(OffsetDateTime.now().plusDays(28))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("59.99"))
                .totalFee(new BigDecimal("59.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        testSubscription3 = Subscription.builder()
                .customer(testCustomer2)
                .device(testDevice2)
                .product(testProduct1)
                .startDate(OffsetDateTime.now().minusDays(3))
                .endDate(OffsetDateTime.now().plusDays(27))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .totalFee(new BigDecimal("29.99"))
                .status(SubscriptionStatus.PENDING)
                .build();

        testSubscription4 = Subscription.builder()
                .customer(testCustomer2)
                .device(testDevice2)
                .product(testProduct2)
                .startDate(OffsetDateTime.now().minusDays(4))
                .endDate(OffsetDateTime.now().minusDays(1)) // Expired
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("59.99"))
                .totalFee(new BigDecimal("59.99"))
                .status(SubscriptionStatus.EXPIRED)
                .build();
    }

    @Test
    void findById_WhenSubscriptionExists_ShouldReturnSubscription() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);
        Long subscriptionId = testSubscription1.getId();

        // When
        Optional<Subscription> foundSubscription = subscriptionRepository.findById(subscriptionId);

        // Then
        assertThat(foundSubscription).isPresent();
        assertThat(foundSubscription.get().getId()).isEqualTo(subscriptionId);
        assertThat(foundSubscription.get().getCustomer().getName()).isEqualTo("John Doe");
        assertThat(foundSubscription.get().getProduct().getTitle()).isEqualTo("Basic Plan");
        assertThat(foundSubscription.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(foundSubscription.get().getBaseFee()).isEqualByComparingTo(new BigDecimal("29.99"));
    }

    @Test
    void findById_WhenSubscriptionNotExists_ShouldReturnEmpty() {
        // When
        Optional<Subscription> foundSubscription = subscriptionRepository.findById(999L);

        // Then
        assertThat(foundSubscription).isEmpty();
    }

    @Test
    void findByCustomer_WhenCustomerHasSubscriptions_ShouldReturnAllSubscriptions() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);
        testSubscription2 = entityManager.persistAndFlush(testSubscription2);

        // When
        List<Subscription> subscriptions = subscriptionRepository.findByCustomer(testCustomer1);

        // Then
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptions).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(testSubscription1.getId(), testSubscription2.getId());
        assertThat(subscriptions).extracting(Subscription::getCustomer)
                .extracting(Customer::getName)
                .containsOnly("John Doe");
    }

    @Test
    void findByCustomer_WhenCustomerHasNoSubscriptions_ShouldReturnEmptyList() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription3 = entityManager.persistAndFlush(testSubscription3); // Owned by customer2

        // When
        List<Subscription> subscriptions = subscriptionRepository.findByCustomer(testCustomer1);

        // Then
        assertThat(subscriptions).isEmpty();
    }

    @Test
    void findByDevice_WhenDeviceHasSubscriptions_ShouldReturnAllSubscriptions() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);
        testSubscription2 = entityManager.persistAndFlush(testSubscription2);

        // When
        List<Subscription> subscriptions = subscriptionRepository.findByDevice(testDevice1);

        // Then
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptions).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(testSubscription1.getId(), testSubscription2.getId());
        assertThat(subscriptions).extracting(Subscription::getDevice)
                .extracting(Device::getDeviceNo)
                .containsOnly("DEV001");
    }

    @Test
    void findByDevice_WhenDeviceHasNoSubscriptions_ShouldReturnEmptyList() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription3 = entityManager.persistAndFlush(testSubscription3); // Owned by device2

        // When
        List<Subscription> subscriptions = subscriptionRepository.findByDevice(testDevice1);

        // Then
        assertThat(subscriptions).isEmpty();
    }

    @Test
    void findByProduct_WhenProductHasSubscriptions_ShouldReturnAllSubscriptions() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1); // Product 1
        testSubscription3 = entityManager.persistAndFlush(testSubscription3); // Product 1

        // When
        List<Subscription> subscriptions = subscriptionRepository.findByProduct(testProduct1);

        // Then
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptions).extracting(Subscription::getProduct)
                .extracting(Product::getTitle)
                .containsOnly("Basic Plan");
        assertThat(subscriptions).extracting(Subscription::getProduct)
                .containsOnly(testProduct1);
    }

    @Test
    void findByProduct_WhenProductHasNoSubscriptions_ShouldReturnEmptyList() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testSubscription2 = entityManager.persistAndFlush(testSubscription2); // Product 2 only

        // When
        List<Subscription> subscriptions = subscriptionRepository.findByProduct(testProduct1);

        // Then
        assertThat(subscriptions).isEmpty();
    }

    @Test
    void findByStatus_WhenSubscriptionsWithStatusExist_ShouldReturnMatchingSubscriptions() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1); // ACTIVE
        testSubscription2 = entityManager.persistAndFlush(testSubscription2); // ACTIVE
        testSubscription3 = entityManager.persistAndFlush(testSubscription3); // PENDING
        testSubscription4 = entityManager.persistAndFlush(testSubscription4); // EXPIRED

        // When
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

        // Then
        assertThat(activeSubscriptions).hasSize(2);
        assertThat(activeSubscriptions).extracting(Subscription::getStatus)
                .containsOnly(SubscriptionStatus.ACTIVE);
        assertThat(activeSubscriptions).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(testSubscription1.getId(), testSubscription2.getId());
    }

    @Test
    void findByStatus_WhenNoSubscriptionsWithStatusExist_ShouldReturnEmptyList() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testSubscription3 = entityManager.persistAndFlush(testSubscription3); // PENDING
        testSubscription4 = entityManager.persistAndFlush(testSubscription4); // EXPIRED

        // When
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

        // Then
        assertThat(activeSubscriptions).isEmpty();
    }

    @Test
    void save_ShouldPersistSubscriptionCorrectly() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        Subscription newSubscription = Subscription.builder()
                .customer(testCustomer1)
                .device(testDevice1)
                .product(testProduct1)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("39.99"))
                .totalFee(new BigDecimal("39.99"))
                .status(SubscriptionStatus.PENDING)
                .build();

        // When
        Subscription savedSubscription = subscriptionRepository.save(newSubscription);

        // Then
        assertThat(savedSubscription.getId()).isNotNull();
        assertThat(savedSubscription.getCustomer().getName()).isEqualTo("John Doe");
        assertThat(savedSubscription.getProduct().getTitle()).isEqualTo("Basic Plan");
        assertThat(savedSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(savedSubscription.getBaseFee()).isEqualByComparingTo(new BigDecimal("39.99"));
        assertThat(savedSubscription.getCreatedAt()).isNotNull();
        assertThat(savedSubscription.getUpdatedAt()).isNotNull();

        // Verify it can be retrieved
        Optional<Subscription> retrievedSubscription = subscriptionRepository.findById(savedSubscription.getId());
        assertThat(retrievedSubscription).isPresent();
        assertThat(retrievedSubscription.get().getCustomer().getName()).isEqualTo("John Doe");
    }

    @Test
    void save_WhenSubscriptionHasMultipleStatuses_ShouldPersistCorrectly() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        Subscription pendingSubscription = Subscription.builder()
                .customer(testCustomer1)
                .device(testDevice1)
                .product(testProduct1)
                .startDate(OffsetDateTime.now().plusDays(1))
                .endDate(OffsetDateTime.now().plusDays(31))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .totalFee(new BigDecimal("29.99"))
                .status(SubscriptionStatus.PENDING)
                .build();

        // When
        Subscription savedSubscription = subscriptionRepository.save(pendingSubscription);

        // Then
        assertThat(savedSubscription.getId()).isNotNull();
        assertThat(savedSubscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(savedSubscription.getStartDate()).isAfter(OffsetDateTime.now());
    }

    @Test
    void findAll_WhenMultipleSubscriptionsExist_ShouldReturnAllSubscriptions() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testCustomer2 = entityManager.persistAndFlush(testCustomer2);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);
        testSubscription2 = entityManager.persistAndFlush(testSubscription2);
        testSubscription3 = entityManager.persistAndFlush(testSubscription3);

        // When
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        // Then
        assertThat(allSubscriptions).hasSize(3);
        assertThat(allSubscriptions).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(
                        testSubscription1.getId(),
                        testSubscription2.getId(),
                        testSubscription3.getId());
    }

    @Test
    void deleteById_WhenSubscriptionExists_ShouldDeleteSubscription() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);
        Long subscriptionId = testSubscription1.getId();

        // When
        subscriptionRepository.deleteById(subscriptionId);
        entityManager.flush();

        // Then
        Optional<Subscription> deletedSubscription = subscriptionRepository.findById(subscriptionId);
        assertThat(deletedSubscription).isEmpty();

        List<Subscription> remainingSubscriptions = subscriptionRepository.findAll();
        assertThat(remainingSubscriptions).doesNotContain(testSubscription1);
    }

    @Test
    void existsById_WhenSubscriptionExists_ShouldReturnTrue() {
        // Given
        testCustomer1 = entityManager.persistAndFlush(testCustomer1);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        testSubscription1 = entityManager.persistAndFlush(testSubscription1);
        Long subscriptionId = testSubscription1.getId();

        // When
        boolean exists = subscriptionRepository.existsById(subscriptionId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenSubscriptionDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = subscriptionRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }
}