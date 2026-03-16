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

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public Device save(Device device) {
        return deviceRepository.save(device);
    }

    @Override
    @Transactional(readOnly = true)
    public Device findById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Device findByDeviceNo(String deviceNo) {
        return deviceRepository.findByDeviceNo(deviceNo)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with deviceNo: " + deviceNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findByCustomer(Customer customer) {
        return deviceRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        deviceRepository.deleteById(id);
    }
}
