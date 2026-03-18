package com.github.lucasdengcn.billing.model.request;

import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request model for updating an existing device")
public class DeviceUpdateRequest {

    @Schema(description = "Friendly name for the device", example = "iPhone 15 Pro")
    @Size(max = 255, message = "Device name cannot exceed 255 characters")
    private String deviceName;

    @NotBlank(message = "Device number is required")
    @Size(max = 255, message = "Device number cannot exceed 255 characters")
    @Pattern(regexp = "^[A-Z]{2,4}-\\d{6,12}$", message = "Device number must follow the format: XX-123456 or XXX-12345678 (2-4 letters, hyphen, 6-12 digits)")
    @Schema(description = "Unique identifier for the hardware device", example = "HW-12345678")
    private String deviceNo;

    @Schema(description = "Type of device", example = "MOBILE")
    @Size(max = 50, message = "Device type cannot exceed 50 characters")
    private String deviceType;

    @Schema(description = "Current status of the device", example = "ACTIVE")
    @NotNull(message = "Device status is required")
    private DeviceStatus status;
}
