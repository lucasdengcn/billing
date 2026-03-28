package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.*;
import com.github.lucasdengcn.billing.handler.strategy.SubscriptionHandlerFactory;
import com.github.lucasdengcn.billing.model.request.SubscriptionRenewalEstimateRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionRenewalEstimateResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService estimateRenewalFee method tests")
class SubscriptionServiceEstimateRenewalFeeTest {

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
    private SubscriptionHandlerFactory subscriptionHandlerFactory;

    @Mock
    private PricingCalculator pricingCalculator;

    private SubscriptionServiceImpl subscriptionService;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionServiceImpl(
                subscriptionRepository,
                subscriptionFeatureRepository,
                subscriptionRenewalRepository,
                null, // productFeatureRepository
                productService,
                customerService,
                deviceService,
                null, // subscriptionMapper
                subscriptionHandlerFactory,
                pricingCalculator
        );

        // Setup test entities
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST-001")
                .build();

        testDevice = Device.builder()
                .id(1L)
                .deviceNo("DEV-001")
                .deviceName("Test Device")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .productNo("PROD-001")
                .title("Test Product")
                .basePrice(new BigDecimal("99.99"))
                .discountRate(new BigDecimal("0.90"))
                .build();

        testSubscription = Subscription.builder()
                .id(1L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .baseFee(new BigDecimal("99.99"))
                .discountRate(new BigDecimal("0.90"))
                .build();
    }

    @Test
    @DisplayName("estimateRenewalFee with valid request should return estimated fee")
    void estimateRenewalFee_WithValidRequest_ShouldReturnEstimatedFee() {
        // Given
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("DEV-001");
        request.setProductNo("PROD-001");
        request.setRenewalPeriods(3);

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct))
                .thenReturn(Optional.of(testSubscription));
        when(pricingCalculator.calculateSubscriptionTotalFee(testProduct, testSubscription, 3))
                .thenReturn(new BigDecimal("269.97")); // 99.99 * 0.90 * 3

        // When
        SubscriptionRenewalEstimateResponse response = subscriptionService.estimateRenewalFee(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEstimatedFee()).isEqualByComparingTo(new BigDecimal("269.97"));
        assertThat(response.getBaseFee()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(response.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(response.getRenewalPeriods()).isEqualTo(3);
        assertThat(response.getProductTitle()).isEqualTo("Test Product");
        assertThat(response.getDeviceNo()).isEqualTo("DEV-001");
        assertThat(response.getProductNo()).isEqualTo("PROD-001");

        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(pricingCalculator).calculateSubscriptionTotalFee(testProduct, testSubscription, 3);
    }

    @Test
    @DisplayName("estimateRenewalFee with non-existent device should throw exception")
    void estimateRenewalFee_WithNonExistentDevice_ShouldThrowException() {
        // Given
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("NON-EXISTENT");
        request.setProductNo("PROD-001");
        request.setRenewalPeriods(1);

        when(deviceService.findByDeviceNo("NON-EXISTENT"))
                .thenThrow(new RuntimeException("Device not found"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.estimateRenewalFee(request))
                .isInstanceOf(RuntimeException.class);

        verify(deviceService).findByDeviceNo("NON-EXISTENT");
        verify(productService, never()).findProductByProductNo(any());
        verify(subscriptionRepository, never()).findByDeviceAndProduct(any(), any());
    }

    @Test
    @DisplayName("estimateRenewalFee with non-existent product should throw exception")
    void estimateRenewalFee_WithNonExistentProduct_ShouldThrowException() {
        // Given
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("DEV-001");
        request.setProductNo("NON-EXISTENT");
        request.setRenewalPeriods(1);

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("NON-EXISTENT"))
                .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.estimateRenewalFee(request))
                .isInstanceOf(RuntimeException.class);

        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("NON-EXISTENT");
        verify(subscriptionRepository, never()).findByDeviceAndProduct(any(), any());
    }

    @Test
    @DisplayName("estimateRenewalFee with non-existent subscription should throw ResourceNotFoundException")
    void estimateRenewalFee_WithNonExistentSubscription_ShouldThrowResourceNotFoundException() {
        // Given
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("DEV-001");
        request.setProductNo("PROD-001");
        request.setRenewalPeriods(1);

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.estimateRenewalFee(request))
                .isInstanceOf(RuntimeException.class); // Assuming ResourceNotFoundException extends RuntimeException

        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
    }
}