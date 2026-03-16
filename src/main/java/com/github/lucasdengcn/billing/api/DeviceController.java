package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.mapper.DeviceMapper;
import com.github.lucasdengcn.billing.model.request.DeviceRequest;
import com.github.lucasdengcn.billing.model.response.DeviceResponse;
import com.github.lucasdengcn.billing.model.response.ErrorResponse;
import com.github.lucasdengcn.billing.model.response.ValidationErrorResponse;
import com.github.lucasdengcn.billing.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.service.CustomerService;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "APIs for registering and managing customer devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final CustomerService customerService;
    private final DeviceMapper deviceMapper;

    @PostMapping
    @Operation(summary = "Register a new device", description = "Registers a hardware device and optionally creates/resolves its owner")
    @ApiResponse(responseCode = "200", description = "Device registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or customer data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        Device saved = deviceService.registerDevice(request);
        return ResponseEntity.ok(deviceMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieves a single device's details by its unique database ID")
    @ApiResponse(responseCode = "200", description = "Device found")
    @ApiResponse(responseCode = "404", description = "Device not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable Long id) {
        Device device = deviceService.findById(id);
        return ResponseEntity.ok(deviceMapper.toResponse(device));
    }

    @GetMapping
    @Operation(summary = "List all devices", description = "Retrieves a complete list of all registered devices")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        List<DeviceResponse> responses = deviceService.findAll().stream()
                .map(deviceMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
