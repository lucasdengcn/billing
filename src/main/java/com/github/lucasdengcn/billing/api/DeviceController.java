package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.model.request.DeviceRequest;
import com.github.lucasdengcn.billing.model.response.DeviceResponse;
import com.github.lucasdengcn.billing.service.CustomerService;
import com.github.lucasdengcn.billing.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        Customer customer = customerService.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Device device = Device.builder()
                .customer(customer)
                .deviceName(request.getDeviceName())
                .deviceNo(request.getDeviceNo())
                .deviceType(request.getDeviceType())
                .status(request.getStatus())
                .build();
        Device saved = deviceService.save(device);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable Long id) {
        return deviceService.findById(id)
                .map(device -> ResponseEntity.ok(mapToResponse(device)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        List<DeviceResponse> responses = deviceService.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private DeviceResponse mapToResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .customerId(device.getCustomer().getId())
                .deviceName(device.getDeviceName())
                .deviceNo(device.getDeviceNo())
                .deviceType(device.getDeviceType())
                .status(device.getStatus())
                .lastActivityAt(device.getLastActivityAt())
                .build();
    }
}
