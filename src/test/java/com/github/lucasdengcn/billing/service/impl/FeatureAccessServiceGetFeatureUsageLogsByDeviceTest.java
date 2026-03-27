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

@ExtendWith(MockitoExtension.class)
class FeatureAccessServiceGetFeatureUsageLogsByDeviceTest {

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
    void getFeatureUsageLogsByDevice_WhenDeviceExists_ShouldReturnLogs() {
        // Given
        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 1);

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(logRepository.findByDeviceIdOrderByAccessTimeDesc(10L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsByDevice("DEV-001", pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeatureAccessLog);
    }

    @Test
    void getFeatureUsageLogsByDevice_WhenDeviceDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-NONEXISTENT"))
                .thenThrow(new ResourceNotFoundException("Device not found: DEV-NONEXISTENT"));

        // When & Then
        assertThatThrownBy(() -> featureAccessService.getFeatureUsageLogsByDevice("DEV-NONEXISTENT", pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Device not found: DEV-NONEXISTENT");
    }

    @Test
    void getFeatureUsageLogsByDevice_WhenNoLogsExist_ShouldReturnEmptyPage() {
        // Given
        Page<FeatureAccessLog> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(deviceService.findByDeviceNo("DEV-EMPTY")).thenReturn(testDevice);
        when(logRepository.findByDeviceIdOrderByAccessTimeDesc(10L, pageable))
                .thenReturn(emptyPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsByDevice("DEV-EMPTY", pageable);

        // Then
        assertThat(result).isEqualTo(emptyPage);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getFeatureUsageLogsByDevice_WhenMultipleLogsExist_ShouldReturnAllLogs() {
        // Given
        FeatureAccessLog log1 = testFeatureAccessLog.toBuilder().id(1000001L).usageAmount(10).build();
        FeatureAccessLog log2 = testFeatureAccessLog.toBuilder().id(1000002L).usageAmount(15).build();
        List<FeatureAccessLog> logs = List.of(log1, log2);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 2);

        when(deviceService.findByDeviceNo("DEV-MULTIPLE")).thenReturn(testDevice);
        when(logRepository.findByDeviceIdOrderByAccessTimeDesc(10L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsByDevice("DEV-MULTIPLE", pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0)).isEqualTo(log1);
        assertThat(result.getContent().get(1)).isEqualTo(log2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getFeatureUsageLogsByDevice_WhenPaginationApplied_ShouldRespectPageableParameters() {
        // Given
        Pageable customPageable = PageRequest.of(1, 5); // Second page with 5 items per page
        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, customPageable, 1);

        when(deviceService.findByDeviceNo("DEV-PAGINATION")).thenReturn(testDevice);
        when(logRepository.findByDeviceIdOrderByAccessTimeDesc(10L, customPageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsByDevice("DEV-PAGINATION", customPageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getPageable()).isEqualTo(customPageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getFeatureUsageLogsByDevice_WhenDeviceHasDifferentId_ShouldUseCorrectDeviceId() {
        // Given
        Device differentDevice = testDevice.toBuilder()
                .id(99L)
                .deviceNo("DEV-99")
                .build();

        List<FeatureAccessLog> logs = List.of(testFeatureAccessLog);
        Page<FeatureAccessLog> expectedPage = new PageImpl<>(logs, pageable, 1);

        when(deviceService.findByDeviceNo("DEV-99")).thenReturn(differentDevice);
        when(logRepository.findByDeviceIdOrderByAccessTimeDesc(99L, pageable))
                .thenReturn(expectedPage);

        // When
        Page<FeatureAccessLog> result = featureAccessService.getFeatureUsageLogsByDevice("DEV-99", pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(1);
        // Verify that the correct device ID (99L) was used in the repository call
        // This is verified by the mock setup and the fact that the test passes
    }
}
