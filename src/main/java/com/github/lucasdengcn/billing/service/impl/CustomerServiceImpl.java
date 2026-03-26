package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.mapper.CustomerMapper;
import com.github.lucasdengcn.billing.model.request.CustomerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

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
    public Customer findByCustomerNoOrNull(String customerNo) {
        log.debug("Finding customer by customerNo (null-safe): {}", customerNo);
        Optional<Customer> optionalCustomer = customerRepository.findByCustomerNo(customerNo);
        return optionalCustomer.orElse(null);
    }

    @Override
    @Transactional
    public Customer createOrGetCustomer(CustomerRequest request) {
        log.debug("Creating or getting customer with customer number: {}", request.getCustomerNo());
        if (request.getCustomerNo() == null || request.getCustomerNo().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer number is required");
        }
        // Check if customerNo exists, if so return existing customer
        {
            Customer existingCustomer = findByCustomerNoOrNull(request.getCustomerNo());
            if (existingCustomer != null) {
                log.info("Customer with customerNo {} already exists, returning existing customer", request.getCustomerNo());
                return existingCustomer;
            }
        }
        
        // Create new customer
        Customer customer = customerMapper.toEntity(request);
        Customer saved = save(customer);
        log.info("Created new customer with customerNo: {}", saved.getCustomerNo());
        return saved;
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