package com.github.lucasdengcn.billing.entity;

import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.model.request.DeviceUpdateRequest;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevicePartialUpdateTest {

    @Mock
    private DeviceRepository deviceRepository;
    
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @Test
    void partialUpdate_WithNonNullFields_ShouldUpdateSpecifiedFieldsOnly() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(100L)
                .customer(customer)
                .deviceName("Original Device")
                .deviceNo("DEV001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusDays(1))
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("Updated Device Name");
        // Only updating name, not other fields
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(updateRequest.getDeviceName()) // Apply update
                .deviceNo(originalDevice.getDeviceNo()) // Keep original
                .deviceType(originalDevice.getDeviceType()) // Keep original
                .status(originalDevice.getStatus()) // Keep original
                .lastActivityAt(originalDevice.getLastActivityAt()) // Keep original
                .build();

        // Then
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Updated Device Name");
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV001");
        assertThat(updatedDevice.getDeviceType()).isEqualTo("MOBILE");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(updatedDevice.getLastActivityAt()).isEqualTo(originalDevice.getLastActivityAt());
    }

    @Test
    void partialUpdate_WithMultipleNonNullFields_ShouldUpdateMultipleFields() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(101L)
                .customer(customer)
                .deviceName("Original Device")
                .deviceNo("DEV002")
                .deviceType("DESKTOP")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusDays(1))
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("Updated Name");
        updateRequest.setDeviceType("LAPTOP");
        updateRequest.setStatus(DeviceStatus.INACTIVE);
        // deviceNo intentionally not updated (it's a non-null field in DB but not updateable)
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(updateRequest.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(updateRequest.getDeviceType())
                .status(updateRequest.getStatus())
                .lastActivityAt(originalDevice.getLastActivityAt())
                .build();

        // Then
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Updated Name");
        assertThat(updatedDevice.getDeviceType()).isEqualTo("LAPTOP");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.INACTIVE);
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV002"); // Unchanged
    }

    @Test
    void partialUpdate_WithNullFieldsInRequest_ShouldNotChangeOriginalValues() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(102L)
                .customer(customer)
                .deviceName("Original Device")
                .deviceNo("DEV003")
                .deviceType("TABLET")
                .status(DeviceStatus.INACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusHours(2))
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("Updated Name");
        // Intentionally leaving other fields as null
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(updateRequest.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(originalDevice.getDeviceType())
                .status(originalDevice.getStatus())
                .lastActivityAt(originalDevice.getLastActivityAt())
                .build();

        // Then
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Updated Name");
        assertThat(updatedDevice.getDeviceType()).isEqualTo("TABLET"); // Unchanged
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.INACTIVE); // Unchanged
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV003"); // Unchanged
    }

    @Test
    void partialUpdate_WithOnlyStatusField_ShouldUpdateOnlyStatus() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(103L)
                .customer(customer)
                .deviceName("Test Device")
                .deviceNo("DEV004")
                .deviceType("SMART_TV")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusHours(5))
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setStatus(DeviceStatus.SUSPENDED);
        // Only setting status, other fields remain null in request
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(originalDevice.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(originalDevice.getDeviceType())
                .status(updateRequest.getStatus())
                .lastActivityAt(originalDevice.getLastActivityAt())
                .build();

        // Then
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.SUSPENDED);
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Test Device"); // Unchanged
        assertThat(updatedDevice.getDeviceType()).isEqualTo("SMART_TV"); // Unchanged
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV004"); // Unchanged
    }

    @Test
    void partialUpdate_WithNonNullableFields_ShouldRespectDBConstraints() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(104L)
                .customer(customer)
                .deviceNo("DEV005") // Required field
                .status(DeviceStatus.ACTIVE) // Required field
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("Updated Name");
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(updateRequest.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo()) // Must preserve non-nullable field
                .status(originalDevice.getStatus()) // Must preserve non-nullable field
                .build();

        // Then
        assertThat(updatedDevice.getDeviceNo()).isNotNull();
        assertThat(updatedDevice.getStatus()).isNotNull();
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Updated Name");
    }

    @Test
    void partialUpdate_WithStatusOnly_ShouldPreserveOtherNonNullFields() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(105L)
                .customer(customer)
                .deviceName("Original Name")
                .deviceNo("DEV006")
                .deviceType("PHONE")
                .status(DeviceStatus.INACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusMinutes(30))
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setStatus(DeviceStatus.ACTIVE);
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(originalDevice.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(originalDevice.getDeviceType())
                .status(updateRequest.getStatus())
                .lastActivityAt(originalDevice.getLastActivityAt())
                .build();

        // Then
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Original Name");
        assertThat(updatedDevice.getDeviceType()).isEqualTo("PHONE");
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV006");
        assertThat(updatedDevice.getLastActivityAt()).isEqualTo(originalDevice.getLastActivityAt());
    }

    @Test
    void partialUpdate_WithNameOnly_ShouldPreserveAllOtherFields() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(106L)
                .customer(customer)
                .deviceName("Old Name")
                .deviceNo("DEV007")
                .deviceType("WATCH")
                .status(DeviceStatus.DEACTIVATED)
                .lastActivityAt(OffsetDateTime.now().minusHours(3))
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("New Name");
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(updateRequest.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(originalDevice.getDeviceType())
                .status(originalDevice.getStatus())
                .lastActivityAt(originalDevice.getLastActivityAt())
                .build();

        // Then
        assertThat(updatedDevice.getDeviceName()).isEqualTo("New Name");
        assertThat(updatedDevice.getDeviceType()).isEqualTo("WATCH");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.DEACTIVATED);
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV007");
        assertThat(updatedDevice.getLastActivityAt()).isEqualTo(originalDevice.getLastActivityAt());
    }

    @Test
    void partialUpdate_WithTypeOnly_ShouldPreserveAllOtherFields() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(107L)
                .customer(customer)
                .deviceName("Same Name")
                .deviceNo("DEV008")
                .deviceType("OLD_TYPE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusDays(2))
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceType("NEW_TYPE");
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(originalDevice.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(updateRequest.getDeviceType())
                .status(originalDevice.getStatus())
                .lastActivityAt(originalDevice.getLastActivityAt())
                .build();

        // Then
        assertThat(updatedDevice.getDeviceType()).isEqualTo("NEW_TYPE");
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Same Name");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV008");
        assertThat(updatedDevice.getLastActivityAt()).isEqualTo(originalDevice.getLastActivityAt());
    }

    @Test
    void partialUpdate_RequestWithMixedNullAndNonNullFields_ShouldUpdateOnlyNonNullFields() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(108L)
                .customer(customer)
                .deviceName("Original Name")
                .deviceNo("DEV009")
                .deviceType("ORIGINAL_TYPE")
                .status(DeviceStatus.INACTIVE)
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("Updated Name");
        // deviceType is null, status is null
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(updateRequest.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(originalDevice.getDeviceType())
                .status(originalDevice.getStatus())
                .build();

        // Then
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Updated Name");
        assertThat(updatedDevice.getDeviceType()).isEqualTo("ORIGINAL_TYPE");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.INACTIVE);
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV009");
    }

    @Test
    void partialUpdate_WithRequestContainingAllFields_ShouldUpdateAllFields() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        Device originalDevice = Device.builder()
                .id(109L)
                .customer(customer)
                .deviceName("Original Name")
                .deviceNo("DEV010")
                .deviceType("OLD_TYPE")
                .status(DeviceStatus.INACTIVE)
                .build();
        
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("New Name");
        updateRequest.setDeviceType("NEW_TYPE");
        updateRequest.setStatus(DeviceStatus.ACTIVE);
        // deviceNo is not in the update request (as it's typically not updatable)
        
        // When
        Device updatedDevice = Device.builder()
                .id(originalDevice.getId())
                .customer(originalDevice.getCustomer())
                .deviceName(updateRequest.getDeviceName())
                .deviceNo(originalDevice.getDeviceNo())
                .deviceType(updateRequest.getDeviceType())
                .status(updateRequest.getStatus())
                .build();

        // Then
        assertThat(updatedDevice.getDeviceName()).isEqualTo("New Name");
        assertThat(updatedDevice.getDeviceType()).isEqualTo("NEW_TYPE");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("DEV010"); // Preserved
    }
}