package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SubscriptionMapper interface.
 * Tests the mapping between SubscriptionRequest, Subscription entity, and SubscriptionResponse.
 */
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
        
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(entity.getEndDate()).isEqualTo(request.getEndDate());
        
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
        
        // Default values should be applied
        assertThat(entity.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(entity.getTotalFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void toEntity_WithMinimalRequest_ShouldMapCorrectly() {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(1L);
        request.setProductId(100L);
        request.setStartDate(testDateTime);
        
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Then
        assertThat(entity.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(entity.getCustomer()).isNull(); // Not mapped from request
        assertThat(entity.getProduct()).isNull();  // Not mapped from request
        
        // Optional fields should be null
        assertThat(entity.getDevice()).isNull();
        assertThat(entity.getEndDate()).isNull();
        
        // Default values should be applied
        assertThat(entity.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(entity.getTotalFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
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
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
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
        assertThat(response.getPeriods()).isEqualTo(entity.getPeriods());
        assertThat(response.getPeriodUnit()).isEqualTo(entity.getPeriodUnit());
        assertThat(response.getBaseFee()).isEqualByComparingTo(entity.getBaseFee());
        assertThat(response.getDiscountRate()).isEqualByComparingTo(entity.getDiscountRate());
        assertThat(response.getTotalFee()).isEqualByComparingTo(entity.getTotalFee());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    void toResponse_WithEntityWithoutRelatedEntities_ShouldMapIdsAsNull() {
        // Given
        Subscription entity = Subscription.builder()
                .id(999L)
                .customer(null) // No customer
                .device(null)   // No device
                .product(null)  // No product
                .startDate(testDateTime)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        // When
        SubscriptionResponse response = mapper.toResponse(entity);
        
        // Then
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getCustomerId()).isNull(); // Should be null when customer is null
        assertThat(response.getDeviceId()).isNull();   // Should be null when device is null
        assertThat(response.getProductId()).isNull();  // Should be null when product is null
        assertThat(response.getStartDate()).isEqualTo(entity.getStartDate());
        assertThat(response.getBaseFee()).isEqualByComparingTo(entity.getBaseFee());
        assertThat(response.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    void updateEntity_WithValidRequest_ShouldUpdateMappedFields() {
        // Given
        Customer originalCustomer = Customer.builder().id(1L).build();
        Product originalProduct = Product.builder().id(100L).build();
        
        Subscription existingEntity = Subscription.builder()
                .id(999L)
                .customer(originalCustomer)
                .product(originalProduct)
                .startDate(testDateTime.minusDays(10))
                .endDate(testDateTime.plusDays(20))
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("19.99"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        SubscriptionRequest updateRequest = new SubscriptionRequest();
        updateRequest.setStartDate(testDateTime);
        updateRequest.setEndDate(testDateTime.plusDays(60));
        
        // When
        mapper.updateEntity(updateRequest, existingEntity);
        
        // Then
        // Fields mapped from request should be updated
        assertThat(existingEntity.getStartDate()).isEqualTo(updateRequest.getStartDate());
        assertThat(existingEntity.getEndDate()).isEqualTo(updateRequest.getEndDate());
        
        // Ignored fields should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(999L); // Should remain unchanged
        assertThat(existingEntity.getCustomer()).isEqualTo(originalCustomer); // Should remain unchanged
        assertThat(existingEntity.getProduct()).isEqualTo(originalProduct); // Should remain unchanged
        assertThat(existingEntity.getBaseFee()).isEqualByComparingTo(new BigDecimal("19.99")); // Should remain unchanged
        assertThat(existingEntity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE); // Should remain unchanged
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
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("0.90"))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        SubscriptionRequest updateRequest = new SubscriptionRequest();
        // Setting fields to null to test the NullValuePropertyMappingStrategy.IGNORE
        updateRequest.setStartDate(null);
        updateRequest.setEndDate(null);
        
        // When
        mapper.updateEntity(updateRequest, existingEntity);
        
        // Then
        // Fields that were null in request should remain unchanged in entity
        assertThat(existingEntity.getStartDate()).isEqualTo(testDateTime.minusDays(10)); // Should keep original value
        assertThat(existingEntity.getEndDate()).isEqualTo(testDateTime.plusDays(30)); // Should keep original value
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
        
        // Check default values
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
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
        
        // When
        Subscription entity = mapper.toEntity(request);
        
        // Set up relationships for response mapping
        Customer customer = Customer.builder().id(request.getCustomerId()).name("Test Customer").build();
        Device device = Device.builder().id(request.getDeviceId()).deviceName("Test Device").build();
        Product product = Product.builder().id(request.getProductId()).title("Premium Plan").build();
        
        entity.setCustomer(customer);
        entity.setDevice(device);
        entity.setProduct(product);
        
        SubscriptionResponse response = mapper.toResponse(entity);
        
        // Then
        // Verify the flow: request -> entity -> response preserves the mappable data
        assertThat(response.getCustomerId()).isEqualTo(request.getCustomerId());
        assertThat(response.getDeviceId()).isEqualTo(request.getDeviceId());
        assertThat(response.getProductId()).isEqualTo(request.getProductId());
        assertThat(response.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(response.getEndDate()).isEqualTo(request.getEndDate());
    }
}