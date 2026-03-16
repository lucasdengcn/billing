package com.github.lucasdengcn.billing.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Response model representing a customer's details")
public class CustomerResponse {
    @Schema(description = "Unique database identifier for the customer", example = "1")
    private Long id;

    @Schema(description = "Customer's full name", example = "John Doe")
    private String name;

    @Schema(description = "Unique customer number", example = "CUST-2026-001")
    private String customerNo;

    @Schema(description = "WeChat ID", example = "wx_12345")
    private String wechatId;

    @Schema(description = "Mobile number", example = "+8613800000000")
    private String mobileNo;

    @Schema(description = "Timestamp when the record was created")
    private OffsetDateTime createdAt;

    @Schema(description = "Timestamp when the record was last updated")
    private OffsetDateTime updatedAt;
}
