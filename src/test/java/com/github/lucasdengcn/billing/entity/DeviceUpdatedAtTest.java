package com.github.lucasdengcn.billing.entity;

import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:device-updatedat-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class DeviceUpdatedAtTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DeviceRepository deviceRepository; // Assuming repository exists

    @Test
    void updatedAt_WhenDeviceIsCreated_ShouldBeNullInitially() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("Test Device")
                .deviceNo("DEV001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .build();
        
        OffsetDateTime beforeSave = OffsetDateTime.now();
        //
        assertThat(device.getUpdatedAt()).isNull();

        // When
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime afterSave = OffsetDateTime.now();

        // Then
        assertThat(savedDevice.getUpdatedAt()).isNotNull();
        assertThat(savedDevice.getUpdatedAt()).isBetween(beforeSave.minusSeconds(1), afterSave.plusSeconds(1));
    }

    @Test
    void updatedAt_WhenDeviceIsUpdated_ShouldReflectCurrentTimestamp() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("Original Device")
                .deviceNo("DEV002")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .build();
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime originalUpdatedAt = savedDevice.getUpdatedAt();
        
        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        OffsetDateTime updateTime = OffsetDateTime.now();
        
        // When - Update the device
        savedDevice.setDeviceName("Updated Device Name");
        Device updatedDevice = entityManager.merge(savedDevice);
        entityManager.flush();

        // Then
        assertThat(updatedDevice.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedDevice.getUpdatedAt()).isAfter(updateTime.minusSeconds(1));
    }

    @Test
    void updatedAt_WhenDeviceStatusIsChanged_ShouldUpdateTimestamp() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("Status Change Test")
                .deviceNo("DEV003")
                .deviceType("DESKTOP")
                .status(DeviceStatus.INACTIVE)
                .build();
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime originalUpdatedAt = savedDevice.getUpdatedAt();
        
        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - Change status
        savedDevice.setStatus(DeviceStatus.ACTIVE);
        Device updatedDevice = entityManager.merge(savedDevice);
        entityManager.flush();

        // Then
        assertThat(updatedDevice.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void updatedAt_WhenDeviceTypeIsChanged_ShouldUpdateTimestamp() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("Type Change Test")
                .deviceNo("DEV004")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .build();
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime originalUpdatedAt = savedDevice.getUpdatedAt();
        
        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - Change device type
        savedDevice.setDeviceType("TABLET");
        Device updatedDevice = entityManager.merge(savedDevice);
        entityManager.flush();

        // Then
        assertThat(updatedDevice.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void updatedAt_WhenLastActivityIsUpdated_ShouldUpdateTimestamp() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("Activity Test")
                .deviceNo("DEV005")
                .deviceType("LAPTOP")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusHours(1))
                .build();
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime originalUpdatedAt = savedDevice.getUpdatedAt();
        
        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - Update last activity
        OffsetDateTime newActivityTime = OffsetDateTime.now();
        savedDevice.setLastActivityAt(newActivityTime);
        Device updatedDevice = entityManager.merge(savedDevice);
        entityManager.flush();

        // Then
        assertThat(updatedDevice.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedDevice.getLastActivityAt()).isEqualTo(newActivityTime);
    }

    @Test
    void updatedAt_WhenNoChangesAreMade_ShouldNotUpdateTimestamp() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("No Change Test")
                .deviceNo("DEV006")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .build();
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime originalUpdatedAt = savedDevice.getUpdatedAt();
        
        // Wait a moment
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - Flush without making changes
        entityManager.flush();

        // Then - The entity should still have the same updatedAt
        Device reloadedDevice = entityManager.find(Device.class, savedDevice.getId());
        assertThat(reloadedDevice.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updatedAt_WithMultipleSequentialUpdates_ShouldContinueToUpdate() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("Sequential Update Test")
                .deviceNo("DEV007")
                .deviceType("SMART_TV")
                .status(DeviceStatus.INACTIVE)
                .build();
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime firstUpdate = savedDevice.getUpdatedAt();
        
        // First update
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        savedDevice.setDeviceName("First Update");
        Device updatedOnce = entityManager.merge(savedDevice);
        entityManager.flush();
        
        OffsetDateTime secondUpdate = updatedOnce.getUpdatedAt();
        
        // Second update
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        updatedOnce.setDeviceType("GAME_CONSOLE");
        Device updatedTwice = entityManager.merge(updatedOnce);
        entityManager.flush();
        
        OffsetDateTime thirdUpdate = updatedTwice.getUpdatedAt();

        // Then
        assertThat(secondUpdate).isAfter(firstUpdate);
        assertThat(thirdUpdate).isAfter(secondUpdate);
    }

    @Test
    void updatedAt_WhenUsingBuilder_ShouldInitializeToNull() {
        // When
        Device device = Device.builder()
                .deviceName("Builder Test")
                .deviceNo("DEV008")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .build();

        // Then
        assertThat(device.getUpdatedAt()).isNull();
    }

    @Test
    void updatedAt_WhenCreatingNewInstance_ShouldInitializeToNull() {
        // When
        Device device = new Device();

        // Then
        assertThat(device.getUpdatedAt()).isNull();
    }

    @Test
    void updatedAt_WithDifferentHibernateOperations_ShouldUpdateCorrectly() {
        // Given
        Customer customer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        customer = entityManager.persistAndFlush(customer);
        
        Device device = Device.builder()
                .customer(customer)
                .deviceName("Hibernate Op Test")
                .deviceNo("DEV009")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .build();
        Device savedDevice = entityManager.persistAndFlush(device);
        
        OffsetDateTime beforeUpdate = savedDevice.getUpdatedAt();
        
        // Wait a moment
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        
        // When - Use save operation
        savedDevice.setDeviceName("Hibernate Save Test");
        Device savedAgain = entityManager.getEntityManager().merge(savedDevice);
        entityManager.flush();

        // Then
        assertThat(savedAgain.getUpdatedAt()).isAfter(beforeUpdate);
    }
}