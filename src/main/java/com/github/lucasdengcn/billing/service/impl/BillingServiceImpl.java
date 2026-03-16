package com.github.lucasdengcn.billing.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.entity.Bill;
import com.github.lucasdengcn.billing.entity.BillDetail;
import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.enums.PaymentStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.repository.BillDetailRepository;
import com.github.lucasdengcn.billing.repository.BillRepository;
import com.github.lucasdengcn.billing.service.BillingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final BillDetailRepository billDetailRepository;

    @Override
    public Bill saveBill(Bill bill) {
        log.info("Saving bill for customer: {} for period {} to {}",
                bill.getCustomer().getId(), bill.getBillingPeriodStart(), bill.getBillingPeriodEnd());
        return billRepository.save(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public Bill findBillById(Long id) {
        log.debug("Finding bill by ID: {}", id);
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findBillsByCustomer(Customer customer) {
        log.debug("Finding bills for customer: {}", customer.getId());
        return billRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findBillsByPaymentStatus(PaymentStatus status) {
        log.debug("Finding bills by payment status: {}", status);
        return billRepository.findByPaymentStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findBillsByPeriod(LocalDate start, LocalDate end) {
        log.debug("Finding bills for period {} to {}", start, end);
        return billRepository.findByBillingPeriodStartBetween(start, end);
    }

    @Override
    public void deleteBillById(Long id) {
        log.info("Deleting bill with ID: {}", id);
        billRepository.deleteById(id);
    }

    @Override
    public BillDetail saveBillDetail(BillDetail detail) {
        log.info("Saving bill detail for bill: {}", detail.getBill().getId());
        return billDetailRepository.save(detail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDetail> findDetailsByBill(Bill bill) {
        log.debug("Finding details for bill: {}", bill.getId());
        return billDetailRepository.findByBill(bill);
    }
}
