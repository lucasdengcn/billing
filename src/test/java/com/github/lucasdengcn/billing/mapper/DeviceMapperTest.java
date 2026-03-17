package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.model.request.CustomerInfo;
import com.github.lucasdengcn.billing.model.request.DeviceRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceUpdateRequest;
import com.github.lucasdengcn.billing.model.response.DeviceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeviceMapperTest {

    @Autowired
    private DeviceMapper deviceMapper;

    private Customer testCustomer;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();

        // Create test device
        testDevice = Device.builder()
                .id(100L)
                .customer(testCustomer)
                .deviceName("Test Device")
                .deviceNo("DEV001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void toEntity_FromDeviceRegisterRequest_ShouldMapCorrectly() {
        // Given
        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setDeviceName("iPhone 15 Pro");
        request.setDeviceNo("HW-12345678");
        request.setDeviceType("MOBILE");
        request.setStatus(DeviceStatus.INACTIVE);

        // When
        Device device = deviceMapper.toEntity(request);

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getId()).isNull();
        assertThat(device.getDeviceName()).isEqualTo("iPhone 15 Pro");
        assertThat(device.getDeviceNo()).isEqualTo("HW-12345678");
        assertThat(device.getDeviceType()).isEqualTo("MOBILE");
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE); // Default value
        
        // Verify ignored fields are null
        assertThat(device.getCustomer()).isNull();
        assertThat(device.getLastActivityAt()).isNull();
        assertThat(device.getCreatedAt()).isNull();
        assertThat(device.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_FromDeviceRegisterRequestWithNullValues_ShouldHandleNulls() {
        // Given
        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setDeviceNo("HW-12345678"); // Only required field
        request.setStatus(DeviceStatus.INACTIVE); // Default value
        // When
        Device device = deviceMapper.toEntity(request);

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getDeviceNo()).isEqualTo("HW-12345678");
        assertThat(device.getDeviceName()).isNull();
        assertThat(device.getDeviceType()).isNull();
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE); // Default value
    }

    @Test
    void toEntity_FromDeviceUpdateRequest_ShouldMapCorrectly() {
        // Given
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Updated Device Name");
        request.setDeviceNo("HW-UPDATED-123");
        request.setDeviceType("TABLET");
        request.setStatus(DeviceStatus.INACTIVE);

        // When
        Device device = deviceMapper.toEntity(request);

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getId()).isNull();
        assertThat(device.getDeviceName()).isEqualTo("Updated Device Name");
        assertThat(device.getDeviceNo()).isEqualTo("HW-UPDATED-123");
        assertThat(device.getDeviceType()).isEqualTo("TABLET");
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE);
        
        // Verify ignored fields are null
        assertThat(device.getCustomer()).isNull();
        assertThat(device.getLastActivityAt()).isNull();
        assertThat(device.getCreatedAt()).isNull();
        assertThat(device.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_FromDeviceUpdateRequestWithNullValues_ShouldHandleNulls() {
        // Given
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceNo("HW-REQUIRED"); // Only required field

        // When
        Device device = deviceMapper.toEntity(request);

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getDeviceNo()).isEqualTo("HW-REQUIRED");
        assertThat(device.getDeviceName()).isNull();
        assertThat(device.getDeviceType()).isNull();
        assertThat(device.getStatus()).isNull(); // No default for update
    }

    @Test
    void toResponse_FromDevice_ShouldMapCorrectly() {
        // When
        DeviceResponse response = deviceMapper.toResponse(testDevice);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getDeviceName()).isEqualTo("Test Device");
        assertThat(response.getDeviceNo()).isEqualTo("DEV001");
        assertThat(response.getDeviceType()).isEqualTo("MOBILE");
        assertThat(response.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(response.getLastActivityAt()).isEqualTo(testDevice.getLastActivityAt());
    }

    @Test
    void toResponse_FromDeviceWithNullValues_ShouldHandleNulls() {
        // Given
        Device deviceWithNulls = Device.builder()
                .id(200L)
                .deviceNo("HW-NULLS")
                .status(DeviceStatus.ACTIVE)
                .build();

        // When
        DeviceResponse response = deviceMapper.toResponse(deviceWithNulls);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getDeviceNo()).isEqualTo("HW-NULLS");
        assertThat(response.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(response.getDeviceName()).isNull();
        assertThat(response.getDeviceType()).isNull();
        assertThat(response.getLastActivityAt()).isNull();
    }

    @Test
    void updateEntity_ShouldUpdateOnlyAllowedFields() {
        // Given
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Updated Name");
        request.setDeviceNo("UPDATED-123");
        request.setDeviceType("UPDATED-TYPE");
        request.setStatus(DeviceStatus.SUSPENDED);

        Device existingDevice = Device.builder()
                .id(300L)
                .customer(testCustomer)
                .deviceName("Original Name")
                .deviceNo("ORIGINAL-123")
                .deviceType("ORIGINAL-TYPE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusDays(5))
                .createdAt(OffsetDateTime.now().minusMonths(1))
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .build();

        // When
        deviceMapper.updateEntity(request, existingDevice);

        // Then
        // Verify updated fields
        assertThat(existingDevice.getDeviceName()).isEqualTo("Updated Name");
        assertThat(existingDevice.getDeviceNo()).isEqualTo("UPDATED-123");
        assertThat(existingDevice.getDeviceType()).isEqualTo("UPDATED-TYPE");
        assertThat(existingDevice.getStatus()).isEqualTo(DeviceStatus.SUSPENDED);

        // Verify ignored fields remain unchanged
        assertThat(existingDevice.getId()).isEqualTo(300L);
        assertThat(existingDevice.getCustomer()).isEqualTo(testCustomer);
        assertThat(existingDevice.getLastActivityAt()).isNotNull();
        assertThat(existingDevice.getCreatedAt()).isNotNull();
        assertThat(existingDevice.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateEntity_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Given
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceNo("PARTIAL-UPDATE");
        request.setStatus(DeviceStatus.INACTIVE);
        request.setDeviceName("Original Name");
        request.setDeviceType("ORIGINAL-TYPE");
        // deviceName and deviceType are null

        Device existingDevice = Device.builder()
                .id(400L)
                .deviceName("Original Name")
                .deviceNo("ORIGINAL")
                .deviceType("ORIGINAL-TYPE")
                .status(DeviceStatus.ACTIVE)
                .build();

        // When
        deviceMapper.updateEntity(request, existingDevice);

        // Then
        // Verify updated fields
        assertThat(existingDevice.getDeviceNo()).isEqualTo("PARTIAL-UPDATE");
        assertThat(existingDevice.getStatus()).isEqualTo(DeviceStatus.INACTIVE);

        // Verify unchanged fields
        assertThat(existingDevice.getDeviceName()).isEqualTo("Original Name");
        assertThat(existingDevice.getDeviceType()).isEqualTo("ORIGINAL-TYPE");
    }

    @Test
    void updateEntity_WithNullRequest_ShouldNotThrowException() {
        // Given
        Device existingDevice = Device.builder()
                .id(500L)
                .deviceNo("EXISTING")
                .status(DeviceStatus.ACTIVE)
                .build();

        // When & Then - Should not throw exception
        deviceMapper.updateEntity(null, existingDevice);

        // Verify device remains unchanged
        assertThat(existingDevice.getDeviceNo()).isEqualTo("EXISTING");
        assertThat(existingDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
    }

    @Test
    void toResponse_WithNullDevice_ShouldReturnNull() {
        // When
        DeviceResponse response = deviceMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    void toEntity_WithNullRequest_ShouldReturnNull() {
        // When
        Device deviceFromRegister = deviceMapper.toEntity((DeviceRegisterRequest) null);
        Device deviceFromUpdate = deviceMapper.toEntity((DeviceUpdateRequest) null);

        // Then
        assertThat(deviceFromRegister).isNull();
        assertThat(deviceFromUpdate).isNull();
    }
}