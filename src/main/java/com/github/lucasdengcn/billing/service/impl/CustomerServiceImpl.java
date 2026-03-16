package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer save(Customer customer) {
        log.info("Saving customer: {}", customer.getName());
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        log.debug("Finding customer by ID: {}", id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findByCustomerNo(String customerNo) {
        log.debug("Finding customer by customerNo: {}", customerNo);
        return customerRepository.findByCustomerNo(customerNo)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with customerNo: " + customerNo));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findByWechatId(String wechatId) {
        log.debug("Finding customer by WeChat ID: {}", wechatId);
        return customerRepository.findByWechatId(wechatId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with wechatId: " + wechatId));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findByMobileNo(String mobileNo) {
        log.debug("Finding customer by mobile number: {}", mobileNo);
        return customerRepository.findByMobileNo(mobileNo)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with mobileNo: " + mobileNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        log.debug("Fetching all customers");
        return customerRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting customer with ID: {}", id);
        customerRepository.deleteById(id);
    }
}
