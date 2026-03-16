package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import com.github.lucasdengcn.billing.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Device> findByDeviceNo(String deviceNo) {
        return deviceRepository.findByDeviceNo(deviceNo);
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
