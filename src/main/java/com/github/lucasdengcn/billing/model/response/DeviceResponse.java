package com.github.lucasdengcn.billing.model.response;

import java.time.OffsetDateTime;

import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response model representing a registered device")
public class DeviceResponse {
    @Schema(description = "Unique database identifier for the device", example = "1")
    private Long id;

    @Schema(description = "Friendly name for the device", example = "iPhone 15 Pro")
    private String deviceName;

    @Schema(description = "Unique hardware identifier", example = "HW-12345678")
    private String deviceNo;

    @Schema(description = "Device hardware or platform type", example = "MOBILE")
    private String deviceType;

    @Schema(description = "Current activation status of the device", example = "ACTIVE")
    private DeviceStatus status;

    @Schema(description = "Timestamp of the last recorded activity from this device")
    private OffsetDateTime lastActivityAt;

    @Schema(description = "Timestamp when the device was last updated")
    private OffsetDateTime updatedAt;

    @Schema(description = "Timestamp when the device was created")
    private OffsetDateTime createdAt;
}
