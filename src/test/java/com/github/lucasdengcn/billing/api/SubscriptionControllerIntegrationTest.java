package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import com.github.lucasdengcn.billing.repository.ProductRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:subscription-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class SubscriptionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with WebApplicationContext
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // Clean up database
        subscriptionRepository.deleteAll();
        deviceRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();

        // Create test customer
        testCustomer = Customer.builder()
                .name("Integration Test Customer")
                .customerNo("SUB-001")
                .mobileNo("1234567890")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test device
        testDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Integration Test Device")
                .deviceNo("IT-000001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();
        testDevice = deviceRepository.save(testDevice);

        // Create test product
        testProduct = Product.builder()
                .title("Premium Plan")
                .description("{\"tier\":\"premium\",\"support\":\"24/7\"}")
                .basePrice(new BigDecimal("59.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create another test product
        testProduct2 = Product.builder()
                .title("Basic Plan")
                .description("{\"tier\":\"basic\",\"support\":\"business hours\"}")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.YEARLY)
                .discountRate(new BigDecimal("1.00"))
                .discountStatus(DiscountStatus.INACTIVE)
                .build();
        testProduct2 = productRepository.save(testProduct2);
    }

    @Test
    void createSubscription_WithValidRequest_ShouldCreateSubscription() throws Exception {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusDays(1));
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.deviceId").value(testDevice.getId()))
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.baseFee").value(59.99))
                .andExpect(jsonPath("$.discountRate").value((0.90)))
                .andExpect(jsonPath("$.totalFee").value((53.99))) // 59.99 * 0.90
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.getValue()));

        // Verify database state
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        assertThat(subscriptions).hasSize(1);
        Subscription savedSubscription = subscriptions.get(0);
        assertThat(savedSubscription.getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(savedSubscription.getDevice().getId()).isEqualTo(testDevice.getId());
        assertThat(savedSubscription.getProduct().getId()).isEqualTo(testProduct.getId());
        assertThat(savedSubscription.getBaseFee()).isEqualByComparingTo(new BigDecimal("59.99"));
        assertThat(savedSubscription.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(savedSubscription.getTotalFee()).isEqualByComparingTo(new BigDecimal("53.99"));
        assertThat(savedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void createSubscription_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required customer ID
        SubscriptionRequest request = new SubscriptionRequest();
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerId").exists());
    }

    @Test
    void createSubscription_WithInvalidCustomer_ShouldReturnNotFound() throws Exception {
        // Given - Non-existent customer ID
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(999L); // Non-existent customer
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    void createSubscription_WithInvalidDevice_ShouldReturnNotFound() throws Exception {
        // Given - Non-existent device ID
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(999L); // Non-existent device
        request.setProductId(testProduct.getId());

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Device not found with id: 999"));
    }

    @Test
    void createSubscription_WithInvalidProduct_ShouldReturnNotFound() throws Exception {
        // Given - Non-existent product ID
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(999L); // Non-existent product

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void getSubscription_WithValidId_ShouldReturnSubscription() throws Exception {
        // Given - Create a subscription first
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusDays(1));
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create the subscription via API
        String response = mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract the ID from the response
        Long subscriptionId = objectMapper.readTree(response).get("id").asLong();

        // When & Then - Get the subscription
        mockMvc.perform(get("/api/subscriptions/{id}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId))
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.deviceId").value(testDevice.getId()))
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.baseFee").value((59.99)))
                .andExpect(jsonPath("$.discountRate").value((0.90)))
                .andExpect(jsonPath("$.totalFee").value((53.99)))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.getValue()));
    }

    @Test
    void getSubscription_WhenSubscriptionNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscriptions/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Subscription not found with id: 999"));
    }

    @Test
    void getCustomerSubscriptions_WithValidCustomerId_ShouldReturnSubscriptions() throws Exception {
        // Given - Create multiple subscriptions for the customer
        SubscriptionRequest request1 = new SubscriptionRequest();
        request1.setCustomerId(testCustomer.getId());
        request1.setDeviceId(testDevice.getId());
        request1.setProductId(testProduct.getId());
        request1.setStartDate(OffsetDateTime.now().plusDays(1));
        request1.setEndDate(OffsetDateTime.now().plusMonths(1));

        SubscriptionRequest request2 = new SubscriptionRequest();
        request2.setCustomerId(testCustomer.getId());
        request2.setDeviceId(testDevice.getId());
        request2.setProductId(testProduct2.getId());
        request2.setStartDate(OffsetDateTime.now().plusDays(1));
        request2.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create both subscriptions
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/subscriptions/customer/{customerId}", testCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$[1].customerId").value(testCustomer.getId()));
    }

    @Test
    void getCustomerSubscriptions_WhenCustomerNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscriptions/customer/{customerId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    void createSubscription_WithPastStartDate_ShouldReturnBadRequest() throws Exception {
        // Given - Start date in the past (violates @FutureOrPresent constraint)
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().minusDays(1)); // Past date
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.startDate").exists());
    }

    @Test
    void createSubscription_WithInvalidDateRange_ShouldFailValidation() throws Exception {
        // Given - End date before start date
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusMonths(2));
        request.setEndDate(OffsetDateTime.now().plusMonths(1)); // End date before start date

        // When & Then - This should fail during business logic validation
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSubscription_WithMinimalValidRequest_ShouldCreateSubscription() throws Exception {
        // Given - Only required fields
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        // Start and end dates are optional

        // When & Then
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.deviceId").value(testDevice.getId()))
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.getValue()));
    }

    @Test
    void getCustomerSubscriptions_WhenCustomerHasNoSubscriptions_ShouldReturnEmptyList() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscriptions/customer/{customerId}", testCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}