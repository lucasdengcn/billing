package com.github.lucasdengcn.billing.service.impl;

import java.time.OffsetDateTime;
import java.util.List;

import com.github.lucasdengcn.billing.model.request.CustomerInfo;
import com.github.lucasdengcn.billing.model.request.DeviceBatchRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.exception.CustomerResolutionException;
import com.github.lucasdengcn.billing.mapper.DeviceMapper;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import com.github.lucasdengcn.billing.service.DeviceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.lucasdengcn.billing.service.CustomerService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final CustomerService customerService;
    private final DeviceMapper deviceMapper;

    @Override
    public Device save(Device device) {
        log.info("Saving device: {} (No: {})", device.getDeviceName(), device.getDeviceNo());
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public Device registerDevice(DeviceRegisterRequest request) {
        log.info("Registering device: {} for customer info provided", request.getDeviceNo());
        Customer customer = resolveCustomer(request.getCustomer());
        // Check if deviceNo exist, if so return existing device
        Device existingDevice = deviceRepository.findByDeviceNo(request.getDeviceNo()).orElse(null);
        if (existingDevice != null) {
            return existingDevice;
        }
        Device device = deviceMapper.toEntity(request);
        device.setCustomer(customer);
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public List<Device> registerDevices(DeviceBatchRegisterRequest request) {
        log.info("Batch registering {} devices for customer info provided", request.getDevices().size());
        Customer customer = resolveCustomer(request.getCustomer());
        // check if any deviceNo exist, if so return existing devices
        List<String> deviceNos = request.getDevices().stream().map(DeviceUpdateRequest::getDeviceNo).toList();
        List<Device> existingDevices = deviceRepository.findByDeviceNosIn(deviceNos);
        // collect existing deviceNos
        List<String> existingDeviceNos = existingDevices.stream()
                .map(Device::getDeviceNo)
                .toList();
        // filter out existing devices from request devices
        request.getDevices().removeIf(deviceReq -> existingDeviceNos.contains(deviceReq.getDeviceNo()));
        // register new devices
        List<Device> devices = request.getDevices().stream()
                .map(deviceReq -> {
                    Device device = deviceMapper.toEntity(deviceReq);
                    device.setCustomer(customer);
                    return device;
                })
                .toList();
        if (!devices.isEmpty()) {
            return existingDevices;
        }
        List<Device> deviceList = deviceRepository.saveAll(devices);
        existingDevices.addAll(deviceList);
        return existingDevices;
    }

    @Override
    @Transactional
    public Device updateDevice(Long id, DeviceUpdateRequest request) {
        log.info("Updating device with ID: {}", id);
        Device device = findById(id);

        // Update device fields
        deviceMapper.updateEntity(request, device);
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public Device activateDeviceByNo(String deviceNo) {
        log.info("Activating device with No: {}", deviceNo);
        Device device = findByDeviceNo(deviceNo);
        device.setStatus(DeviceStatus.ACTIVE);
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public Device deactivateDeviceByNo(String deviceNo) {
        log.info("Deactivating device with No: {}", deviceNo);
        Device device = findByDeviceNo(deviceNo);
        device.setStatus(DeviceStatus.DEACTIVATED);
        return deviceRepository.save(device);
    }

    private Customer resolveCustomer(CustomerInfo info) {
        if (info == null) {
            throw new CustomerResolutionException("Customer information is required to register a device");
        }

        // 1. Try by customerId
        if (info.getId() != null) {
            return customerService.findById(info.getId());
        }

        // 2. Try by customerNo
        if (info.getCustomerNo() != null && !info.getCustomerNo().isBlank()) {
            try {
                return customerService.findByCustomerNo(info.getCustomerNo());
            } catch (ResourceNotFoundException e) {
                // Not found, will try to create if name is provided
            }
        }

        // 3. Try by mobileNo
        if (info.getMobileNo() != null && !info.getMobileNo().isBlank()) {
            try {
                return customerService.findByMobileNo(info.getMobileNo());
            } catch (ResourceNotFoundException e) {
                // Not found
            }
        }

        // 4. Create new customer if name is provided
        if (info.getName() != null && !info.getName().isBlank()) {
            log.info("Creating new customer inline: {}", info);
            Customer newCustomer = Customer.builder()
                    .customerNo(info.getCustomerNo())
                    .name(info.getName())
                    .wechatId(info.getWechatId())
                    .mobileNo(info.getMobileNo())
                    .build();
            return customerService.save(newCustomer);
        }

        throw new CustomerResolutionException();
    }

    @Override
    @Transactional(readOnly = true)
    public Device findById(Long id) {
        log.debug("Finding device by ID: {}", id);
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Device findByDeviceNo(String deviceNo) {
        log.debug("Finding device by number: {}", deviceNo);
        return deviceRepository.findByDeviceNo(deviceNo)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with deviceNo: " + deviceNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findByCustomer(Customer customer) {
        log.debug("Finding devices for customer: {}", customer.getId());
        return deviceRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findByStatus(DeviceStatus status) {
        log.debug("Finding devices by status: {}", status);
        return deviceRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findAll() {
        log.debug("Fetching all devices");
        return deviceRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting device with ID: {}", id);
        deviceRepository.deleteById(id);
    }
}