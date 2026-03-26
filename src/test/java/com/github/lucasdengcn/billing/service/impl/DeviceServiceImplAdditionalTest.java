package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.exception.CustomerResolutionException;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplAdditionalTest {

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
    void registerDevice_WhenDeviceNoExists_ShouldReturnExistingDevice() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(1L);
        registerRequest.setCustomer(customerInfo);

        Device existingDevice = Device.builder()
                .id(200L)
                .customer(testCustomer)
                .deviceName("Existing Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .status(DeviceStatus.ACTIVE)
                .build();

        when(deviceRepository.findByDeviceNo("NEW001")).thenReturn(Optional.of(existingDevice));

        // When
        Device result = deviceService.registerDevice(registerRequest);

        // Then
        assertThat(result).isEqualTo(existingDevice);
        verify(deviceRepository).findByDeviceNo("NEW001");
        verify(customerService, never()).findById(anyLong()); // Should not call customer service
        verify(deviceMapper, never()).toEntity(any(DeviceRegisterRequest.class)); // Should not map to entity
        verify(deviceRepository, never()).save(any()); // Should not save new device
    }

    @Test
    void registerDevice_WithMobileNo_ShouldResolveCustomerByMobile() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setMobileNo("1234567890");
        registerRequest.setCustomer(customerInfo);

        Device mappedDevice = Device.builder()
                .deviceName("New Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .build();

        when(customerService.findByMobileNo("1234567890")).thenReturn(testCustomer);
        when(deviceMapper.toEntity(registerRequest)).thenReturn(mappedDevice);
        when(deviceRepository.save(mappedDevice)).thenReturn(testDevice);

        // When
        Device registeredDevice = deviceService.registerDevice(registerRequest);

        // Then
        assertThat(registeredDevice).isEqualTo(testDevice);
        verify(customerService).findByMobileNo("1234567890");
        verify(deviceMapper).toEntity(registerRequest);
        verify(deviceRepository).save(mappedDevice);
        assertThat(mappedDevice.getCustomer()).isEqualTo(testCustomer);
    }

    @Test
    void registerDevice_WithNonExistentMobileNo_ShouldThrowException() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setMobileNo("9999999999");
        customerInfo.setName("New Customer"); // Has name to create new customer
        registerRequest.setCustomer(customerInfo);

        Customer newCustomer = Customer.builder()
                .id(2L)
                .name("New Customer")
                .mobileNo("9999999999")
                .build();

        Device mappedDevice = Device.builder()
                .deviceName("New Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .build();

        when(customerService.findByMobileNo("9999999999")).thenThrow(new ResourceNotFoundException("Not found"));
        when(customerService.save(any(Customer.class))).thenReturn(newCustomer);
        when(deviceMapper.toEntity(registerRequest)).thenReturn(mappedDevice);
        when(deviceRepository.save(mappedDevice)).thenReturn(testDevice);

        // When
        Device registeredDevice = deviceService.registerDevice(registerRequest);

        // Then
        assertThat(registeredDevice).isEqualTo(testDevice);
        verify(customerService).findByMobileNo("9999999999");
        verify(customerService).save(any(Customer.class));
        verify(deviceMapper).toEntity(registerRequest);
        verify(deviceRepository).save(mappedDevice);
    }

    @Test
    void registerDevice_WithNonExistentCustomerInfoAndNoName_ShouldThrowException() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustomerNo("NONEXISTENT");
        registerRequest.setCustomer(customerInfo);

        when(customerService.findByCustomerNo("NONEXISTENT")).thenThrow(new ResourceNotFoundException("Not found"));

        // When & Then
        assertThatThrownBy(() -> deviceService.registerDevice(registerRequest))
                .isInstanceOf(CustomerResolutionException.class);
    }

    @Test
    void registerDevices_WithSomeExistingDevices_ShouldReturnMixedResults() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(1L);

        DeviceUpdateRequest newDeviceRequest = new DeviceUpdateRequest();
        newDeviceRequest.setDeviceName("New Device");
        newDeviceRequest.setDeviceNo("NEW001");
        newDeviceRequest.setDeviceType("TABLET");
        newDeviceRequest.setStatus(DeviceStatus.ACTIVE);

        DeviceUpdateRequest existingDeviceRequest = new DeviceUpdateRequest();
        existingDeviceRequest.setDeviceName("Existing Device");
        existingDeviceRequest.setDeviceNo("EXIST001");
        existingDeviceRequest.setDeviceType("PHONE");
        existingDeviceRequest.setStatus(DeviceStatus.ACTIVE);

        DeviceBatchRegisterRequest batchRequest = new DeviceBatchRegisterRequest();
        batchRequest.setCustomer(customerInfo);
        batchRequest.setDevices(Arrays.asList(newDeviceRequest, existingDeviceRequest));

        Device existingDevice = Device.builder()
                .id(200L)
                .customer(testCustomer)
                .deviceName("Existing Device")
                .deviceNo("EXIST001")
                .deviceType("PHONE")
                .status(DeviceStatus.ACTIVE)
                .build();

        Device newDevice = Device.builder()
                .id(300L)
                .customer(testCustomer)
                .deviceName("New Device")
                .deviceNo("NEW001")
                .deviceType("TABLET")
                .status(DeviceStatus.ACTIVE)
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceRepository.findByDeviceNoIn(Arrays.asList("NEW001", "EXIST001"))).thenReturn(Arrays.asList(existingDevice));
        when(deviceMapper.toEntity(newDeviceRequest)).thenReturn(newDevice);
        when(deviceRepository.saveAll(any(List.class))).thenReturn(Arrays.asList(newDevice));

        // When
        List<Device> registeredDevices = deviceService.registerDevices(batchRequest);

        // Then
        assertThat(registeredDevices).hasSize(2); // One existing + one new
        verify(deviceRepository).findByDeviceNoIn(Arrays.asList("NEW001", "EXIST001"));
        verify(deviceMapper).toEntity(newDeviceRequest); // Only called for new device
        verify(deviceRepository).saveAll(any(List.class)); // Only saves new devices
        verify(customerService).findById(1L);
    }

    @Test
    void registerDevices_WithAllExistingDevices_ShouldReturnOnlyExisting() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(1L);

        DeviceUpdateRequest existingDeviceRequest1 = new DeviceUpdateRequest();
        existingDeviceRequest1.setDeviceName("Existing Device 1");
        existingDeviceRequest1.setDeviceNo("EXIST001");
        existingDeviceRequest1.setDeviceType("PHONE");
        existingDeviceRequest1.setStatus(DeviceStatus.ACTIVE);

        DeviceUpdateRequest existingDeviceRequest2 = new DeviceUpdateRequest();
        existingDeviceRequest2.setDeviceName("Existing Device 2");
        existingDeviceRequest2.setDeviceNo("EXIST002");
        existingDeviceRequest2.setDeviceType("TABLET");
        existingDeviceRequest2.setStatus(DeviceStatus.ACTIVE);

        DeviceBatchRegisterRequest batchRequest = new DeviceBatchRegisterRequest();
        batchRequest.setCustomer(customerInfo);
        batchRequest.setDevices(Arrays.asList(existingDeviceRequest1, existingDeviceRequest2));

        Device existingDevice1 = Device.builder()
                .id(200L)
                .customer(testCustomer)
                .deviceName("Existing Device 1")
                .deviceNo("EXIST001")
                .deviceType("PHONE")
                .status(DeviceStatus.ACTIVE)
                .build();

        Device existingDevice2 = Device.builder()
                .id(201L)
                .customer(testCustomer)
                .deviceName("Existing Device 2")
                .deviceNo("EXIST002")
                .deviceType("TABLET")
                .status(DeviceStatus.ACTIVE)
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceRepository.findByDeviceNoIn(Arrays.asList("EXIST001", "EXIST002"))).thenReturn(Arrays.asList(existingDevice1, existingDevice2));

        // When
        List<Device> registeredDevices = deviceService.registerDevices(batchRequest);

        // Then
        assertThat(registeredDevices).hasSize(2); // Both are existing
        assertThat(registeredDevices).containsExactlyInAnyOrder(existingDevice1, existingDevice2);
        verify(deviceRepository).findByDeviceNoIn(Arrays.asList("EXIST001", "EXIST002"));
        verify(deviceMapper, never()).toEntity(any(DeviceRegisterRequest.class)); // Should not be called for existing devices
        verify(deviceRepository, never()).saveAll(any(List.class)); // Should not save anything
        verify(customerService).findById(1L);
    }

    @Test
    void registerDevices_WithAllNewDevices_ShouldSaveAll() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(1L);

        DeviceUpdateRequest newDeviceRequest1 = new DeviceUpdateRequest();
        newDeviceRequest1.setDeviceName("New Device 1");
        newDeviceRequest1.setDeviceNo("NEW001");
        newDeviceRequest1.setDeviceType("PHONE");
        newDeviceRequest1.setStatus(DeviceStatus.ACTIVE);

        DeviceUpdateRequest newDeviceRequest2 = new DeviceUpdateRequest();
        newDeviceRequest2.setDeviceName("New Device 2");
        newDeviceRequest2.setDeviceNo("NEW002");
        newDeviceRequest2.setDeviceType("TABLET");
        newDeviceRequest2.setStatus(DeviceStatus.ACTIVE);

        DeviceBatchRegisterRequest batchRequest = new DeviceBatchRegisterRequest();
        batchRequest.setCustomer(customerInfo);
        batchRequest.setDevices(Arrays.asList(newDeviceRequest1, newDeviceRequest2));

        Device newDevice1 = Device.builder()
                .id(300L)
                .customer(testCustomer)
                .deviceName("New Device 1")
                .deviceNo("NEW001")
                .deviceType("PHONE")
                .status(DeviceStatus.ACTIVE)
                .build();

        Device newDevice2 = Device.builder()
                .id(301L)
                .customer(testCustomer)
                .deviceName("New Device 2")
                .deviceNo("NEW002")
                .deviceType("TABLET")
                .status(DeviceStatus.ACTIVE)
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceRepository.findByDeviceNoIn(Arrays.asList("NEW001", "NEW002"))).thenReturn(Collections.emptyList());
        when(deviceMapper.toEntity(eq(newDeviceRequest1))).thenReturn(newDevice1);
        when(deviceMapper.toEntity(eq(newDeviceRequest2))).thenReturn(newDevice2);
        when(deviceRepository.saveAll(any(List.class))).thenReturn(Arrays.asList(newDevice1, newDevice2));

        // When
        List<Device> registeredDevices = deviceService.registerDevices(batchRequest);

        // Then
        assertThat(registeredDevices).hasSize(2); // Both are new
        verify(deviceRepository).findByDeviceNoIn(Arrays.asList("NEW001", "NEW002"));
        verify(deviceMapper, times(2)).toEntity(any(DeviceUpdateRequest.class)); // Called for both new devices
        verify(deviceRepository).saveAll(any(List.class)); // Saves both new devices
        verify(customerService).findById(1L);
    }

    @Test
    void registerDevices_WithNullCustomerInfo_ShouldThrowException() {
        // Given
        DeviceBatchRegisterRequest batchRequest = new DeviceBatchRegisterRequest();
        batchRequest.setCustomer(null);
        batchRequest.setDevices(Arrays.asList(updateRequest));

        // When & Then
        assertThatThrownBy(() -> deviceService.registerDevices(batchRequest))
                .isInstanceOf(CustomerResolutionException.class)
                .hasMessage("Customer information is required to register a device");
    }

    @Test
    void registerDevice_WithEmptyDeviceNo_ShouldStillTryToResolveCustomer() {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(1L);
        registerRequest.setCustomer(customerInfo);
        registerRequest.setDeviceNo(""); // Empty deviceNo

        Device mappedDevice = Device.builder()
                .deviceName("New Device")
                .deviceNo("")
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
    }
}
