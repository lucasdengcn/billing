package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Bill;
import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.model.request.BillRequest;
import com.github.lucasdengcn.billing.model.response.BillResponse;
import com.github.lucasdengcn.billing.service.BillingService;
import com.github.lucasdengcn.billing.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillingService billingService;
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
        Customer customer = customerService.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Bill bill = Bill.builder()
                .customer(customer)
                .billingPeriodStart(request.getBillingPeriodStart())
                .billingPeriodEnd(request.getBillingPeriodEnd())
                .baseFees(request.getBaseFees())
                .usageFees(request.getUsageFees())
                .paymentStatus(request.getPaymentStatus())
                .build();
        Bill saved = billingService.saveBill(bill);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponse> getBill(@PathVariable Long id) {
        return billingService.findBillById(id)
                .map(bill -> ResponseEntity.ok(mapToResponse(bill)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BillResponse>> getCustomerBills(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        List<BillResponse> responses = billingService.findBillsByCustomer(customer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private BillResponse mapToResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .customerId(bill.getCustomer().getId())
                .billingPeriodStart(bill.getBillingPeriodStart())
                .billingPeriodEnd(bill.getBillingPeriodEnd())
                .totalFees(bill.getTotalFees())
                .baseFees(bill.getBaseFees())
                .usageFees(bill.getUsageFees())
                .paymentStatus(bill.getPaymentStatus())
                .paidAt(bill.getPaidAt())
                .build();
    }
}
