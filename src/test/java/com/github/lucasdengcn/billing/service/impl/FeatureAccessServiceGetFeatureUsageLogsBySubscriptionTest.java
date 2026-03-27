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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureAccessServiceGetFeatureUsageLogsBySubscriptionTest {

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
    void getFeatureUsageLogsBySubscription_WhenSubscriptionExists_ShouldReturnLogs() {
        // Given
        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 1);

        when(subscriptionRepository.findById(10000L)).thenReturn(Optional.of(testSubscription));
        when(logRepository.findBySubscriptionIdOrderByAccessTimeDesc(10000L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsBySubscription(10000L, pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeatureAccessLog);
    }

    @Test
    void getFeatureUsageLogsBySubscription_WhenSubscriptionDoesNotExist_ShouldThrowException() {
        // Given
        when(subscriptionRepository.findById(99999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> featureAccessService.getFeatureUsageLogsBySubscription(99999L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription not found: 99999");
    }

    @Test
    void getFeatureUsageLogsBySubscription_WhenNoLogsExist_ShouldReturnEmptyPage() {
        // Given
        Page<FeatureAccessLog> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(subscriptionRepository.findById(10000L)).thenReturn(Optional.of(testSubscription));
        when(logRepository.findBySubscriptionIdOrderByAccessTimeDesc(10000L, pageable))
                .thenReturn(emptyPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsBySubscription(10000L, pageable);

        // Then
        assertThat(result).isEqualTo(emptyPage);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getFeatureUsageLogsBySubscription_WhenMultipleLogsExist_ShouldReturnAllLogs() {
        // Given
        FeatureAccessLog log1 = testFeatureAccessLog.toBuilder().id(1000001L).usageAmount(10).build();
        FeatureAccessLog log2 = testFeatureAccessLog.toBuilder().id(1000002L).usageAmount(15).build();
        List<FeatureAccessLog> logs = List.of(log1, log2);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 2);

        when(subscriptionRepository.findById(10000L)).thenReturn(Optional.of(testSubscription));
        when(logRepository.findBySubscriptionIdOrderByAccessTimeDesc(10000L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsBySubscription(10000L, pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0)).isEqualTo(log1);
        assertThat(result.getContent().get(1)).isEqualTo(log2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getFeatureUsageLogsBySubscription_WhenPaginationApplied_ShouldRespectPageableParameters() {
        // Given
        Pageable customPageable = PageRequest.of(1, 5); // Second page with 5 items per page
        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, customPageable, 1);

        when(subscriptionRepository.findById(10000L)).thenReturn(Optional.of(testSubscription));
        when(logRepository.findBySubscriptionIdOrderByAccessTimeDesc(10000L, customPageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsBySubscription(10000L, customPageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getPageable()).isEqualTo(customPageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getFeatureUsageLogsBySubscription_WhenDifferentSubscriptionId_ShouldUseCorrectId() {
        // Given
        Subscription differentSubscription = testSubscription.toBuilder()
                .id(20000L)
                .build();

        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 1);

        when(subscriptionRepository.findById(20000L)).thenReturn(Optional.of(differentSubscription));
        when(logRepository.findBySubscriptionIdOrderByAccessTimeDesc(20000L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsBySubscription(20000L, pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(1);
        // Verify that the correct subscription ID (20000L) was used in the repository call
    }
}