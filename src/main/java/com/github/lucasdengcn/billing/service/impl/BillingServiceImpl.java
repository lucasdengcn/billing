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

@Service
@RequiredArgsConstructor
@Transactional
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final BillDetailRepository billDetailRepository;

    @Override
    public Bill saveBill(Bill bill) {
        return billRepository.save(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public Bill findBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findBillsByCustomer(Customer customer) {
        return billRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findBillsByPaymentStatus(PaymentStatus status) {
        return billRepository.findByPaymentStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findBillsByPeriod(LocalDate start, LocalDate end) {
        return billRepository.findByBillingPeriodStartBetween(start, end);
    }

    @Override
    public void deleteBillById(Long id) {
        billRepository.deleteById(id);
    }

    @Override
    public BillDetail saveBillDetail(BillDetail detail) {
        return billDetailRepository.save(detail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDetail> findDetailsByBill(Bill bill) {
        return billDetailRepository.findByBill(bill);
    }
}
