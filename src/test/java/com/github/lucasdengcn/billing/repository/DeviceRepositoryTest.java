package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer testCustomer;
    private Device testDevice1;
    private Device testDevice2;
    private Device testDevice3;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        deviceRepository.deleteAll();
        
        // Create test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST001")
                .mobileNo("1234567890")
                .build();
        
        // Create test devices
        testDevice1 = Device.builder()
                .customer(testCustomer)
                .deviceName("Test Device 1")
                .deviceNo("DEV001")
                .deviceType("Type A")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();

        testDevice2 = Device.builder()
                .customer(testCustomer)
                .deviceName("Test Device 2")
                .deviceNo("DEV002")
                .deviceType("Type B")
                .status(DeviceStatus.DEACTIVATED)
                .lastActivityAt(OffsetDateTime.now().minusDays(1))
                .build();

        testDevice3 = Device.builder()
                .customer(testCustomer)
                .deviceName("Test Device 3")
                .deviceNo("DEV003")
                .deviceType("Type A")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusHours(2))
                .build();
    }

    @Test
    void findByDeviceNo_WhenDeviceExists_ShouldReturnDevice() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        // When
        Optional<Device> foundDevice = deviceRepository.findByDeviceNo("DEV001");

        // Then
        assertThat(foundDevice).isPresent();
        assertThat(foundDevice.get().getDeviceNo()).isEqualTo("DEV001");
        assertThat(foundDevice.get().getDeviceName()).isEqualTo("Test Device 1");
        assertThat(foundDevice.get().getStatus()).isEqualTo(DeviceStatus.ACTIVE);
    }

    @Test
    void findByDeviceNo_WhenDeviceNotExists_ShouldReturnEmpty() {
        // When
        Optional<Device> foundDevice = deviceRepository.findByDeviceNo("NONEXISTENT");

        // Then
        assertThat(foundDevice).isEmpty();
    }

    @Test
    void findByCustomer_WhenCustomerHasDevices_ShouldReturnAllDevices() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testDevice3 = entityManager.persistAndFlush(testDevice3);
        // When
        List<Device> devices = deviceRepository.findByCustomer(testCustomer);

        // Then
        assertThat(devices).hasSize(3);
        assertThat(devices).extracting(Device::getDeviceNo)
                .containsExactlyInAnyOrder("DEV001", "DEV002", "DEV003");
    }

    @Test
    void findByCustomer_WhenCustomerHasNoDevices_ShouldReturnEmptyList() {
        // Given
        Customer newCustomer = Customer.builder()
                .name("New Customer")
                .customerNo("CUST002")
                .mobileNo("0987654321")
                .build();
        newCustomer = entityManager.persistAndFlush(newCustomer);

        // When
        List<Device> devices = deviceRepository.findByCustomer(newCustomer);

        // Then
        assertThat(devices).isEmpty();
    }

    @Test
    void findByStatus_WhenDevicesWithStatusExist_ShouldReturnMatchingDevices() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testDevice3 = entityManager.persistAndFlush(testDevice3);
        // When
        List<Device> activeDevices = deviceRepository.findByStatus(DeviceStatus.ACTIVE);

        // Then
        assertThat(activeDevices).hasSize(2);
        assertThat(activeDevices).extracting(Device::getDeviceNo)
                .containsExactlyInAnyOrder("DEV001", "DEV003");
        assertThat(activeDevices).extracting(Device::getStatus)
                .containsOnly(DeviceStatus.ACTIVE);
    }

    @Test
    void findByStatus_WhenNoDevicesWithStatusExist_ShouldReturnEmptyList() {
        // When
        List<Device> suspendedDevices = deviceRepository.findByStatus(DeviceStatus.SUSPENDED);

        // Then
        assertThat(suspendedDevices).isEmpty();
    }

    @Test
    void save_ShouldPersistDeviceCorrectly() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        Device newDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("New Device")
                .deviceNo("DEV004")
                .deviceType("Type C")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();

        // When
        Device savedDevice = deviceRepository.save(newDevice);

        // Then
        assertThat(savedDevice.getId()).isNotNull();
        assertThat(savedDevice.getDeviceNo()).isEqualTo("DEV004");
        assertThat(savedDevice.getCreatedAt()).isNotNull();
        assertThat(savedDevice.getUpdatedAt()).isNotNull();

        // Verify it can be retrieved
        Optional<Device> retrievedDevice = deviceRepository.findByDeviceNo("DEV004");
        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().getDeviceName()).isEqualTo("New Device");
    }

    @Test
    void findAll_ShouldReturnAllDevices() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testDevice3 = entityManager.persistAndFlush(testDevice3);
        // When
        List<Device> allDevices = deviceRepository.findAll();

        // Then
        assertThat(allDevices).hasSize(3);
        assertThat(allDevices).extracting(Device::getDeviceNo)
                .containsExactlyInAnyOrder("DEV001", "DEV002", "DEV003");
    }

    @Test
    void findById_WhenDeviceExists_ShouldReturnDevice() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        // When
        Optional<Device> foundDevice = deviceRepository.findById(testDevice1.getId());

        // Then
        assertThat(foundDevice).isPresent();
        assertThat(foundDevice.get().getId()).isEqualTo(testDevice1.getId());
        assertThat(foundDevice.get().getDeviceNo()).isEqualTo("DEV001");
    }

    @Test
    void findById_WhenDeviceNotExists_ShouldReturnEmpty() {
        // When
        Optional<Device> foundDevice = deviceRepository.findById(999L);

        // Then
        assertThat(foundDevice).isEmpty();
    }

    @Test
    void delete_ShouldRemoveDevice() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        testDevice2 = entityManager.persistAndFlush(testDevice2);
        testDevice3 = entityManager.persistAndFlush(testDevice3);
        // When
        deviceRepository.delete(testDevice1);
        entityManager.flush();

        // Then
        Optional<Device> deletedDevice = deviceRepository.findByDeviceNo("DEV001");
        assertThat(deletedDevice).isEmpty();

        List<Device> remainingDevices = deviceRepository.findAll();
        assertThat(remainingDevices).hasSize(2);
    }

    @Test
    void update_ShouldModifyDeviceProperties() {
        // Given
        testCustomer = entityManager.persistAndFlush(testCustomer);
        testDevice1 = entityManager.persistAndFlush(testDevice1);
        // Given
        testDevice1.setDeviceName("Updated Device Name");
        testDevice1.setStatus(DeviceStatus.DEACTIVATED);

        // When
        Device updatedDevice = deviceRepository.save(testDevice1);

        // Then
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Updated Device Name");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.DEACTIVATED);
        assertThat(updatedDevice.getUpdatedAt()).isAfter(testDevice1.getCreatedAt());

        // Verify the update is persisted
        Optional<Device> retrievedDevice = deviceRepository.findByDeviceNo("DEV001");
        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().getDeviceName()).isEqualTo("Updated Device Name");
    }
}