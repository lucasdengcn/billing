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
import com.github.lucasdengcn.billing.service.BillingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.service.CustomerService;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

        private final BillingService billingService;
        private final CustomerService customerService;
        private final BillMapper billMapper;

        @PostMapping
        public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
                Customer customer = customerService.findById(request.getCustomerId());

                Bill bill = billMapper.toEntity(request);
                bill.setCustomer(customer);
                Bill saved = billingService.saveBill(bill);
                return ResponseEntity.ok(billMapper.toResponse(saved));
        }

        @GetMapping("/{id}")
        public ResponseEntity<BillResponse> getBill(@PathVariable Long id) {
                Bill bill = billingService.findBillById(id);
                return ResponseEntity.ok(billMapper.toResponse(bill));
        }

        @GetMapping("/customer/{customerId}")
        public ResponseEntity<List<BillResponse>> getCustomerBills(@PathVariable Long customerId) {
                Customer customer = customerService.findById(customerId);
                List<BillResponse> responses = billingService.findBillsByCustomer(customer).stream()
                                .map(billMapper::toResponse)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }
}
