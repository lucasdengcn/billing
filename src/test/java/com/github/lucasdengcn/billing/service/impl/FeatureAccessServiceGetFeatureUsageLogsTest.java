package com.github.lucasdengcn.billing.service.impl;


import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.repository.FeatureAccessLogRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureAccessServiceGetFeatureUsageLogsTest {

    @Mock
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Mock
    private FeatureAccessLogRepository logRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private FeatureAccessServiceImpl featureAccessService;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private ProductFeature testProductFeature;
    private Subscription testSubscription;
    private SubscriptionFeature testSubscriptionFeature;
    private FeatureAccessLog testFeatureAccessLog;
    private Pageable pageable;

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
                .basePrice(new BigDecimal("29.99"))
                .build();

        testProductFeature = ProductFeature.builder()
                .id(1000L)
                .featureNo("FEATURE-001")
                .title("Test Feature")
                .build();

        testSubscription = Subscription.builder()
                .id(10000L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(30))
                .endDate(OffsetDateTime.now().plusDays(30))
                .baseFee(new BigDecimal("29.99"))
                .build();

        testSubscriptionFeature = SubscriptionFeature.builder()
                .id(100000L)
                .subscription(testSubscription)
                .productFeature(testProductFeature)
                .device(testDevice)
                .trackId("TRACK-001")
                .build();

        testFeatureAccessLog = FeatureAccessLog.builder()
                .id(1000000L)
                .subscriptionId(10000L)
                .productFeatureId(1000L)
                .deviceId(10L)
                .usageAmount(5)
                .detailValue("Test usage")
                .accessTime(OffsetDateTime.now())
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getFeatureUsageLogs_WhenSubscriptionFeatureExists_ShouldReturnLogs() {
        // Given
        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 1);

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .thenReturn(testSubscriptionFeature);
        when(logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogs("DEV-001", "PROD-001", "FEATURE-001", pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeatureAccessLog);
    }

    @Test
    void getFeatureUsageLogs_WhenSubscriptionFeatureDoesNotExist_ShouldThrowException() {
        // Given
        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-NONEXISTENT", "FEATURE-NONEXISTENT", "PROD-NONEXISTENT"))
                .thenThrow(new ResourceNotFoundException("Subscription feature not found for device: DEV-NONEXISTENT, feature: FEATURE-NONEXISTENT, product: PROD-NONEXISTENT"));

        // When & Then
        assertThatThrownBy(() -> featureAccessService.getFeatureUsageLogs("DEV-NONEXISTENT", "PROD-NONEXISTENT","FEATURE-NONEXISTENT", pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription feature not found for device: DEV-NONEXISTENT, feature: FEATURE-NONEXISTENT, product: PROD-NONEXISTENT");
    }

    @Test
    void getFeatureUsageLogs_WhenSubscriptionFeatureReturnsNull_ShouldThrowException() {
        // Given
        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-NULL", "FEATURE-NULL", "PROD-NULL"))
                .thenThrow(new ResourceNotFoundException("Subscription feature not found for device: DEV-NULL, feature: FEATURE-NULL, product: PROD-NULL"));

        // When & Then
        assertThatThrownBy(() -> featureAccessService.getFeatureUsageLogs("DEV-NULL", "PROD-NULL", "FEATURE-NULL", pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription feature not found for device: DEV-NULL, feature: FEATURE-NULL, product: PROD-NULL");
    }

    @Test
    void getFeatureUsageLogs_WhenNoLogsExist_ShouldReturnEmptyPage() {
        // Given
        Page<FeatureAccessLog> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-EMPTY", "FEATURE-EMPTY", "PROD-EMPTY"))
                .thenReturn(testSubscriptionFeature);
        when(logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, pageable))
                .thenReturn(emptyPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogs("DEV-EMPTY", "PROD-EMPTY", "FEATURE-EMPTY", pageable);

        // Then
        assertThat(result).isEqualTo(emptyPage);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getFeatureUsageLogs_WhenMultipleLogsExist_ShouldReturnAllLogs() {
        // Given
        FeatureAccessLog log1 = testFeatureAccessLog.toBuilder().id(1000001L).usageAmount(10).build();
        FeatureAccessLog log2 = testFeatureAccessLog.toBuilder().id(1000002L).usageAmount(15).build();
        List<FeatureAccessLog> logs = List.of(log1, log2);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 2);

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-MULTIPLE", "FEATURE-MULTIPLE", "PROD-MULTIPLE"))
                .thenReturn(testSubscriptionFeature);
        when(logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogs("DEV-MULTIPLE", "PROD-MULTIPLE", "FEATURE-MULTIPLE", pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0)).isEqualTo(log1);
        assertThat(result.getContent().get(1)).isEqualTo(log2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getFeatureUsageLogs_WhenPaginationApplied_ShouldRespectPageableParameters() {
        // Given
        Pageable customPageable = PageRequest.of(1, 5); // Second page with 5 items per page
        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, customPageable, 1);

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-PAGINATION", "FEATURE-PAGINATION", "PROD-PAGINATION"))
                .thenReturn(testSubscriptionFeature);
        when(logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, customPageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogs("DEV-PAGINATION", "PROD-PAGINATION", "FEATURE-PAGINATION", customPageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getPageable()).isEqualTo(customPageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getFeatureUsageLogs_VerifyCorrectMethodCalls() {
        // Given
        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 1);

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .thenReturn(testSubscriptionFeature);
        when(logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogs("DEV-001", "PROD-001", "FEATURE-001", pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);

        // Verify the correct methods were called with correct parameters
        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");
        verify(logRepository).findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, pageable);
        verifyNoMoreInteractions(subscriptionFeatureRepository, deviceService, subscriptionRepository);
    }

    @Test
    void getFeatureUsageLogs_WhenExceptionOccurs_ShouldVerifyNoExtraInteractions() {
        // Given
        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-ERROR", "FEATURE-ERROR", "PROD-ERROR"))
                .thenThrow(new ResourceNotFoundException("Test error"));

        // When & Then
        assertThatThrownBy(() -> featureAccessService.getFeatureUsageLogs("DEV-ERROR", "PROD-ERROR", "FEATURE-ERROR", pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        // Verify that only the expected method was called and no others
        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-ERROR", "FEATURE-ERROR", "PROD-ERROR");
        verifyNoMoreInteractions(logRepository, subscriptionFeatureRepository, deviceService, subscriptionRepository);
    }

    @Test
    void getFeatureUsageLogs_WithDifferentPageSizes_ShouldReturnCorrectCounts() {
        // Given
        FeatureAccessLog log1 = testFeatureAccessLog.toBuilder().id(1000001L).build();
        FeatureAccessLog log2 = testFeatureAccessLog.toBuilder().id(1000002L).build();
        FeatureAccessLog log3 = testFeatureAccessLog.toBuilder().id(1000003L).build();
        List<FeatureAccessLog> allLogs = List.of(log1, log2, log3);

        Pageable pageSize2 = PageRequest.of(0, 2);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(List.of(log1, log2), pageSize2, 3); // 3 total elements

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-PAGESIZE", "FEATURE-PAGESIZE", "PROD-PAGESIZE"))
                .thenReturn(testSubscriptionFeature);
        when(logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, pageSize2))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogs("DEV-PAGESIZE", "PROD-PAGESIZE", "FEATURE-PAGESIZE", pageSize2);

        // Then
        assertThat(result.getContent()).hasSize(2); // Page size is 2
        assertThat(result.getTotalElements()).isEqualTo(3); // Total elements is 3
        assertThat(result.getNumber()).isEqualTo(0); // Current page number
        assertThat(result.getNumberOfElements()).isEqualTo(2); // Elements in current page
    }

    @Test
    void getFeatureUsageLogs_WithDifferentPageNumbers_ShouldReturnCorrectPage() {
        // Given
        FeatureAccessLog log1 = testFeatureAccessLog.toBuilder().id(1000001L).build();
        List<FeatureAccessLog> logs = List.of(log1);
        Pageable secondPage = PageRequest.of(1, 1); // Page 1 (second page), size 1
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, secondPage, 10); // 10 total elements

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-PAGENUM", "FEATURE-PAGENUM", "PROD-PAGENUM"))
                .thenReturn(testSubscriptionFeature);
        when(logRepository.findBySubscriptionIdAndProductFeatureIdOrderByAccessTimeDesc(10000L, 1000L, secondPage))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogs("DEV-PAGENUM", "PROD-PAGENUM", "FEATURE-PAGENUM", secondPage);

        // Then
        assertThat(result.getNumber()).isEqualTo(1); // Should be page 1 (second page)
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(10);
    }

}
