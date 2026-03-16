package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findByWechatId(String wechatId) {
        return customerRepository.findByWechatId(wechatId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with wechatId: " + wechatId));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findByMobileNo(String mobileNo) {
        return customerRepository.findByMobileNo(mobileNo)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with mobileNo: " + mobileNo));
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
