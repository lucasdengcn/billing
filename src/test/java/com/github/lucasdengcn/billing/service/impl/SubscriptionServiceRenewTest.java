package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.*;
import com.github.lucasdengcn.billing.exception.BusinessException;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.handler.SubscriptionHandler;
import com.github.lucasdengcn.billing.handler.strategy.SubscriptionHandlerFactory;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.request.SubscriptionRenewalRequest;
import com.github.lucasdengcn.billing.repository.SubscriptionRenewalRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceRenewTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionRenewalRepository subscriptionRenewalRepository;

    @Mock
    private DeviceServiceImpl deviceService;

    @Mock
    private ProductServiceImpl productService;

    @Mock
    private SubscriptionHandlerFactory subscriptionHandlerFactory;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private PricingCalculator pricingCalculator;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private Subscription testSubscription;
    private SubscriptionRenewalRequest renewalRequest;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST-001")
                .build();

        testDevice = Device.builder()
                .id(10L)
                .deviceNo("DEV-001")
                .deviceName("Test Device")
                .customer(testCustomer)
                .build();

        testProduct = Product.builder()
                .id(100L)
                .productNo("PROD-001")
                .title("Test Product")
                .basePrice(BigDecimal.valueOf(29.99))
                .priceType(PriceType.MONTHLY)
                .discountRate(BigDecimal.valueOf(1.0))
                .build();

        testSubscription = Subscription.builder()
                .id(1000L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(30))
                .endDate(OffsetDateTime.now().plusDays(30))
                .baseFee(BigDecimal.valueOf(29.99))
                .discountRate(BigDecimal.valueOf(1.0))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        renewalRequest = new SubscriptionRenewalRequest();
        renewalRequest.setDeviceNo("DEV-001");
        renewalRequest.setProductNo("PROD-001");
        renewalRequest.setRenewalPeriods(1);
        renewalRequest.setRenewalPeriodUnit(PeriodUnit.MONTHS);
    }

    @Test
    void renewSubscription_WhenSubscriptionExistsAndIsActive_ShouldRenewSuccessfully() {
        // Given
        SubscriptionHandler mockHandler = mock(SubscriptionHandler.class);

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(testSubscription));
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);

        Subscription updatedSubscription = testSubscription.toBuilder()
                .status(SubscriptionStatus.ACTIVE)
                .build();
        when(subscriptionRepository.save(testSubscription)).thenReturn(updatedSubscription);

        // When
        Subscription result = subscriptionService.renewSubscription(renewalRequest);

        // Then
        assertThat(result).isEqualTo(updatedSubscription);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(subscriptionHandlerFactory).getHandler(PriceType.MONTHLY);
        verify(mockHandler).handleRenewal(any(Product.class), any(Subscription.class), any(SubscriptionRenewal.class));
        verify(subscriptionRenewalRepository).save(any(SubscriptionRenewal.class));
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    void renewSubscription_WhenSubscriptionIsExpired_ShouldRenewSuccessfully() {
        // Given
        Subscription expiredSubscription = testSubscription.toBuilder()
                .status(SubscriptionStatus.EXPIRED)
                .build();

        SubscriptionHandler mockHandler = mock(SubscriptionHandler.class);

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(expiredSubscription));
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);

        Subscription updatedSubscription = expiredSubscription.toBuilder()
                .status(SubscriptionStatus.ACTIVE)
                .build();
        when(subscriptionRepository.save(expiredSubscription)).thenReturn(updatedSubscription);

        // When
        Subscription result = subscriptionService.renewSubscription(renewalRequest);

        // Then
        assertThat(result).isEqualTo(updatedSubscription);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(subscriptionHandlerFactory).getHandler(PriceType.MONTHLY);
        verify(mockHandler).handleRenewal(any(Product.class), any(Subscription.class), any(SubscriptionRenewal.class));
        verify(subscriptionRenewalRepository).save(any(SubscriptionRenewal.class));
        verify(subscriptionRepository).save(expiredSubscription);
    }

    @Test
    void renewSubscription_WhenSubscriptionDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.renewSubscription(renewalRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription not found for device: DEV-001 and product: PROD-001");
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
    }

    @Test
    void renewSubscription_WhenSubscriptionIsNotActiveOrExpired_ShouldThrowException() {
        // Given
        Subscription cancelledSubscription = testSubscription.toBuilder()
                .status(SubscriptionStatus.CANCELLED)
                .build();

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(cancelledSubscription));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.renewSubscription(renewalRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot renew subscription with status: 2. Only active or expired subscriptions can be renewed.")
                .extracting("httpStatus").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
    }

    @Test
    void renewSubscription_WhenNoHandlerFound_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(testSubscription));
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> subscriptionService.renewSubscription(renewalRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No handler found for price type: monthly")
                .extracting("httpStatus").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(subscriptionHandlerFactory).getHandler(PriceType.MONTHLY);
    }

    @Test
    void renewSubscription_WhenRenewalPeriodsIsNull_ShouldUseDefaultOfOne() {
        // Given
        SubscriptionRenewalRequest requestWithNullPeriods = new SubscriptionRenewalRequest();
        requestWithNullPeriods.setDeviceNo("DEV-001");
        requestWithNullPeriods.setProductNo("PROD-001");
        requestWithNullPeriods.setRenewalPeriodUnit(PeriodUnit.MONTHS);
        // renewalPeriods is intentionally left null

        SubscriptionHandler mockHandler = mock(SubscriptionHandler.class);

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(testSubscription));
        when(subscriptionHandlerFactory.getHandler(PriceType.MONTHLY)).thenReturn(mockHandler);

        Subscription updatedSubscription = testSubscription.toBuilder()
                .status(SubscriptionStatus.ACTIVE)
                .build();
        when(subscriptionRepository.save(testSubscription)).thenReturn(updatedSubscription);

        // When
        Subscription result = subscriptionService.renewSubscription(requestWithNullPeriods);

        // Then
        assertThat(result).isEqualTo(updatedSubscription);
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(subscriptionHandlerFactory).getHandler(PriceType.MONTHLY);
        verify(mockHandler).handleRenewal(any(Product.class), any(Subscription.class), any(SubscriptionRenewal.class));
        verify(subscriptionRenewalRepository).save(any(SubscriptionRenewal.class));
        verify(subscriptionRepository).save(testSubscription);

        // Verify that the renewal was created with 1 period (the default)
        verify(subscriptionRenewalRepository).save(argThat(renewal ->
                renewal.getRenewalPeriods() != null && renewal.getRenewalPeriods().equals(1)
        ));
    }

    @Test
    void renewSubscription_WhenDeviceDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-NONEXISTENT")).thenThrow(
                new ResourceNotFoundException("Device not found with deviceNo: DEV-NONEXISTENT"));

        renewalRequest.setDeviceNo("DEV-NONEXISTENT");

        // When & Then
        assertThatThrownBy(() -> subscriptionService.renewSubscription(renewalRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Device not found with deviceNo: DEV-NONEXISTENT");
        verify(deviceService).findByDeviceNo("DEV-NONEXISTENT");
    }

    @Test
    void renewSubscription_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-NONEXISTENT")).thenThrow(
                new ResourceNotFoundException("Product not found with product number: PROD-NONEXISTENT"));

        renewalRequest.setProductNo("PROD-NONEXISTENT");

        // When & Then
        assertThatThrownBy(() -> subscriptionService.renewSubscription(renewalRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with product number: PROD-NONEXISTENT");
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-NONEXISTENT");
    }
}
