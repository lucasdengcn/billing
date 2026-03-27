package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.mapper.CustomerMapper;
import com.github.lucasdengcn.billing.model.request.CustomerRequest;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private CustomerRequest testRequest;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST-001")
                .wechatId("test_wechat")
                .mobileNo("1234567890")
                .build();

        testRequest = new CustomerRequest();
        testRequest.setName("New Customer");
        testRequest.setCustomerNo("CUST-NEW");
        testRequest.setWechatId("new_wechat");
        testRequest.setMobileNo("0987654321");
    }

    @Test
    void save_ShouldCallRepositorySave() {
        // Given
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        // When
        Customer savedCustomer = customerService.save(testCustomer);

        // Then
        assertThat(savedCustomer).isEqualTo(testCustomer);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void findById_WhenCustomerExists_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When
        Customer foundCustomer = customerService.findById(1L);

        // Then
        assertThat(foundCustomer).isEqualTo(testCustomer);
        verify(customerRepository).findById(1L);
    }

    @Test
    void findById_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 999");
    }

    @Test
    void findByCustomerNo_WhenCustomerExists_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findByCustomerNo("CUST-001")).thenReturn(Optional.of(testCustomer));

        // When
        Customer foundCustomer = customerService.findByCustomerNo("CUST-001");

        // Then
        assertThat(foundCustomer).isEqualTo(testCustomer);
        verify(customerRepository).findByCustomerNo("CUST-001");
    }

    @Test
    void findByCustomerNo_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Given
        when(customerRepository.findByCustomerNo("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.findByCustomerNo("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with customerNo: NONEXISTENT");
    }

    @Test
    void findByCustomerNoOrNull_WhenCustomerExists_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findByCustomerNo("CUST-001")).thenReturn(Optional.of(testCustomer));

        // When
        Customer foundCustomer = customerService.findByCustomerNoOrNull("CUST-001");

        // Then
        assertThat(foundCustomer).isEqualTo(testCustomer);
        verify(customerRepository).findByCustomerNo("CUST-001");
    }

    @Test
    void findByCustomerNoOrNull_WhenCustomerDoesNotExist_ShouldReturnNull() {
        // Given
        when(customerRepository.findByCustomerNo("NONEXISTENT")).thenReturn(Optional.empty());

        // When
        Customer foundCustomer = customerService.findByCustomerNoOrNull("NONEXISTENT");

        // Then
        assertThat(foundCustomer).isNull();
        verify(customerRepository).findByCustomerNo("NONEXISTENT");
    }

    @Test
    void createOrGetCustomer_WhenCustomerNoExists_ShouldReturnExistingCustomer() {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo("EXISTING-CUST");
        request.setName("New Name");

        Customer existingCustomer = Customer.builder()
                .id(2L)
                .name("Existing Customer")
                .customerNo("EXISTING-CUST")
                .build();

        when(customerRepository.findByCustomerNo("EXISTING-CUST")).thenReturn(Optional.of(existingCustomer));

        // When
        Customer result = customerService.createOrGetCustomer(request);

        // Then
        assertThat(result).isEqualTo(existingCustomer);
        verify(customerRepository).findByCustomerNo("EXISTING-CUST");
        verify(customerMapper, never()).toEntity(any()); // Should not create new customer
        verify(customerRepository, never()).save(any()); // Should not save new customer
    }

    @Test
    void createOrGetCustomer_WhenCustomerNoDoesNotExist_ShouldCreateNewCustomer() {
        // Given
        when(customerRepository.findByCustomerNo(testRequest.getCustomerNo())).thenReturn(Optional.empty());
        when(customerMapper.toEntity(testRequest)).thenReturn(testCustomer);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        // When
        Customer result = customerService.createOrGetCustomer(testRequest);

        // Then
        assertThat(result).isEqualTo(testCustomer);
        verify(customerRepository).findByCustomerNo(testRequest.getCustomerNo());
        verify(customerMapper).toEntity(testRequest);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void createOrGetCustomer_WhenCustomerNoIsNull_ShouldCreateNewCustomer() {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo(null);
        request.setName("New Customer Without Number");

        Customer newCustomer = Customer.builder()
                .id(3L)
                .name("New Customer Without Number")
                .build();

        // When
        assertThatThrownBy(() -> customerService.createOrGetCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer number is required");

        // Then
        verify(customerRepository, never()).findByCustomerNo(any()); // Should not check for customerNo
        verify(customerMapper, never()).toEntity(request);
        verify(customerRepository, never()).save(newCustomer);
    }

    @Test
    void createOrGetCustomer_WhenCustomerNoIsEmpty_ShouldThrowException() {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo("");
        request.setName("New Customer Empty Number");

        Customer newCustomer = Customer.builder()
                .id(4L)
                .name("New Customer Empty Number")
                .build();

        // When
        assertThatThrownBy(() -> customerService.createOrGetCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer number is required");

        // Then
        verify(customerRepository, never()).findByCustomerNo(any()); // Should not check for customerNo
        verify(customerMapper, never()).toEntity(request);
        verify(customerRepository, never()).save(newCustomer);
    }

    @Test
    void createOrGetCustomer_WhenCustomerNoIsWhitespace_ShouldThrowException() {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo("   ");
        request.setName("New Customer Whitespace Number");

        Customer newCustomer = Customer.builder()
                .id(5L)
                .name("New Customer Whitespace Number")
                .build();

        // When
        assertThatThrownBy(() -> customerService.createOrGetCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer number is required");

        // Then
        verify(customerRepository, never()).findByCustomerNo(any()); // Should not check for customerNo
        verify(customerMapper, never()).toEntity(request);
        verify(customerRepository, never()).save(newCustomer);
    }

    @Test
    void findByWechatId_WhenCustomerExists_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findByWechatId("test_wechat")).thenReturn(Optional.of(testCustomer));

        // When
        Customer foundCustomer = customerService.findByWechatId("test_wechat");

        // Then
        assertThat(foundCustomer).isEqualTo(testCustomer);
        verify(customerRepository).findByWechatId("test_wechat");
    }

    @Test
    void findByWechatId_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Given
        when(customerRepository.findByWechatId("nonexistent_wechat")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.findByWechatId("nonexistent_wechat"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with wechatId: nonexistent_wechat");
    }

    @Test
    void findByMobileNo_WhenCustomerExists_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findByMobileNo("1234567890")).thenReturn(Optional.of(testCustomer));

        // When
        Customer foundCustomer = customerService.findByMobileNo("1234567890");

        // Then
        assertThat(foundCustomer).isEqualTo(testCustomer);
        verify(customerRepository).findByMobileNo("1234567890");
    }

    @Test
    void findByMobileNo_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Given
        when(customerRepository.findByMobileNo("nonexistent_mobile")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.findByMobileNo("nonexistent_mobile"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with mobileNo: nonexistent_mobile");
    }

    @Test
    void findAll_ShouldReturnAllCustomers() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        // When
        List<Customer> foundCustomers = customerService.findAll();

        // Then
        assertThat(foundCustomers).isEqualTo(customers);
        verify(customerRepository).findAll();
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        // When
        customerService.deleteById(1L);
        // Then
        verify(customerRepository).deleteById(1L);
    }
}

