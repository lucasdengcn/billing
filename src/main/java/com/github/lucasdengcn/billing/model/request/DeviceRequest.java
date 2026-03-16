package com.github.lucasdengcn.billing.model.request;

import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request model for registering or updating a device")
public class DeviceRequest {
    @Valid
    @Schema(description = "Customer information for lookup or creation")
    private CustomerInfo customer;

    @Schema(description = "Friendly name for the device", example = "iPhone 15 Pro")
    @Size(max = 255, message = "Device name cannot exceed 255 characters")
    private String deviceName;

    @NotBlank(message = "Device number is required")
    @Size(max = 255, message = "Device number cannot exceed 255 characters")
    @Schema(description = "Unique identifier for the hardware device", example = "HW-12345678")
    private String deviceNo;

    @Schema(description = "Type of device", example = "MOBILE")
    @Size(max = 50, message = "Device type cannot exceed 50 characters")
    private String deviceType;

    @Schema(description = "Current status of the device", example = "ACTIVE")
    private DeviceStatus status;

    @Data
    @Schema(description = "Embedded customer information within device request")
    public static class CustomerInfo {
        @Schema(description = "ID of an existing customer", example = "1")
        private Long id;

        @Schema(description = "Customer business number", example = "CUST-001")
        @Size(max = 50, message = "Customer number cannot exceed 50 characters")
        private String customerNo;

        @Schema(description = "Customer name (required for new customers)", example = "John Doe")
        @Size(max = 255, message = "Customer name cannot exceed 255 characters")
        private String name;

        @Schema(description = "Customer WeChat ID", example = "wx_john")
        @Size(max = 255, message = "WeChat ID cannot exceed 255 characters")
        private String wechatId;

        @Schema(description = "Customer mobile number", example = "+8613800000000")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
        private String mobileNo;
    }
}
