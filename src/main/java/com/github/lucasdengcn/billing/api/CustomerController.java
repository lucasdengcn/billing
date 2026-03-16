package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.mapper.CustomerMapper;
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
    private final CustomerMapper customerMapper;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerService.save(customer);
        return ResponseEntity.ok(customerMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(customerMapper.toResponse(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> responses = customerService.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
