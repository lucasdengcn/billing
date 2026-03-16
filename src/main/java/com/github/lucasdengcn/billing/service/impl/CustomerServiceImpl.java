package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByWechatId(String wechatId) {
        return customerRepository.findByWechatId(wechatId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByMobileNo(String mobileNo) {
        return customerRepository.findByMobileNo(mobileNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }
}
