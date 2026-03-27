package com.github.lucasdengcn.billing.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request model for creating or updating a customer")
public class CustomerRequest {

  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
  @Schema(description = "Customer's full name", example = "John Doe")
  private String name;

  @NotBlank(message = "Customer number is required")
  @Schema(description = "Unique customer number", example = "CUST-2026-001")
  @Size(min = 6, max = 50, message = "Customer number must be between 6 and 50 characters")
  @Pattern(regexp = "^[A-Z]{2,4}-\\d{3,12}$", message = "Customer number must follow the format: XX-123456 or XXX-12345678 (2-4 letters, hyphen, 3-12 digits)")
  private String customerNo;

  @Schema(description = "WeChat ID for communication", example = "wx_12345")
  @Size(max = 255, message = "WeChat ID cannot exceed 255 characters")
  private String wechatId;

  @Schema(description = "Mobile number for contact", example = "+8613800000000")
  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
  private String mobileNo;
}
