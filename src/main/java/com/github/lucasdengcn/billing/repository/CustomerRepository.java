package com.github.lucasdengcn.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.lucasdengcn.billing.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByWechatId(String wechatId);

  Optional<Customer> findByMobileNo(String mobileNo);
}
