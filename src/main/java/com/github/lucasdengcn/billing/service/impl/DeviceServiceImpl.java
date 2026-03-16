package com.github.lucasdengcn.billing.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import com.github.lucasdengcn.billing.service.DeviceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public Device save(Device device) {
        log.info("Saving device: {} (No: {})", device.getDeviceName(), device.getDeviceNo());
        return deviceRepository.save(device);
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
