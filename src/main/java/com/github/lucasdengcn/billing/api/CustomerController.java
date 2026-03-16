package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.model.request.CustomerRequest;
import com.github.lucasdengcn.billing.model.response.CustomerResponse;
import com.github.lucasdengcn.billing.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .wechatId(request.getWechatId())
                .mobileNo(request.getMobileNo())
                .build();
        Customer saved = customerService.save(customer);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(mapToResponse(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> responses = customerService.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .wechatId(customer.getWechatId())
                .mobileNo(customer.getMobileNo())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
