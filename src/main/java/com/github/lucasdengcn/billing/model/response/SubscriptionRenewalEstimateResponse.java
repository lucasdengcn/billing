package com.github.lucasdengcn.billing.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response model for estimated subscription renewal fee")
public class SubscriptionRenewalEstimateResponse {

    @Schema(description = "Estimated renewal fee", example = "89.99")
    private BigDecimal estimatedFee;

    @Schema(description = "Base fee of the subscription", example = "99.99")
    private BigDecimal baseFee;

    @Schema(description = "Discount rate applied", example = "0.90")
    private BigDecimal discountRate;

    @Schema(description = "Number of renewal periods", example = "1")
    private Integer renewalPeriods;

    @Schema(description = "Product title", example = "Monthly Premium Plan")
    private String productTitle;

    @Schema(description = "Device number", example = "DEV001")
    private String deviceNo;

    @Schema(description = "Product number", example = "PROD001")
    private String productNo;
}
