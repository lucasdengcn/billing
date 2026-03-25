package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.mapper.DeviceMapper;
import com.github.lucasdengcn.billing.model.request.CustomerInfo;
import com.github.lucasdengcn.billing.model.request.DeviceBatchRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceUpdateRequest;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private Customer testCustomer;
    private Device testDevice;
    private DeviceRegisterRequest registerRequest;
    private DeviceUpdateRequest updateRequest;

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
                .build();

        // Create test requests
        registerRequest = new DeviceRegisterRequest();
        registerRequest.setDeviceName("New Device");
        registerRequest.setDeviceNo("NEW001");
        registerRequest.setDeviceType("TABLET");

        updateRequest = new DeviceUpdateRequest();
        updateRequest.setDeviceName("Updated Device");
        updateRequest.setDeviceNo("UPD001");
        updateRequest.setDeviceType("DESKTOP");
        updateRequest.setStatus(DeviceStatus.INACTIVE);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        // Given
        when(deviceRepository.save(testDevice)).thenReturn(testDevice);

        // When
        Device savedDevice = deviceService.save(testDevice);

        // Then
        assertThat(savedDevice).isEqualTo(testDevice);
        verify(deviceRepository).save(testDevice);
    }

    @Test
    void registerDevice_WithCustomerId_ShouldResolveCustomerAndSaveDevice() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(1L);
        registerRequest.setCustomer(customerInfo);

        Device mappedDevice = Device.builder()
                .deviceName("New Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceMapper.toEntity(registerRequest)).thenReturn(mappedDevice);
        when(deviceRepository.save(mappedDevice)).thenReturn(testDevice);

        // When
        Device registeredDevice = deviceService.registerDevice(registerRequest);

        // Then
        assertThat(registeredDevice).isEqualTo(testDevice);
        verify(customerService).findById(1L);
        verify(deviceMapper).toEntity(registerRequest);
        verify(deviceRepository).save(mappedDevice);
        assertThat(mappedDevice.getCustomer()).isEqualTo(testCustomer);
    }

    @Test
    void registerDevice_WithCustomerNo_ShouldResolveCustomerAndSaveDevice() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustomerNo("CUST001");
        registerRequest.setCustomer(customerInfo);

        Device mappedDevice = Device.builder()
                .deviceName("New Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .build();

        when(customerService.findByCustomerNo("CUST001")).thenReturn(testCustomer);
        when(deviceMapper.toEntity(registerRequest)).thenReturn(mappedDevice);
        when(deviceRepository.save(mappedDevice)).thenReturn(testDevice);

        // When
        Device registeredDevice = deviceService.registerDevice(registerRequest);

        // Then
        assertThat(registeredDevice).isEqualTo(testDevice);
        verify(customerService).findByCustomerNo("CUST001");
        verify(deviceMapper).toEntity(registerRequest);
        verify(deviceRepository).save(mappedDevice);
    }

    @Test
    void registerDevice_WithNewCustomer_ShouldCreateCustomerAndSaveDevice() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setName("New Customer");
        customerInfo.setCustomerNo("NEWCUST");
        registerRequest.setCustomer(customerInfo);

        Customer newCustomer = Customer.builder()
                .id(2L)
                .name("New Customer")
                .customerNo("NEWCUST")
                .build();

        Device mappedDevice = Device.builder()
                .deviceName("New Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .build();

        when(customerService.findByCustomerNo("NEWCUST")).thenThrow(new ResourceNotFoundException("Not found"));
        when(customerService.save(any(Customer.class))).thenReturn(newCustomer);
        when(deviceMapper.toEntity(registerRequest)).thenReturn(mappedDevice);
        when(deviceRepository.save(mappedDevice)).thenReturn(testDevice);

        // When
        Device registeredDevice = deviceService.registerDevice(registerRequest);

        // Then
        assertThat(registeredDevice).isEqualTo(testDevice);
        verify(customerService).findByCustomerNo("NEWCUST");
        verify(customerService).save(any(Customer.class));
        verify(deviceMapper).toEntity(registerRequest);
        verify(deviceRepository).save(mappedDevice);
    }

    @Test
    void registerDevice_WithoutCustomerInfo_ShouldThrowException() {
        // Given
        registerRequest.setCustomer(null);

        // When & Then
        assertThatThrownBy(() -> deviceService.registerDevice(registerRequest))
                .isInstanceOf(com.github.lucasdengcn.billing.exception.CustomerResolutionException.class)
                .hasMessage("Customer information is required to register a device");
    }

    @Test
    void registerDevices_ShouldRegisterMultipleDevicesForCustomer() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(1L);
        
        DeviceBatchRegisterRequest batchRequest = new DeviceBatchRegisterRequest();
        batchRequest.setCustomer(customerInfo);
        List<DeviceUpdateRequest> devices = new ArrayList<>(List.of(updateRequest, updateRequest));
        batchRequest.setDevices(devices);

        Device mappedDevice = Device.builder()
                .deviceName("New Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceMapper.toEntity(updateRequest)).thenReturn(mappedDevice);
        when(deviceRepository.saveAll(any(List.class))).thenReturn(List.of(testDevice, testDevice));

        // When
        List<Device> registeredDevices = deviceService.registerDevices(batchRequest);

        // Then
        assertThat(registeredDevices).hasSize(2);
        verify(customerService).findById(1L);
        verify(deviceMapper, times(2)).toEntity(updateRequest);
        verify(deviceRepository).saveAll(any(List.class));
    }

    @Test
    void updateDevice_ShouldUpdateExistingDevice() {
        // Given
        Device existingDevice = Device.builder()
                .id(100L)
                .deviceName("Old Name")
                .deviceNo("OLD001")
                .deviceType("OLD_TYPE")
                .status(DeviceStatus.ACTIVE)
                .build();

        when(deviceRepository.findById(100L)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.save(existingDevice)).thenReturn(testDevice);

        // When
        Device updatedDevice = deviceService.updateDevice(100L, updateRequest);

        // Then
        assertThat(updatedDevice).isEqualTo(testDevice);
        verify(deviceRepository).findById(100L);
        verify(deviceMapper).updateEntity(updateRequest, existingDevice);
        verify(deviceRepository).save(existingDevice);
    }

    @Test
    void updateDevice_WhenDeviceNotFound_ShouldThrowException() {
        // Given
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.updateDevice(999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with id: 999");
    }

    @Test
    void activateDeviceByNo_ShouldActivateDevice() {
        // Given
        Device inactiveDevice = Device.builder()
                .id(100L)
                .deviceNo("DEV001")
                .status(DeviceStatus.INACTIVE)
                .build();

        when(deviceRepository.findByDeviceNo("DEV001")).thenReturn(Optional.of(inactiveDevice));
        when(deviceRepository.save(inactiveDevice)).thenReturn(testDevice);

        // When
        Device activatedDevice = deviceService.activateDeviceByNo("DEV001");

        // Then
        assertThat(activatedDevice).isEqualTo(testDevice);
        assertThat(inactiveDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        verify(deviceRepository).findByDeviceNo("DEV001");
        verify(deviceRepository).save(inactiveDevice);
    }

    @Test
    void activateDeviceByNo_WhenDeviceNotFound_ShouldThrowException() {
        // Given
        when(deviceRepository.findByDeviceNo("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.activateDeviceByNo("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with deviceNo: NONEXISTENT");
    }

    @Test
    void deactivateDeviceByNo_ShouldDeactivateDevice() {
        // Given
        Device activeDevice = Device.builder()
                .id(100L)
                .deviceNo("DEV001")
                .status(DeviceStatus.ACTIVE)
                .build();

        when(deviceRepository.findByDeviceNo("DEV001")).thenReturn(Optional.of(activeDevice));
        when(deviceRepository.save(activeDevice)).thenReturn(testDevice);

        // When
        Device deactivatedDevice = deviceService.deactivateDeviceByNo("DEV001");

        // Then
        assertThat(deactivatedDevice).isEqualTo(testDevice);
        assertThat(activeDevice.getStatus()).isEqualTo(DeviceStatus.DEACTIVATED);
        verify(deviceRepository).findByDeviceNo("DEV001");
        verify(deviceRepository).save(activeDevice);
    }

    @Test
    void deactivateDeviceByNo_WhenDeviceNotFound_ShouldThrowException() {
        // Given
        when(deviceRepository.findByDeviceNo("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.deactivateDeviceByNo("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with deviceNo: NONEXISTENT");
    }

    @Test
    void findById_ShouldReturnDevice() {
        // Given
        when(deviceRepository.findById(100L)).thenReturn(Optional.of(testDevice));

        // When
        Device foundDevice = deviceService.findById(100L);

        // Then
        assertThat(foundDevice).isEqualTo(testDevice);
        verify(deviceRepository).findById(100L);
    }

    @Test
    void findById_WhenDeviceNotFound_ShouldThrowException() {
        // Given
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with id: 999");
    }

    @Test
    void findByDeviceNo_ShouldReturnDevice() {
        // Given
        when(deviceRepository.findByDeviceNo("DEV001")).thenReturn(Optional.of(testDevice));

        // When
        Device foundDevice = deviceService.findByDeviceNo("DEV001");

        // Then
        assertThat(foundDevice).isEqualTo(testDevice);
        verify(deviceRepository).findByDeviceNo("DEV001");
    }

    @Test
    void findByDeviceNo_WhenDeviceNotFound_ShouldThrowException() {
        // Given
        when(deviceRepository.findByDeviceNo("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.findByDeviceNo("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with deviceNo: NONEXISTENT");
    }

    @Test
    void findByCustomer_ShouldReturnDevices() {
        // Given
        List<Device> devices = List.of(testDevice);
        when(deviceRepository.findByCustomer(testCustomer)).thenReturn(devices);

        // When
        List<Device> foundDevices = deviceService.findByCustomer(testCustomer);

        // Then
        assertThat(foundDevices).isEqualTo(devices);
        verify(deviceRepository).findByCustomer(testCustomer);
    }

    @Test
    void findByStatus_ShouldReturnDevices() {
        // Given
        List<Device> devices = List.of(testDevice);
        when(deviceRepository.findByStatus(DeviceStatus.ACTIVE)).thenReturn(devices);

        // When
        List<Device> foundDevices = deviceService.findByStatus(DeviceStatus.ACTIVE);

        // Then
        assertThat(foundDevices).isEqualTo(devices);
        verify(deviceRepository).findByStatus(DeviceStatus.ACTIVE);
    }

    @Test
    void findAll_ShouldReturnAllDevices() {
        // Given
        List<Device> devices = List.of(testDevice);
        when(deviceRepository.findAll()).thenReturn(devices);

        // When
        List<Device> foundDevices = deviceService.findAll();

        // Then
        assertThat(foundDevices).isEqualTo(devices);
        verify(deviceRepository).findAll();
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        // When
        deviceService.deleteById(100L);

        // Then
        verify(deviceRepository).deleteById(100L);
    }
}