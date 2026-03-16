package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.Bill;
import com.github.lucasdengcn.billing.entity.BillDetail;
import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillingService {
    Bill saveBill(Bill bill);
    Optional<Bill> findBillById(Long id);
    List<Bill> findBillsByCustomer(Customer customer);
    List<Bill> findBillsByPaymentStatus(PaymentStatus status);
    List<Bill> findBillsByPeriod(LocalDate start, LocalDate end);
    void deleteBillById(Long id);

    BillDetail saveBillDetail(BillDetail detail);
    List<BillDetail> findDetailsByBill(Bill bill);
}
