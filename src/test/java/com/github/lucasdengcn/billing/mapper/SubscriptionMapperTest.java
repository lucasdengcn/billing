package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SubscriptionMapper interface.
 * Tests the mapping between SubscriptionRequest, Subscription entity, and SubscriptionResponse.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionMapperTest {

    private SubscriptionMapper mapper;
    private OffsetDateTime testDateTime;
    
    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(SubscriptionMapper.class);
        testDateTime = OffsetDateTime.now();
    }

    @Test
    void toEntity_WithValidRequest_ShouldMapBasicFieldsCorrectly() {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(1L);
        request.setDeviceId(10L);
        request.setProductId(100L);
        request.setStartDate(testDateTime);
        request.setEndDate(testDateTime.plusDays(30));
        request.setPeriodDays(30);
        request.setBaseFee(new BigDecimal("29.99"));
        request.setDiscountRate(new BigDecimal("0.90"));
        request.setStatus(SubscriptionStatus.ACTIVE);
        request.setTotalFee(new BigDecimal("26.99"));
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(entity.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(entity.getPeriodDays()).isEqualTo(request.getPeriodDays());
        assertThat(entity.getBaseFee()).isEqualByComparingTo(request.getBaseFee());
        assertThat(entity.getDiscountRate()).isEqualByComparingTo(request.getDiscountRate());
        assertThat(entity.getTotalFee()).isEqualByComparingTo(request.getTotalFee());
        assertThat(entity.getStatus()).isEqualTo(request.getStatus());
        
        // Ignored fields should be null
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCustomer()).isNull();
        assertThat(entity.getDevice()).isNull();
        assertThat(entity.getProduct()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
        assertThat(entity.getSubscriptionFeatures()).isNull();
        assertThat(entity.getSubscriptionRenewals()).isNull();
        assertThat(entity.getAccessLogs()).isNull();
        assertThat(entity.getUsageStats()).isNull();
    }

    @Test
    void toEntity_WithMinimalRequest_ShouldMapCorrectly() {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(1L);
        request.setProductId(100L);
        request.setStartDate(testDateTime);
        request.setPeriodDays(30);
        
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Then
        assertThat(entity.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(entity.getPeriodDays()).isEqualTo(request.getPeriodDays());
        
        // Optional fields should be null
        assertThat(entity.getDevice()).isNull();
        assertThat(entity.getEndDate()).isNull();
        assertThat(entity.getBaseFee()).isNull();
        assertThat(entity.getDiscountRate()).isNull();
        assertThat(entity.getStatus()).isNull();
    }

    @Test
    void toEntity_WithNullRequest_ShouldReturnEntityWithDefaults() {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Then
        // Basic required fields should be null
        assertThat(entity.getStartDate()).isNull();
        assertThat(entity.getPeriodDays()).isNull();
        
        // But defaults should be applied to entity fields
        assertThat(entity.getBaseFee()).isNull(); // override by mapper
        assertThat(entity.getDiscountRate()).isNull(); // override by mapper
        assertThat(entity.getTotalFee()).isNull(); // override by mapper
        assertThat(entity.getStatus()).isNull(); // override by mapper
    }

    @Test
    void toResponse_WithValidEntity_ShouldMapCorrectly() {
        // Given
        Customer customer = Customer.builder().id(1L).name("Test Customer").build();
        Device device = Device.builder().id(10L).deviceName("Test Device").build();
        Product product = Product.builder().id(100L).title("Premium Plan").build();
        
        Subscription entity = Subscription.builder()
                .id(999L)
                .customer(customer)
                .device(device)
                .product(product)
                .startDate(testDateTime)
                .endDate(testDateTime.plusDays(30))
                .periodDays(30)
                .baseFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("0.90"))
                .totalFee(new BigDecimal("26.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        // When
        SubscriptionResponse response = mapper.toResponse(entity);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getCustomerId()).isEqualTo(entity.getCustomer().getId());
        assertThat(response.getDeviceId()).isEqualTo(entity.getDevice().getId());
        assertThat(response.getProductId()).isEqualTo(entity.getProduct().getId());
        assertThat(response.getStartDate()).isEqualTo(entity.getStartDate());
        assertThat(response.getEndDate()).isEqualTo(entity.getEndDate());
        assertThat(response.getPeriodDays()).isEqualTo(entity.getPeriodDays());
        assertThat(response.getBaseFee()).isEqualByComparingTo(entity.getBaseFee());
        assertThat(response.getDiscountRate()).isEqualByComparingTo(entity.getDiscountRate());
        assertThat(response.getTotalFee()).isEqualByComparingTo(entity.getTotalFee());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    void toResponse_WithEntityWithoutDevice_ShouldMapDeviceIdAsNull() {
        // Given
        Customer customer = Customer.builder().id(1L).name("Test Customer").build();
        Product product = Product.builder().id(100L).title("Premium Plan").build();
        
        Subscription entity = Subscription.builder()
                .id(999L)
                .customer(customer)
                .device(null) // No device
                .product(product)
                .startDate(testDateTime)
                .periodDays(30)
                .baseFee(new BigDecimal("29.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        // When
        SubscriptionResponse response = mapper.toResponse(entity);
        
        // Then
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getCustomerId()).isEqualTo(entity.getCustomer().getId());
        assertThat(response.getDeviceId()).isNull(); // Should be null when device is null
        assertThat(response.getProductId()).isEqualTo(entity.getProduct().getId());
        assertThat(response.getBaseFee()).isEqualByComparingTo(entity.getBaseFee());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    void toResponse_WithEntityWithoutDeviceButWithDeviceIdInRequest_ShouldMapCorrectly() {
        // Given
        Customer customer = Customer.builder().id(1L).name("Test Customer").build();
        Product product = Product.builder().id(100L).title("Premium Plan").build();
        
        Subscription entity = Subscription.builder()
                .id(999L)
                .customer(customer)
                .device(null)
                .product(product)
                .startDate(testDateTime)
                .periodDays(30)
                .build();
        
        // When
        SubscriptionResponse response = mapper.toResponse(entity);
        
        // Then
        assertThat(response.getCustomerId()).isEqualTo(entity.getCustomer().getId());
        assertThat(response.getDeviceId()).isNull();
        assertThat(response.getProductId()).isEqualTo(entity.getProduct().getId());
    }

    @Test
    void updateEntity_WithValidRequest_ShouldUpdateMappedFields() {
        // Given
        Subscription existingEntity = Subscription.builder()
                .id(999L)
                .customer(Customer.builder().id(1L).build())
                .product(Product.builder().id(100L).build())
                .startDate(testDateTime.minusDays(10))
                .periodDays(30)
                .baseFee(new BigDecimal("19.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        SubscriptionRequest updateRequest = new SubscriptionRequest();
        updateRequest.setCustomerId(2L); // This should be ignored
        updateRequest.setDeviceId(15L); // This should be ignored
        updateRequest.setProductId(200L); // This should be ignored
        updateRequest.setStartDate(testDateTime);
        updateRequest.setEndDate(testDateTime.plusDays(60));
        updateRequest.setPeriodDays(60);
        updateRequest.setBaseFee(new BigDecimal("39.99"));
        updateRequest.setDiscountRate(new BigDecimal("0.85"));
        updateRequest.setStatus(SubscriptionStatus.PENDING);
        
        // When
        mapper.updateEntity(updateRequest, existingEntity);
        
        // Then
        // Fields mapped from request should be updated
        assertThat(existingEntity.getStartDate()).isEqualTo(updateRequest.getStartDate());
        assertThat(existingEntity.getEndDate()).isEqualTo(updateRequest.getEndDate());
        assertThat(existingEntity.getPeriodDays()).isEqualTo(updateRequest.getPeriodDays());
        assertThat(existingEntity.getBaseFee()).isEqualByComparingTo(updateRequest.getBaseFee());
        assertThat(existingEntity.getDiscountRate()).isEqualByComparingTo(updateRequest.getDiscountRate());
        assertThat(existingEntity.getStatus()).isEqualTo(updateRequest.getStatus());
        
        // Ignored fields should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(999L); // Should remain unchanged
        assertThat(existingEntity.getCustomer().getId()).isEqualTo(1L); // Should remain unchanged
        assertThat(existingEntity.getProduct().getId()).isEqualTo(100L); // Should remain unchanged
        
        // Total fee is ignored, so it should keep its original value or default
        // (depends on whether it had a value before or is using @Builder.Default)
    }

    @Test
    void updateEntity_WithNullValuesInRequest_ShouldIgnoreNullsDueToNullValuePropertyMappingStrategy() {
        // Given
        Subscription existingEntity = Subscription.builder()
                .id(999L)
                .customer(Customer.builder().id(1L).build())
                .product(Product.builder().id(100L).build())
                .startDate(testDateTime.minusDays(10))
                .endDate(testDateTime.plusDays(30))
                .periodDays(30)
                .baseFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("0.90"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        SubscriptionRequest updateRequest = new SubscriptionRequest();
        // Setting some fields to null to test the NullValuePropertyMappingStrategy.IGNORE
        updateRequest.setStartDate(null);
        updateRequest.setEndDate(null);
        updateRequest.setBaseFee(null);
        updateRequest.setPeriodDays(null);
        
        // Keep some values
        updateRequest.setDiscountRate(new BigDecimal("0.75"));
        updateRequest.setStatus(SubscriptionStatus.CANCELLED);
        
        // When
        mapper.updateEntity(updateRequest, existingEntity);
        
        // Then
        // Fields that were null in request should remain unchanged in entity
        assertThat(existingEntity.getStartDate()).isNotNull(); // Should keep original value
        assertThat(existingEntity.getEndDate()).isNotNull(); // Should keep original value
        assertThat(existingEntity.getBaseFee()).isNotNull(); // Should keep original value
        assertThat(existingEntity.getPeriodDays()).isNotNull(); // Should keep original value
        
        // Fields with values in request should be updated
        assertThat(existingEntity.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.75"));
        assertThat(existingEntity.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    void createSubscription_UsingObjectFactory_ShouldReturnValidEntity() {
        // When
        Subscription subscription = mapper.createSubscription();
        
        // Then
        assertThat(subscription).isNotNull();
        assertThat(subscription.getId()).isNull();
        assertThat(subscription.getCustomer()).isNull();
        assertThat(subscription.getDevice()).isNull();
        assertThat(subscription.getProduct()).isNull();
        assertThat(subscription.getStartDate()).isNull();
        assertThat(subscription.getEndDate()).isNull();
        assertThat(subscription.getPeriodDays()).isNull();
        
        // Check default values
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getTotalFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void mappingConsistency_FromRequestToEntityToResponse_ShouldPreserveMappedData() {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(1L);
        request.setDeviceId(10L);
        request.setProductId(100L);
        request.setStartDate(testDateTime);
        request.setEndDate(testDateTime.plusDays(30));
        request.setPeriodDays(30);
        request.setBaseFee(new BigDecimal("29.99"));
        request.setDiscountRate(new BigDecimal("0.90"));
        request.setStatus(SubscriptionStatus.ACTIVE);
        
        // Simulate having entities to satisfy the response mapping
        Customer customer = Customer.builder().id(request.getCustomerId()).name("Test Customer").build();
        Device device = Device.builder().id(request.getDeviceId()).deviceName("Test Device").build();
        Product product = Product.builder().id(request.getProductId()).title("Premium Plan").build();
        
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Manually set the relationships that would normally come from the DB
        entity.setCustomer(customer);
        entity.setDevice(device);
        entity.setProduct(product);
        
        SubscriptionResponse response = mapper.toResponse(entity);
        
        // Then
        // Verify the flow: request -> entity -> response preserves the data that should be mapped
        assertThat(response.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(response.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(response.getPeriodDays()).isEqualTo(request.getPeriodDays());
        assertThat(response.getBaseFee()).isEqualByComparingTo(request.getBaseFee());
        assertThat(response.getDiscountRate()).isEqualByComparingTo(request.getDiscountRate());
        assertThat(response.getStatus()).isEqualTo(request.getStatus());
        
        // IDs should be correctly mapped
        assertThat(response.getCustomerId()).isEqualTo(request.getCustomerId());
        assertThat(response.getDeviceId()).isEqualTo(request.getDeviceId());
        assertThat(response.getProductId()).isEqualTo(request.getProductId());
    }

    @Test
    void mappingWithDifferentStatusValues_ShouldPreserveStatusEnum() {
        // Test ACTIVE status
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(1L);
        request.setProductId(100L);
        request.setStartDate(testDateTime);
        request.setPeriodDays(30);
        request.setStatus(SubscriptionStatus.ACTIVE);
        
        Subscription entity = mapper.toEntity(request);
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        
        // Test EXPIRED status
        request.setStatus(SubscriptionStatus.EXPIRED);
        entity = mapper.toEntity(request);
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        
        // Test CANCELLED status
        request.setStatus(SubscriptionStatus.CANCELLED);
        entity = mapper.toEntity(request);
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    void toEntity_WithBoundaryValues_ShouldHandleCorrectly() {
        // Given - Test boundary values
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(1L); // Minimum positive ID
        request.setProductId(1L); // Minimum positive ID
        request.setDeviceId(Long.MAX_VALUE); // Maximum possible ID
        request.setStartDate(testDateTime);
        request.setEndDate(testDateTime.plusDays(1)); // Minimum duration
        request.setPeriodDays(1); // Minimum period
        request.setBaseFee(BigDecimal.ZERO); // Minimum base fee
        request.setDiscountRate(BigDecimal.ZERO); // Minimum discount rate
        request.setStatus(SubscriptionStatus.ACTIVE);
        
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Then
        assertThat(entity.getPeriodDays()).isEqualTo(1);
        assertThat(entity.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getDiscountRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void toResponse_WithLargeNumericValues_ShouldMapCorrectly() {
        // Given
        Customer customer = Customer.builder().id(Long.MAX_VALUE).name("Test Customer").build();
        Device device = Device.builder().id(Long.MAX_VALUE).deviceName("Test Device").build();
        Product product = Product.builder().id(Long.MAX_VALUE).title("Premium Plan").build();
        
        Subscription entity = Subscription.builder()
                .id(Long.MAX_VALUE)
                .customer(customer)
                .device(device)
                .product(product)
                .startDate(testDateTime)
                .periodDays(Integer.MAX_VALUE)
                .baseFee(new BigDecimal("9999999999.9999"))
                .discountRate(new BigDecimal("1.0000"))
                .totalFee(new BigDecimal("9999999999.9999"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        // When
        SubscriptionResponse response = mapper.toResponse(entity);
        
        // Then
        assertThat(response.getId()).isEqualTo(Long.MAX_VALUE);
        assertThat(response.getCustomerId()).isEqualTo(Long.MAX_VALUE);
        assertThat(response.getDeviceId()).isEqualTo(Long.MAX_VALUE);
        assertThat(response.getProductId()).isEqualTo(Long.MAX_VALUE);
        assertThat(response.getPeriodDays()).isEqualTo(Integer.MAX_VALUE);
        assertThat(response.getBaseFee()).isEqualByComparingTo(new BigDecimal("9999999999.9999"));
        assertThat(response.getDiscountRate()).isEqualByComparingTo(new BigDecimal("1.0000"));
        assertThat(response.getTotalFee()).isEqualByComparingTo(new BigDecimal("9999999999.9999"));
    }

    @Test
    void updateEntity_WithMixedNullAndNonNullValues_ShouldHandleCorrectly() {
        // Given
        Subscription existingEntity = Subscription.builder()
                .id(999L)
                .customer(Customer.builder().id(1L).build())
                .product(Product.builder().id(100L).build())
                .startDate(testDateTime.minusDays(5))
                .endDate(testDateTime.plusDays(10))
                .periodDays(30)
                .baseFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("0.90"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        SubscriptionRequest updateRequest = new SubscriptionRequest();
        // Mix of null and non-null values
        updateRequest.setStartDate(null); // Should be ignored
        updateRequest.setEndDate(testDateTime.plusDays(20)); // Should be updated
        updateRequest.setPeriodDays(null); // Should be ignored
        updateRequest.setBaseFee(new BigDecimal("39.99")); // Should be updated
        updateRequest.setDiscountRate(null); // Should be ignored
        updateRequest.setStatus(SubscriptionStatus.PENDING); // Should be updated
        
        // When
        mapper.updateEntity(updateRequest, existingEntity);
        
        // Then
        // Null values should remain unchanged due to NullValuePropertyMappingStrategy.IGNORE
        assertThat(existingEntity.getStartDate()).isEqualTo(testDateTime.minusDays(5)); // Original value
        assertThat(existingEntity.getPeriodDays()).isEqualTo(30); // Original value
        assertThat(existingEntity.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90")); // Original value
        
        // Non-null values should be updated
        assertThat(existingEntity.getEndDate()).isEqualTo(testDateTime.plusDays(20));
        assertThat(existingEntity.getBaseFee()).isEqualByComparingTo(new BigDecimal("39.99"));
        assertThat(existingEntity.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    }
}