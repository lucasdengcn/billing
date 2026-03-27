package com.github.lucasdengcn.billing.model.request;

import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Request model for subscription renewal")
public class SubscriptionRenewalRequest {
    
    @NotBlank(message = "Device number is required")
    @Schema(description = "Device number", example = "DEV001", required = true)
    private String deviceNo;

    @NotBlank(message = "Product number is required")
    @Schema(description = "Product number", example = "PROD001", required = true)
    private String productNo;
    
    @NotNull(message = "Renewal periods is required")
    @Schema(description = "Number of renewal periods", example = "1", defaultValue = "1")
    @Positive(message = "Renewal periods must be positive")
    private Integer renewalPeriods;
    
    @NotNull(message = "Renewal period unit is required")
    @Schema(description = "Unit of renewal period", example = "months", defaultValue = "months")
    private PeriodUnit renewalPeriodUnit;

}
