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

  @Schema(description = "WeChat ID for communication", example = "wx_12345")
  @Size(max = 255, message = "WeChat ID cannot exceed 255 characters")
  private String wechatId;

  @Schema(description = "Mobile number for contact", example = "+8613800000000")
  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
  private String mobileNo;
}
