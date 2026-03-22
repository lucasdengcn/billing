package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.handler.SubscriptionHandler;
import com.github.lucasdengcn.billing.handler.strategy.SubscriptionHandlerFactory;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRenewalRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionServiceImpl createSubscription Unit Tests")
class SubscriptionCreateSubscriptionTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Mock
    private SubscriptionRenewalRepository subscriptionRenewalRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CustomerService customerService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private SubscriptionHandlerFactory subscriptionHandlerFactory;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private SubscriptionRequest request;
    private Customer customer;
    private Product product;
    private Device device;
    private Subscription subscription;
    private SubscriptionHandler mockHandler;

    @BeforeEach
    void setUp() {
        request = new SubscriptionRequest();
        request.setCustomerId(1L);
        request.setProductId(10L);
        request.setDeviceId(100L);
        request.setStartDate(OffsetDateTime.now());
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        customer = Customer.builder()
                .id(1L)
                .build();

        product = Product.builder()
                .id(10L)
                .title("Test Product")
                .basePrice(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("0.90"))
                .priceType(PriceType.MONTHLY)
                .build();

        device = Device.builder()
                .id(100L)
                .build();

        subscription = Subscription.builder()
                .id(1L)
                .customer(customer)
                .product(product)
                .device(device)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalFee(new BigDecimal("26.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        mockHandler = mock(SubscriptionHandler.class);
    }

    @Test
    @DisplayName("Create subscription with valid request should succeed")
    void createSubscription_WithValidRequest_ShouldSucceed() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(productService.findFeaturesByProduct(10L)).thenReturn(Collections.emptyList());

        // When
        Subscription result = subscriptionService.createSubscription(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomer().getId()).isEqualTo(1L);
        assertThat(result.getProduct().getId()).isEqualTo(10L);
        assertThat(result.getDevice().getId()).isEqualTo(100L);
        verify(customerService).findById(1L);
        verify(productService).findProductById(10L);
        verify(deviceService).findById(100L);
        verify(subscriptionMapper).toEntity(request);
        verify(subscriptionHandlerFactory).getHandler(PriceType.MONTHLY);
        verify(mockHandler).handleNew(eq(product), any(Subscription.class));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Create subscription should call handler with correct parameters")
    void createSubscription_ShouldCallHandlerWithCorrectParameters() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.empty());
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(productService.findFeaturesByProduct(10L)).thenReturn(Collections.emptyList());

        // When
        Subscription result = subscriptionService.createSubscription(request);

        // Then
        verify(mockHandler).handleNew(product, subscription);
    }

    @Test
    @DisplayName("Create subscription should set customer, product, and device on subscription entity")
    void createSubscription_ShouldSetEntitiesOnSubscription() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.empty());
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(productService.findFeaturesByProduct(10L)).thenReturn(Collections.emptyList());

        // When
        Subscription result = subscriptionService.createSubscription(request);

        // Then
        verify(subscriptionMapper).toEntity(request);
        // Verify that the entities were set on the subscription
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getProduct()).isEqualTo(product);
        assertThat(result.getDevice()).isEqualTo(device);
    }

    @Test
    @DisplayName("Create subscription should handle product features correctly")
    void createSubscription_WithProductFeatures_ShouldCreateSubscriptionFeatures() {
        // Given
        ProductFeature feature1 = ProductFeature.builder()
                .id(1L)
                .title("Feature 1")
                .build();
        ProductFeature feature2 = ProductFeature.builder()
                .id(2L)
                .title("Feature 2")
                .build();
        List<ProductFeature> productFeatures = Arrays.asList(feature1, feature2);

        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.empty());
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(productService.findFeaturesByProduct(10L)).thenReturn(productFeatures);
        when(subscriptionFeatureRepository.saveAll(any())).thenReturn(new ArrayList<>());

        // When
        Subscription result = subscriptionService.createSubscription(request);

        // Then
        verify(productService).findFeaturesByProduct(10L);
        verify(subscriptionFeatureRepository).saveAll(any());
    }

    @Test
    @DisplayName("Create subscription should throw exception when customer not found")
    void createSubscription_WithNonExistentCustomer_ShouldThrowException() {
        // Given
        when(customerService.findById(1L)).thenThrow(new RuntimeException("Customer not found"));

        // When & Then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> subscriptionService.createSubscription(request))
                .withMessage("Customer not found");

        verify(customerService).findById(1L);
        verify(productService, never()).findProductById(anyLong());
        verify(deviceService, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Create subscription should throw exception when product not found")
    void createSubscription_WithNonExistentProduct_ShouldThrowException() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenThrow(new RuntimeException("Product not found"));

        // When & Then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> subscriptionService.createSubscription(request))
                .withMessage("Product not found");

        verify(customerService).findById(1L);
        verify(productService).findProductById(10L);
        verify(deviceService, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Create subscription should throw exception when device not found")
    void createSubscription_WithNonExistentDevice_ShouldThrowException() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenThrow(new RuntimeException("Device not found"));

        // When & Then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> subscriptionService.createSubscription(request))
                .withMessage("Device not found");

        verify(customerService).findById(1L);
        verify(productService).findProductById(10L);
        verify(deviceService).findById(100L);
    }

    @Test
    @DisplayName("Create subscription should validate date range")
    void createSubscription_WithInvalidDateRange_ShouldThrowException() {
        // Given
        SubscriptionRequest invalidRequest = new SubscriptionRequest();
        invalidRequest.setCustomerId(1L);
        invalidRequest.setProductId(10L);
        invalidRequest.setDeviceId(100L);
        invalidRequest.setStartDate(OffsetDateTime.now().plusDays(1));
        invalidRequest.setEndDate(OffsetDateTime.now()); // End date before start date

        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> subscriptionService.createSubscription(invalidRequest))
                .withMessage("Start date must be before end date");

        verify(customerService, never()).findById(anyLong());
        verify(productService, never()).findProductById(anyLong());
        verify(deviceService, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Create subscription should use handler factory to get appropriate handler")
    void createSubscription_ShouldUseHandlerFactoryToGetHandler() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.empty());
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(productService.findFeaturesByProduct(10L)).thenReturn(Collections.emptyList());

        // When
        Subscription result = subscriptionService.createSubscription(request);

        // Then
        verify(subscriptionHandlerFactory).getHandler(PriceType.MONTHLY);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Create subscription should throw exception when no handler is found")
    void createSubscription_WithNoHandlerFound_ShouldThrowException() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(null);

        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> subscriptionService.createSubscription(request))
                .withMessage("No handler found for price type: monthly");

        verify(subscriptionHandlerFactory).getHandler(PriceType.MONTHLY);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Create subscription should save subscription to repository")
    void createSubscription_ShouldSaveSubscriptionToRepository() {
        // Given
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.empty());
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(productService.findFeaturesByProduct(10L)).thenReturn(Collections.emptyList());

        // When
        Subscription result = subscriptionService.createSubscription(request);

        // Then
        verify(subscriptionRepository).save(any(Subscription.class));
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Create subscription should create subscription features from product features")
    void createSubscription_ShouldCreateSubscriptionFeaturesFromProductFeatures() {
        // Given
        ProductFeature productFeature = ProductFeature.builder()
                .id(1L)
                .title("Test Feature")
                .description("Test Description")
                .build();
        List<ProductFeature> productFeatures = Collections.singletonList(productFeature);

        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.empty());
        when(productService.findFeaturesByProduct(10L)).thenReturn(productFeatures);
        when(subscriptionFeatureRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // When
        Subscription result = subscriptionService.createSubscription(request);

        // Then
        verify(productService).findFeaturesByProduct(10L);
        verify(subscriptionFeatureRepository).saveAll(any());
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Create subscription should fail when device already has an active subscription to the same product")
    void createSubscription_WhenDeviceAlreadyHasActiveSubscriptionToSameProduct_ShouldThrowException() {
        // Given
        Subscription existingSubscription = Subscription.builder()
                .id(999L)
                .customer(customer)
                .product(product)
                .device(device)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.of(existingSubscription));
        
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> subscriptionService.createSubscription(request))
                .withMessage("Device 100 already has an active subscription to product 10");
        
        verify(customerService).findById(1L);
        verify(productService).findProductById(10L);
        verify(deviceService).findById(100L);
        verify(subscriptionRepository).findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Create subscription should succeed when device has an inactive subscription to the same product")
    void createSubscription_WhenDeviceHasInactiveSubscriptionToSameProduct_ShouldSucceed() {
        // Given
        Subscription inactiveSubscription = Subscription.builder()
                .id(999L)
                .customer(customer)
                .product(product)
                .device(device)
                .status(SubscriptionStatus.CANCELLED) // Different status
                .build();
        
        when(customerService.findById(1L)).thenReturn(customer);
        when(productService.findProductById(10L)).thenReturn(product);
        when(deviceService.findById(100L)).thenReturn(device);
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);
        when(subscriptionRepository.findByDeviceIdAndProductIdAndStatus(100L, 10L, SubscriptionStatus.ACTIVE)).thenReturn(java.util.Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(productService.findFeaturesByProduct(10L)).thenReturn(Collections.emptyList());
        
        // When
        Subscription result = subscriptionService.createSubscription(request);
        
        // Then
        assertThat(result).isNotNull();
        verify(subscriptionRepository).save(any(Subscription.class));
    }
}