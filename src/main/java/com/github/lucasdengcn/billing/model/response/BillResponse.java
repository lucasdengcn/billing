package com.github.lucasdengcn.billing.model.response;

import com.github.lucasdengcn.billing.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Response model representing a generated bill")
public class BillResponse {
    @Schema(description = "Unique database identifier for the bill", example = "1")
    private Long id;

    @Schema(description = "ID of the customer", example = "1")
    private Long customerId;

    @Schema(description = "Start date of the billing period", example = "2026-03-01")
    private LocalDate billingPeriodStart;

    @Schema(description = "End date of the billing period", example = "2026-03-31")
    private LocalDate billingPeriodEnd;

    @Schema(description = "Sum of base and usage fees", example = "35.49")
    private BigDecimal totalFees;

    @Schema(description = "Total base subscription fees", example = "29.99")
    private BigDecimal baseFees;

    @Schema(description = "Total usage-based variable fees", example = "5.50")
    private BigDecimal usageFees;

    @Schema(description = "Current payment state", example = "PAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Timestamp when the bill was fully paid")
    private OffsetDateTime paidAt;
}
