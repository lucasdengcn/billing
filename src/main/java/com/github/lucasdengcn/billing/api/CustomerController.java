package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.mapper.CustomerMapper;
import com.github.lucasdengcn.billing.model.response.CustomerResponse;
import com.github.lucasdengcn.billing.model.response.ErrorResponse;
import com.github.lucasdengcn.billing.model.response.ValidationErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.model.request.CustomerRequest;
import com.github.lucasdengcn.billing.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing customers and their profiles")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Registers a new customer in the system")
    @ApiResponse(responseCode = "200", description = "Customer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        // Check if customerNo exists, if so return existing customer
        if (request.getCustomerNo() != null && !request.getCustomerNo().trim().isEmpty()) {
            Customer existingCustomer = customerService.findByCustomerNoOrNull(request.getCustomerNo());
            if (existingCustomer != null) {
                return ResponseEntity.ok(customerMapper.toResponse(existingCustomer));
            }
        }
        
        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerService.save(customer);
        return ResponseEntity.ok(customerMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves a single customer's details by their unique database ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        Customer customer = customerService.findById(id);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @GetMapping
    @Operation(summary = "List all customers", description = "Retrieves a complete list of all registered customers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> responses = customerService.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer", description = "Removes a customer's record from the system")
    @ApiResponse(responseCode = "204", description = "Customer deleted successfully", content = @Content)
    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}