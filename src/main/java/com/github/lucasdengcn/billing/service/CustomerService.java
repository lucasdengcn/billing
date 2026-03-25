package com.github.lucasdengcn.billing.service;

import java.util.List;

import com.github.lucasdengcn.billing.entity.Customer;

public interface CustomerService {
  Customer save(Customer customer);

  Customer findById(Long id);

  Customer findByCustomerNo(String customerNo);

  Customer findByWechatId(String wechatId);

  Customer findByMobileNo(String mobileNo);

  // Methods that return null instead of throwing exceptions
  Customer findByCustomerNoOrNull(String customerNo);

  List<Customer> findAll();

  void deleteById(Long id);
}