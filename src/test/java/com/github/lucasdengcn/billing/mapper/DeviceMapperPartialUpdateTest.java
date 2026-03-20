package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.model.request.DeviceUpdateRequest;
import com.github.lucasdengcn.billing.model.response.DeviceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeviceMapperPartialUpdateTest {

    @Autowired
    private DeviceMapper deviceMapper;

    private Device testDevice;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();

        testDevice = Device.builder()
                .id(100L)
                .customer(testCustomer)
                .deviceName("Original Device")
                .deviceNo("DEV001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusDays(1))
                .createdAt(OffsetDateTime.now().minusDays(2))
                .updatedAt(OffsetDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void updateEntity_WithNullDeviceName_ShouldNotUpdateDeviceName() {
        // Given
        Device originalDevice = testDevice;
        String originalName = originalDevice.getDeviceName();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName(null); // Null name
        request.setDeviceType("LAPTOP");
        request.setStatus(DeviceStatus.INACTIVE);
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo(originalName); // Unchanged
        assertThat(originalDevice.getDeviceType()).isEqualTo("LAPTOP"); // Updated
        assertThat(originalDevice.getStatus()).isEqualTo(DeviceStatus.INACTIVE); // Updated
    }

    @Test
    void updateEntity_WithNullDeviceType_ShouldNotUpdateDeviceType() {
        // Given
        Device originalDevice = testDevice;
        String originalType = originalDevice.getDeviceType();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Updated Name");
        request.setDeviceType(null); // Null type
        request.setStatus(DeviceStatus.INACTIVE);
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo("Updated Name"); // Updated
        assertThat(originalDevice.getDeviceType()).isEqualTo(originalType); // Unchanged
        assertThat(originalDevice.getStatus()).isEqualTo(DeviceStatus.INACTIVE); // Updated
    }

    @Test
    void updateEntity_WithNullStatus_ShouldNotUpdateStatus() {
        // Given
        Device originalDevice = testDevice;
        DeviceStatus originalStatus = originalDevice.getStatus();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Updated Name");
        request.setDeviceType("TABLET");
        request.setStatus(null); // Null status
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo("Updated Name"); // Updated
        assertThat(originalDevice.getDeviceType()).isEqualTo("TABLET"); // Updated
        assertThat(originalDevice.getStatus()).isEqualTo(originalStatus); // Unchanged
    }

    @Test
    void updateEntity_WithPartiallyFilledRequest_ShouldUpdateOnlyNonNullFields() {
        // Given
        Device originalDevice = testDevice;
        String originalType = originalDevice.getDeviceType();
        DeviceStatus originalStatus = originalDevice.getStatus();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Partially Updated Name");
        // deviceType and status are null
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo("Partially Updated Name"); // Updated
        assertThat(originalDevice.getDeviceType()).isEqualTo(originalType); // Unchanged
        assertThat(originalDevice.getStatus()).isEqualTo(originalStatus); // Unchanged
    }

    @Test
    void updateEntity_WithAllNullFields_ShouldNotUpdateAnyFields() {
        // Given
        Device originalDevice = testDevice;
        String originalName = originalDevice.getDeviceName();
        String originalType = originalDevice.getDeviceType();
        DeviceStatus originalStatus = originalDevice.getStatus();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName(null);
        request.setDeviceType(null);
        request.setStatus(null);
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo(originalName); // Unchanged
        assertThat(originalDevice.getDeviceType()).isEqualTo(originalType); // Unchanged
        assertThat(originalDevice.getStatus()).isEqualTo(originalStatus); // Unchanged
    }

    @Test
    void updateEntity_WithAllNonNullFields_ShouldUpdateAllFields() {
        // Given
        Device originalDevice = testDevice;
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Fully Updated Name");
        request.setDeviceType("SMART_WATCH");
        request.setStatus(DeviceStatus.SUSPENDED);
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo("Fully Updated Name");
        assertThat(originalDevice.getDeviceType()).isEqualTo("SMART_WATCH");
        assertThat(originalDevice.getStatus()).isEqualTo(DeviceStatus.SUSPENDED);
    }

    @Test
    void updateEntity_WithIgnoredFields_ShouldNotUpdateIgnoredFields() {
        // Given
        Device originalDevice = testDevice;
        Long originalId = originalDevice.getId();
        Customer originalCustomer = originalDevice.getCustomer();
        OffsetDateTime originalCreatedAt = originalDevice.getCreatedAt();
        OffsetDateTime originalUpdatedAt = originalDevice.getUpdatedAt();
        OffsetDateTime originalLastActivityAt = originalDevice.getLastActivityAt();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Name Update Only");
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        // Ignored fields should remain unchanged
        assertThat(originalDevice.getId()).isEqualTo(originalId);
        assertThat(originalDevice.getCustomer()).isEqualTo(originalCustomer);
        assertThat(originalDevice.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(originalDevice.getUpdatedAt()).isEqualTo(originalUpdatedAt);
        assertThat(originalDevice.getLastActivityAt()).isEqualTo(originalLastActivityAt);
        
        // Only mapped fields should be updated
        assertThat(originalDevice.getDeviceName()).isEqualTo("Name Update Only");
    }

    @Test
    void updateEntity_WithOnlyStatusField_ShouldUpdateOnlyStatus() {
        // Given
        Device originalDevice = testDevice;
        String originalName = originalDevice.getDeviceName();
        String originalType = originalDevice.getDeviceType();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setStatus(DeviceStatus.DEACTIVATED);
        // Other fields are null
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getStatus()).isEqualTo(DeviceStatus.DEACTIVATED); // Updated
        assertThat(originalDevice.getDeviceName()).isEqualTo(originalName); // Unchanged
        assertThat(originalDevice.getDeviceType()).isEqualTo(originalType); // Unchanged
    }

    @Test
    void updateEntity_WithOnlyDeviceTypeField_ShouldUpdateOnlyDeviceType() {
        // Given
        Device originalDevice = testDevice;
        String originalName = originalDevice.getDeviceName();
        DeviceStatus originalStatus = originalDevice.getStatus();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceType("DESKTOP");
        // Other fields are null
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceType()).isEqualTo("DESKTOP"); // Updated
        assertThat(originalDevice.getDeviceName()).isEqualTo(originalName); // Unchanged
        assertThat(originalDevice.getStatus()).isEqualTo(originalStatus); // Unchanged
    }

    @Test
    void updateEntity_WithOnlyDeviceNameField_ShouldUpdateOnlyDeviceName() {
        // Given
        Device originalDevice = testDevice;
        String originalType = originalDevice.getDeviceType();
        DeviceStatus originalStatus = originalDevice.getStatus();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("New Device Name");
        // Other fields are null
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo("New Device Name"); // Updated
        assertThat(originalDevice.getDeviceType()).isEqualTo(originalType); // Unchanged
        assertThat(originalDevice.getStatus()).isEqualTo(originalStatus); // Unchanged
    }

    @Test
    void updateEntity_WithMixedNullAndNonNullFields_ShouldUpdateOnlyNonNullFields() {
        // Given
        Device originalDevice = testDevice;
        DeviceStatus originalStatus = originalDevice.getStatus();
        
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Mixed Update Name");
        request.setDeviceType(null); // Null
        request.setStatus(DeviceStatus.ACTIVE); // Non-null
        
        // When
        deviceMapper.updateEntity(request, originalDevice);

        // Then
        assertThat(originalDevice.getDeviceName()).isEqualTo("Mixed Update Name"); // Updated
        assertThat(originalDevice.getDeviceType()).isEqualTo("MOBILE"); // Unchanged
        assertThat(originalDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE); // Updated
    }

    @Test
    void toResponse_WithDeviceEntity_ShouldMapCorrectly() {
        // Given
        Device device = testDevice;
        
        // When
        DeviceResponse response = deviceMapper.toResponse(device);

        // Then
        assertThat(response.getId()).isEqualTo(device.getId());
        assertThat(response.getDeviceName()).isEqualTo(device.getDeviceName());
        assertThat(response.getDeviceNo()).isEqualTo(device.getDeviceNo());
        assertThat(response.getDeviceType()).isEqualTo(device.getDeviceType());
        assertThat(response.getStatus()).isEqualTo(device.getStatus());
        assertThat(response.getCreatedAt()).isEqualTo(device.getCreatedAt());
    }

    @Test
    void toEntity_WithDeviceUpdateRequest_ShouldCreateNewEntityWithNullHandling() {
        // Given
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("New Request Device");
        request.setDeviceType(null); // Null
        request.setStatus(DeviceStatus.ACTIVE);
        
        // When
        Device entity = deviceMapper.toEntity(request);

        // Then
        assertThat(entity.getDeviceName()).isEqualTo("New Request Device");
        assertThat(entity.getDeviceType()).isNull();
        assertThat(entity.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        // Ignored fields should be null
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCustomer()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
        assertThat(entity.getLastActivityAt()).isNull();
    }
}