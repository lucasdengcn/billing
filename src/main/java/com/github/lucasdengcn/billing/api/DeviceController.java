package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.mapper.DeviceMapper;
import com.github.lucasdengcn.billing.model.request.DeviceRequest;
import com.github.lucasdengcn.billing.model.response.DeviceResponse;
import com.github.lucasdengcn.billing.service.DeviceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.service.CustomerService;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final CustomerService customerService;
    private final DeviceMapper deviceMapper;

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        Customer customer = customerService.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Device device = deviceMapper.toEntity(request);
        device.setCustomer(customer);
        Device saved = deviceService.save(device);
        return ResponseEntity.ok(deviceMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable Long id) {
        return deviceService.findById(id)
                .map(device -> ResponseEntity.ok(deviceMapper.toResponse(device)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        List<DeviceResponse> responses = deviceService.findAll().stream()
                .map(deviceMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
