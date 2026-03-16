package com.github.lucasdengcn.billing.service;

import java.util.List;
import java.util.Optional;

import com.github.lucasdengcn.billing.entity.Customer;

public interface CustomerService {
  Customer save(Customer customer);

  Optional<Customer> findById(Long id);

  Optional<Customer> findByWechatId(String wechatId);

  Optional<Customer> findByMobileNo(String mobileNo);

  List<Customer> findAll();

  void deleteById(Long id);
}
