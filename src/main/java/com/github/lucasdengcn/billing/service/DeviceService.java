package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.model.request.DeviceRegisterRequest;

import java.util.List;

public interface DeviceService {
    Device save(Device device);
    Device registerDevice(DeviceRegisterRequest request);
    Device findById(Long id);
    Device findByDeviceNo(String deviceNo);
    List<Device> findByCustomer(Customer customer);
    List<Device> findByStatus(DeviceStatus status);
    List<Device> findAll();
    void deleteById(Long id);
}
