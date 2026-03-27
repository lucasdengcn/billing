package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.model.request.CustomerRequest;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:customer-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class CustomerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer testCustomer1;
    private Customer testCustomer2;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with WebApplicationContext
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // Clean up database
        customerRepository.deleteAll();

        // Create test customers
        testCustomer1 = Customer.builder()
                .customerNo("CUST-001")
                .name("John Doe")
                .wechatId("john_wechat")
                .mobileNo("12345678901")
                .build();
        testCustomer1 = customerRepository.save(testCustomer1);

        testCustomer2 = Customer.builder()
                .customerNo("CUST-002")
                .name("Jane Smith")
                .wechatId("jane_wechat")
                .mobileNo("12345678902")
                .build();
        testCustomer2 = customerRepository.save(testCustomer2);
    }

    @Test
    void createCustomer_WithValidRequest_ShouldCreateCustomer() throws Exception {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo("CUST-003");
        request.setName("Bob Johnson");
        request.setWechatId("bob_wechat");
        request.setMobileNo("12345678903");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerNo").value("CUST-003"))
                .andExpect(jsonPath("$.name").value("Bob Johnson"))
                .andExpect(jsonPath("$.wechatId").value("bob_wechat"))
                .andExpect(jsonPath("$.mobileNo").value("12345678903"));

        // Verify database state
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(3); // 2 existing + 1 new
        assertThat(customers).anyMatch(c -> c.getName().equals("Bob Johnson"));
    }

    @Test
    void createCustomer_WithDuplicateCustomerNo_ShouldReturnExistingCustomer() throws Exception {
        // Given - Try to create a customer with an existing customerNo
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo("CUST-001"); // Already exists
        request.setName("Different Name");
        request.setWechatId("different_wechat");
        request.setMobileNo("12345678999");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(testCustomer1.getId()))
                .andExpect(jsonPath("$.customerNo").value("CUST-001"))
                .andExpect(jsonPath("$.name").value("John Doe")) // Original name, not the new one
                .andExpect(jsonPath("$.wechatId").value("john_wechat"))
                .andExpect(jsonPath("$.mobileNo").value("12345678901"));

        // Verify database state - no new customer created
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(2); // Still only 2 customers
    }

    @Test
    void createCustomer_WithMissingName_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required name field
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo("CUST-003");
        // name is required but not set
        request.setWechatId("test_wechat");
        request.setMobileNo("12345678903");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createCustomer_WithInvalidMobileNo_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid mobile number format
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNo("CUST-003");
        request.setName("Invalid Mobile Customer");
        request.setWechatId("test_wechat");
        request.setMobileNo("invalid-mobile"); // Invalid format

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.mobileNo").exists());
    }

    @Test
    void getCustomer_WithValidId_ShouldReturnCustomer() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/customers/{id}", testCustomer1.getId()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(testCustomer1.getId()))
                .andExpect(jsonPath("$.customerNo").value("CUST-001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.wechatId").value("john_wechat"))
                .andExpect(jsonPath("$.mobileNo").value("12345678901"));
    }

    @Test
    void getCustomer_WhenCustomerNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/customers/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerNo").value("CUST-001"))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].customerNo").value("CUST-002"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    void getAllCustomers_WhenNoCustomers_ShouldReturnEmptyList() throws Exception {
        // Given
        customerRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteCustomer_WithValidId_ShouldDeleteCustomer() throws Exception {
        // Verify customer exists before deletion
        assertThat(customerRepository.findById(testCustomer1.getId())).isPresent();

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", testCustomer1.getId()))
                .andExpect(status().isNoContent());

        // Verify customer is deleted
        assertThat(customerRepository.findById(testCustomer1.getId())).isEmpty();
        List<Customer> remainingCustomers = customerRepository.findAll();
        assertThat(remainingCustomers).hasSize(1); // Only testCustomer2 remains
    }

    @Test
    void deleteCustomer_WhenCustomerNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    void createCustomer_WithMinimalValidRequest_ShouldCreateCustomer() throws Exception {
        // Given - Minimal valid request
        CustomerRequest request = new CustomerRequest();
        request.setName("Minimal Customer"); // Only required field

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
