package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Bill;
import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByCustomer(Customer customer);
    List<Bill> findByPaymentStatus(PaymentStatus status);
    List<Bill> findByBillingPeriodStartBetween(LocalDate start, LocalDate end);
}
