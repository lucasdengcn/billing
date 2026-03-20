package com.github.lucasdengcn.billing.entity;

import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceBuilderTest {

    @Test
    void builder_WithAllFields_ShouldCreateDeviceCorrectly() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        OffsetDateTime now = OffsetDateTime.now();
        
        // When
        Device device = Device.builder()
                .id(100L)
                .customer(customer)
                .deviceName("Test Device")
                .deviceNo("DEV001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(now)
                .build();

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getId()).isEqualTo(100L);
        assertThat(device.getCustomer()).isEqualTo(customer);
        assertThat(device.getDeviceName()).isEqualTo("Test Device");
        assertThat(device.getDeviceNo()).isEqualTo("DEV001");
        assertThat(device.getDeviceType()).isEqualTo("MOBILE");
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(device.getLastActivityAt()).isEqualTo(now);
        assertThat(device.getCreatedAt()).isNull(); // Not set by builder
        assertThat(device.getUpdatedAt()).isNull(); // Not set by builder
    }

    @Test
    void builder_WithMinimalFields_ShouldCreateDeviceWithDefaults() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        // When
        Device device = Device.builder()
                .customer(customer)
                .deviceNo("DEV002")
                .build();

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getId()).isNull(); // Not set
        assertThat(device.getCustomer()).isEqualTo(customer);
        assertThat(device.getDeviceName()).isNull(); // Not set
        assertThat(device.getDeviceNo()).isEqualTo("DEV002");
        assertThat(device.getDeviceType()).isNull(); // Not set
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE); // Default value from @Builder.Default
        assertThat(device.getLastActivityAt()).isNull(); // Not set
        assertThat(device.getCreatedAt()).isNull(); // Not set by builder
        assertThat(device.getUpdatedAt()).isNull(); // Not set by builder
    }

    @Test
    void builder_WithNoFields_ShouldCreateDeviceWithNullsAndDefaults() {
        // When
        Device device = Device.builder().build();

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getId()).isNull();
        assertThat(device.getCustomer()).isNull();
        assertThat(device.getDeviceName()).isNull();
        assertThat(device.getDeviceNo()).isNull();
        assertThat(device.getDeviceType()).isNull();
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE); // Default value from @Builder.Default
        assertThat(device.getLastActivityAt()).isNull();
        assertThat(device.getCreatedAt()).isNull();
        assertThat(device.getUpdatedAt()).isNull();
    }

    @Test
    void builder_WithSpecificStatus_ShouldRespectProvidedStatus() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        // When
        Device device = Device.builder()
                .customer(customer)
                .deviceNo("DEV003")
                .status(DeviceStatus.SUSPENDED)
                .build();

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getCustomer()).isEqualTo(customer);
        assertThat(device.getDeviceNo()).isEqualTo("DEV003");
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.SUSPENDED); // Provided value overrides default
    }

    @Test
    void builder_WithDifferentDeviceTypes_ShouldSetCorrectly() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        // When
        Device mobileDevice = Device.builder()
                .customer(customer)
                .deviceNo("DEV-MOBILE")
                .deviceType("MOBILE")
                .build();
        
        Device desktopDevice = Device.builder()
                .customer(customer)
                .deviceNo("DEV-DESKTOP")
                .deviceType("DESKTOP")
                .build();
        
        Device tabletDevice = Device.builder()
                .customer(customer)
                .deviceNo("DEV-TABLET")
                .deviceType("TABLET")
                .build();

        // Then
        assertThat(mobileDevice.getDeviceType()).isEqualTo("MOBILE");
        assertThat(desktopDevice.getDeviceType()).isEqualTo("DESKTOP");
        assertThat(tabletDevice.getDeviceType()).isEqualTo("TABLET");
    }

    @Test
    void builder_WithLastActivity_ShouldPreserveTimestamp() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        OffsetDateTime activityTime = OffsetDateTime.now().minusHours(2);
        
        // When
        Device device = Device.builder()
                .customer(customer)
                .deviceNo("DEV004")
                .lastActivityAt(activityTime)
                .build();

        // Then
        assertThat(device.getLastActivityAt()).isEqualTo(activityTime);
    }

    @Test
    void builder_WithSameCustomer_ShouldWorkCorrectly() {
        // Given
        Customer sharedCustomer = Customer.builder()
                .id(5L)
                .name("Shared Customer")
                .customerNo("CUST005")
                .mobileNo("0987654321")
                .build();
        
        // When
        Device device1 = Device.builder()
                .customer(sharedCustomer)
                .deviceNo("DEV005-A")
                .deviceName("Device A")
                .build();
        
        Device device2 = Device.builder()
                .customer(sharedCustomer)
                .deviceNo("DEV005-B")
                .deviceName("Device B")
                .build();

        // Then
        assertThat(device1.getCustomer()).isEqualTo(sharedCustomer);
        assertThat(device2.getCustomer()).isEqualTo(sharedCustomer);
        assertThat(device1.getCustomer()).isSameAs(device2.getCustomer()); // Same reference
        assertThat(device1.getDeviceNo()).isEqualTo("DEV005-A");
        assertThat(device2.getDeviceNo()).isEqualTo("DEV005-B");
    }

    @Test
    void builder_DefaultStatusShouldBeInactive() {
        // When
        Device device = Device.builder()
                .build();

        // Then
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.INACTIVE);
    }

    @Test
    void builder_CanOverrideDefaultStatus() {
        // When
        Device deviceWithActiveStatus = Device.builder()
                .status(DeviceStatus.ACTIVE)
                .build();
        
        Device deviceWithDeactivatedStatus = Device.builder()
                .status(DeviceStatus.DEACTIVATED)
                .build();

        // Then
        assertThat(deviceWithActiveStatus.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(deviceWithDeactivatedStatus.getStatus()).isEqualTo(DeviceStatus.DEACTIVATED);
    }

    @Test
    void builder_WithChainedCalls_ShouldWorkCorrectly() {
        // Given
        Customer customer = Customer.builder().build();
        
        // When - using chained builder calls
        Device device = Device.builder()
                .id(200L)
                .customer(customer)
                .deviceName("Chained Device")
                .deviceNo("DEV-CHAINED")
                .deviceType("LAPTOP")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();

        // Then
        assertThat(device.getId()).isEqualTo(200L);
        assertThat(device.getDeviceName()).isEqualTo("Chained Device");
        assertThat(device.getDeviceNo()).isEqualTo("DEV-CHAINED");
        assertThat(device.getDeviceType()).isEqualTo("LAPTOP");
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
    }
}