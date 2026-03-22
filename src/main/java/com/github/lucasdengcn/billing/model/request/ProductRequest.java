package com.github.lucasdengcn.billing.model.request;

import java.math.BigDecimal;

import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import jakarta.validation.constraints.Pattern;

@Data
@Schema(description = "Request model for creating or updating a product")
public class ProductRequest {

    @NotBlank(message = "Product number is required")
    @Size(min = 2, max = 50, message = "Product number must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Product number can only contain uppercase letters, numbers, hyphens, and underscores")
    @Schema(description = "Unique product number identifier", example = "PREMIUM_PLAN_001")
    private String productNo;

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    @Schema(description = "Title of the service/product", example = "Premium Plan")
    private String title;

    @Schema(description = "JSON description of the product", example = "{\"tier\":\"premium\",\"support\":\"24/7\"}")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be non-negative")
    @Schema(description = "Standard price before any discounts", example = "29.99")
    private BigDecimal basePrice;

    @NotNull(message = "Price type is required")
    @Schema(description = "Type of pricing model for the product", example = "MONTHLY")
    private PriceType priceType;

    @DecimalMax(value = "1.0", inclusive = true, message = "Discount rate must be at most 1.0")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount rate must be at least 0.0")
    @Schema(description = "Discount rate applied to base price (1.0 = no discount), fee = basePrice * discountRate", example = "0.90")
    private BigDecimal discountRate;

    @Schema(description = "Whether a discount is currently active for this product", example = "1")
    private DiscountStatus discountStatus;
}