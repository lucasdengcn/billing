package com.github.lucasdengcn.billing.service;

import java.util.List;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.model.request.CustomerRequest;

public interface CustomerService {
  Customer save(Customer customer);

  Customer findById(Long id);

  Customer findByCustomerNo(String customerNo);

  Customer findByWechatId(String wechatId);

  Customer findByMobileNo(String mobileNo);

  // Methods that return null instead of throwing exceptions
  Customer findByCustomerNoOrNull(String customerNo);

  Customer createOrGetCustomer(CustomerRequest request);

  List<Customer> findAll();

  void deleteById(Long id);
}