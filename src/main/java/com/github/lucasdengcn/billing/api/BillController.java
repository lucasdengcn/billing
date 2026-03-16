package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Bill;
import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.mapper.BillMapper;
import com.github.lucasdengcn.billing.model.request.BillRequest;
import com.github.lucasdengcn.billing.model.response.BillResponse;
import com.github.lucasdengcn.billing.model.response.ErrorResponse;
import com.github.lucasdengcn.billing.model.response.ValidationErrorResponse;
import com.github.lucasdengcn.billing.service.BillingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.service.CustomerService;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Billing Management", description = "APIs for generating and managing customer bills")
public class BillController {

        private final BillingService billingService;
        private final CustomerService customerService;
        private final BillMapper billMapper;

        @PostMapping
        @Operation(summary = "Create a new bill", description = "Generates a new billing record for a customer and period")
        @ApiResponse(responseCode = "200", description = "Bill created successfully")
        @ApiResponse(responseCode = "400", description = "Invalid billing data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
                Customer customer = customerService.findById(request.getCustomerId());

                Bill bill = billMapper.toEntity(request);
                bill.setCustomer(customer);
                Bill saved = billingService.saveBill(bill);
                return ResponseEntity.ok(billMapper.toResponse(saved));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get bill by ID", description = "Retrieves details of a specific bill")
        @ApiResponse(responseCode = "200", description = "Bill found")
        @ApiResponse(responseCode = "404", description = "Bill not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<BillResponse> getBill(@PathVariable Long id) {
                Bill bill = billingService.findBillById(id);
                return ResponseEntity.ok(billMapper.toResponse(bill));
        }

        @GetMapping("/customer/{customerId}")
        @Operation(summary = "List customer bills", description = "Retrieves all bills associated with a specific customer")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved bills")
        @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<List<BillResponse>> getCustomerBills(@PathVariable Long customerId) {
                Customer customer = customerService.findById(customerId);
                List<BillResponse> responses = billingService.findBillsByCustomer(customer).stream()
                                .map(billMapper::toResponse)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }
}
