package com.github.lucasdengcn.billing.model.request;

import java.math.BigDecimal;

import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request model for creating or updating a product")
public class ProductRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    @Schema(description = "Title of the service/product", example = "Premium Plan")
    private String title;

    @Schema(description = "JSON description of the product", example = "{\"tier\":\"premium\",\"support\":\"24/7\"}")
    private String description;

    @NotNull(message = "Base monthly fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base monthly fee must be non-negative")
    @Schema(description = "Standard monthly cost before any discounts", example = "29.99")
    private BigDecimal baseMonthlyFee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount rate must be at least 0.0")
    @Schema(description = "Discount rate applied to base fee (1.0 = no discount)", example = "0.90")
    private BigDecimal discountRate;

    @Schema(description = "Whether a discount is currently active for this product", example = "ACTIVE")
    private DiscountStatus discountStatus;
}
