package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionFeature;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.response.SubscriptionFeatureResponse;
import com.github.lucasdengcn.billing.model.response.SubscriptionWithFeaturesResponse;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionServiceImpl findSubscriptionWithFeaturesById Unit Tests")
class SubscriptionServiceFindSubscriptionWithFeaturesByIdTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Subscription testSubscription;
    private SubscriptionFeature feature1;
    private SubscriptionFeature feature2;
    private SubscriptionWithFeaturesResponse mockResponse;

    @BeforeEach
    void setUp() {
        // Create a test subscription
        Customer customer = Customer.builder().id(1L).build();
        Device device = Device.builder().id(1L).build();
        Product product = Product.builder().id(1L).productNo("TEST_PRODUCT_001").build();

        testSubscription = Subscription.builder()
                .id(1L)
                .customer(customer)
                .device(device)
                .product(product)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("1.0000"))
                .totalFee(new BigDecimal("29.99"))
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        // Create test features
        ProductFeature productFeature1 = ProductFeature.builder()
                .id(101L)
                .featureType(FeatureType.API_ACCESS)
                .build();

        feature1 = SubscriptionFeature.builder()
                .id(101L)
                .subscription(testSubscription)
                .productFeature(productFeature1)
                .title("API Access")
                .description("Provides access to the API with rate limiting")
                .featureType(FeatureType.API_ACCESS)
                .quota(1000)
                .accessed(150)
                .balance(850)
                .createdAt(OffsetDateTime.now())
                .build();

        ProductFeature productFeature2 = ProductFeature.builder()
                .id(102L)
                .featureType(FeatureType.STORAGE_SPACE)
                .build();

        feature2 = SubscriptionFeature.builder()
                .id(102L)
                .subscription(testSubscription)
                .productFeature(productFeature2)
                .title("Storage")
                .description("Provides cloud storage space")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .accessed(25)
                .balance(75)
                .createdAt(OffsetDateTime.now())
                .build();

        // Create mock response
        SubscriptionFeatureResponse responseFeature1 = SubscriptionFeatureResponse.builder()
                .id(101L)
                .subscriptionId(1L)
                .productFeatureId(101L)
                .title("API Access")
                .description("Provides access to the API with rate limiting")
                .featureType(FeatureType.API_ACCESS)
                .quota(1000)
                .accessed(150)
                .balance(850)
                .createdAt(OffsetDateTime.now())
                .build();

        SubscriptionFeatureResponse responseFeature2 = SubscriptionFeatureResponse.builder()
                .id(102L)
                .subscriptionId(1L)
                .productFeatureId(102L)
                .title("Storage")
                .description("Provides cloud storage space")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .accessed(25)
                .balance(75)
                .createdAt(OffsetDateTime.now())
                .build();

        mockResponse = SubscriptionWithFeaturesResponse.builder()
                .id(1L)
                .customerId(1L)
                .deviceId(1L)
                .productId(1L)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("1.0000"))
                .totalFee(new BigDecimal("29.99"))
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .subscriptionFeatures(Arrays.asList(responseFeature1, responseFeature2))
                .build();
    }

    @Test
    @DisplayName("Should return subscription with features when subscription exists and has features")
    void findSubscriptionWithFeaturesById_WithValidSubscriptionAndFeatures_ShouldReturnResponse() {
        // Given
        List<SubscriptionFeature> subscriptionFeatures = Arrays.asList(feature1, feature2);
        when(subscriptionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSubscription));
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(subscriptionFeatures);
        when(subscriptionMapper.toWithFeaturesResponse(testSubscription)).thenReturn(mockResponse);

        // When
        SubscriptionWithFeaturesResponse result = subscriptionService.findSubscriptionWithFeaturesById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getSubscriptionFeatures()).hasSize(2);
        assertThat(result.getSubscriptionFeatures())
                .extracting(SubscriptionFeatureResponse::getId)
                .containsExactlyInAnyOrder(101L, 102L);

        verify(subscriptionRepository).findById(1L);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
        verify(subscriptionMapper).toWithFeaturesResponse(testSubscription);
    }

    @Test
    @DisplayName("Should return subscription with empty features list when subscription exists but has no features")
    void findSubscriptionWithFeaturesById_WithValidSubscriptionButNoFeatures_ShouldReturnResponseWithEmptyFeatures() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSubscription));
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(Collections.emptyList());
        when(subscriptionMapper.toWithFeaturesResponse(testSubscription)).thenReturn(
                mockResponse.toBuilder().subscriptionFeatures(Collections.emptyList()).build()
        );

        // When
        SubscriptionWithFeaturesResponse result = subscriptionService.findSubscriptionWithFeaturesById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubscriptionFeatures()).isEmpty();

        verify(subscriptionRepository).findById(1L);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
        verify(subscriptionMapper).toWithFeaturesResponse(testSubscription);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when subscription does not exist")
    void findSubscriptionWithFeaturesById_WithNonExistentSubscription_ShouldThrowResourceNotFoundException() {
        // Given
        when(subscriptionRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionWithFeaturesById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Subscription not found with id: 999");

        verify(subscriptionRepository).findById(999L);
        verify(subscriptionFeatureRepository, never()).findBySubscription(any());
        verify(subscriptionMapper, never()).toWithFeaturesResponse(any());
    }

    @Test
    @DisplayName("Should call repository methods with correct parameters")
    void findSubscriptionWithFeaturesById_ShouldCallRepositoriesWithCorrectParameters() {
        // Given
        List<SubscriptionFeature> subscriptionFeatures = Arrays.asList(feature1);
        when(subscriptionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSubscription));
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(subscriptionFeatures);
        when(subscriptionMapper.toWithFeaturesResponse(testSubscription)).thenReturn(mockResponse);

        // When
        subscriptionService.findSubscriptionWithFeaturesById(1L);

        // Then
        verify(subscriptionRepository).findById(eq(1L));
        verify(subscriptionFeatureRepository).findBySubscription(eq(testSubscription));
        verify(subscriptionMapper).toWithFeaturesResponse(eq(testSubscription));
    }

    @Test
    @DisplayName("Should handle mapper returning null by throwing IllegalStateException")
    void findSubscriptionWithFeaturesById_WithMapperReturningNull_ShouldThrowIllegalStateException() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSubscription));
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(Arrays.asList(feature1));
        when(subscriptionMapper.toWithFeaturesResponse(testSubscription)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionWithFeaturesById(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to map subscription to response for ID: 1");

        verify(subscriptionRepository).findById(1L);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
        verify(subscriptionMapper).toWithFeaturesResponse(testSubscription);
    }
}