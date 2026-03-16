package com.github.lucasdengcn.billing.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.github.lucasdengcn.billing.entity.enums.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Request model for generating or updating a bill")
public class BillRequest {
    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the customer being billed", example = "1")
    private Long customerId;

    @NotNull(message = "Billing period start is required")
    @Schema(description = "Start date of the billing period", example = "2026-03-01")
    private LocalDate billingPeriodStart;

    @NotNull(message = "Billing period end is required")
    @Schema(description = "End date of the billing period", example = "2026-03-31")
    private LocalDate billingPeriodEnd;

    @DecimalMin(value = "0.0", inclusive = true, message = "Base fees must be non-negative")
    @Schema(description = "Total base subscription fees for the period", example = "29.99")
    private BigDecimal baseFees;

    @DecimalMin(value = "0.0", inclusive = true, message = "Usage fees must be non-negative")
    @Schema(description = "Variable usage-based fees for the period", example = "5.50")
    private BigDecimal usageFees;

    @Schema(description = "Current payment state of the bill", example = "PENDING")
    private PaymentStatus paymentStatus;
}
