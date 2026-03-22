package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionFeatureResponse;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
import com.github.lucasdengcn.billing.repository.ProductFeatureRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
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
    private ProductFeatureRepository productFeatureRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private Product testProduct2;
    private ProductFeature testFeature1;
    private ProductFeature testFeature2;

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
                .productNo("PREMIUM_PLAN_001")
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
                .productNo("BASIC_PLAN_001")
                .title("Basic Plan")
                .description("{\"tier\":\"basic\",\"support\":\"business hours\"}")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.YEARLY)
                .discountRate(new BigDecimal("1.00"))
                .discountStatus(DiscountStatus.INACTIVE)
                .build();
        testProduct2 = productRepository.save(testProduct2);
        
        // Create test product features
        testFeature1 = ProductFeature.builder()
                .product(testProduct)
                .featureNo("FEAT_0001")
                .title("Storage Feature")
                .description("Additional storage capacity")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(1000)
                .build();
        testFeature1 = productFeatureRepository.save(testFeature1);
        
        testFeature2 = ProductFeature.builder()
                .product(testProduct)
                .featureNo("FEAT_0002")
                .title("API Access")
                .description("API access allowance")
                .featureType(FeatureType.API_ACCESS)
                .quota(5000)
                .build();
        testFeature2 = productFeatureRepository.save(testFeature2);
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

    @Test
    void cancelSubscription_WithValidIds_ShouldCancelSubscription2() throws Exception {
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

        // Verify the subscription is initially active
        mockMvc.perform(get("/api/subscriptions/{id}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.getValue()));

        // Prepare cancel request
        com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest cancelRequest = 
            new com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest();
        cancelRequest.setCustomerId(testCustomer.getId());
        cancelRequest.setDeviceId(testDevice.getId());
        cancelRequest.setProductId(testProduct.getId());

        // When & Then - Cancel the subscription
        mockMvc.perform(post("/api/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.CANCELLED.getValue()));

        // Verify the subscription is now cancelled in the database
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        assertThat(subscriptions).hasSize(1);
        Subscription cancelledSubscription = subscriptions.get(0);
        assertThat(cancelledSubscription.getId()).isEqualTo(subscriptionId);
        assertThat(cancelledSubscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    void cancelSubscription_WhenSubscriptionDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Prepare cancel request with non-existent IDs
        com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest cancelRequest = 
            new com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest();
        cancelRequest.setCustomerId(999L);
        cancelRequest.setDeviceId(999L);
        cancelRequest.setProductId(999L);

        // When & Then
        mockMvc.perform(post("/api/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void cancelSubscription_WhenCustomerDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given - Create a subscription first
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusDays(1));
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create the subscription via API
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Prepare cancel request with non-existent customer ID
        com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest cancelRequest = 
            new com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest();
        cancelRequest.setCustomerId(999L);
        cancelRequest.setDeviceId(testDevice.getId());
        cancelRequest.setProductId(testProduct.getId());

        // When & Then
        mockMvc.perform(post("/api/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    void cancelSubscription_WithValidIds_ShouldCancelSubscription() throws Exception {
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

        // Extract the ID of the created subscription
        Long subscriptionId = objectMapper.readTree(response).get("id").asLong();

        // Prepare cancel request
        com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest cancelRequest = 
            new com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest();
        cancelRequest.setCustomerId(testCustomer.getId());
        cancelRequest.setDeviceId(testDevice.getId());
        cancelRequest.setProductId(testProduct.getId());

        // When & Then - Cancel the subscription
        mockMvc.perform(post("/api/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.CANCELLED.getValue()));
    }

    @Test
    void cancelSubscription_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request with missing customer ID
        com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest cancelRequest = 
            new com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest();
        // Don't set customer ID to trigger validation
        cancelRequest.setDeviceId(testDevice.getId());
        cancelRequest.setProductId(testProduct.getId());

        // When & Then
        mockMvc.perform(post("/api/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerId").exists());
    }

    @Test
    void cancelSubscription_WithNegativeIds_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request with negative customer ID
        com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest cancelRequest = 
            new com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest();
        cancelRequest.setCustomerId(-1L);
        cancelRequest.setDeviceId(testDevice.getId());
        cancelRequest.setProductId(testProduct.getId());

        // When & Then
        mockMvc.perform(post("/api/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerId").value("Customer ID must be a positive number"));
    }

    @Test
    void getSubscriptionsByDeviceNo_WithValidDeviceNo_ShouldReturnSubscriptions() throws Exception {
        // Given - Create a subscription first
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusDays(1));
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create the subscription via API
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}", testDevice.getDeviceNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$[0].deviceId").value(testDevice.getId()))
                .andExpect(jsonPath("$[0].productId").value(testProduct.getId()));
    }

    @Test
    void getSubscriptionsByDeviceNo_WithValidDeviceNoAndMultipleSubscriptions_ShouldReturnAllSubscriptions() throws Exception {
        // Given - Create multiple subscriptions for the same device
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
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}", testDevice.getDeviceNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].deviceId").value(everyItem(equalTo(testDevice.getId().intValue()))));
    }

    @Test
    void getSubscriptionsByDeviceNo_WhenDeviceHasNoSubscriptions_ShouldReturnEmptyList() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}", testDevice.getDeviceNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getSubscriptionsByDeviceNo_WhenDeviceDoesNotExist_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}", "NONEXISTENT_DEVICE_NO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Device not found with deviceNo: NONEXISTENT_DEVICE_NO"));
    }

    @Test
    void getSubscriptionsByDeviceNo_WithSpecialCharactersInDeviceNo_ShouldWork() throws Exception {
        // Given - Create a device with special characters in device number
        Device specialDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Special Device")
                .deviceNo("DEV-2026-ABC_123")
                .deviceType("TABLET")
                .status(DeviceStatus.ACTIVE)
                .build();
        specialDevice = deviceRepository.save(specialDevice);

        // Create a subscription for the special device
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(specialDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusDays(1));
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}", "DEV-2026-ABC_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deviceId").value(specialDevice.getId()));
    }

    @Test
    void getSubscriptionByDeviceNoAndProductNo_WithValidDeviceNoAndProductNo_ShouldReturnSubscription() throws Exception {
        // Given - Create a subscription first
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusDays(1));
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create the subscription via API
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}/product/{productNo}", 
                           testDevice.getDeviceNo(), testProduct.getProductNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.deviceId").value(testDevice.getId()))
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.baseFee").value((59.99)))
                .andExpect(jsonPath("$.discountRate").value((0.90)))
                .andExpect(jsonPath("$.totalFee").value((53.99)))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.getValue()))
                .andExpect(jsonPath("$.subscriptionFeatures").isArray());
    }

    @Test
    void getSubscriptionByDeviceNoAndProductNo_WhenNoMatchingSubscriptions_ShouldReturnNotFound() throws Exception {
        // Given - Create a subscription for a different product
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setDeviceId(testDevice.getId());
        request.setProductId(testProduct.getId());
        request.setStartDate(OffsetDateTime.now().plusDays(1));
        request.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create the subscription via API
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then - Try to get subscription for the same device but different product
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}/product/{productNo}", 
                           testDevice.getDeviceNo(), testProduct2.getProductNo()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No active subscription found for device number: " + testDevice.getDeviceNo() + " and product number: " + testProduct2.getProductNo()));
    }

    @Test
    void getSubscriptionByDeviceNoAndProductNo_WhenDeviceDoesNotExist_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}/product/{productNo}", 
                           "NONEXISTENT_DEVICE", testProduct.getProductNo()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSubscriptionByDeviceNoAndProductNo_WhenProductDoesNotExist_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}/product/{productNo}", 
                           testDevice.getDeviceNo(), "NONEXISTENT_PRODUCT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSubscription_WhenDeviceAlreadyHasActiveSubscriptionToSameProduct_ShouldReturnBadRequest() throws Exception {
        // Given - Create a subscription first
        SubscriptionRequest firstRequest = new SubscriptionRequest();
        firstRequest.setCustomerId(testCustomer.getId());
        firstRequest.setDeviceId(testDevice.getId());
        firstRequest.setProductId(testProduct.getId());
        firstRequest.setStartDate(OffsetDateTime.now().plusDays(1));
        firstRequest.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create the first subscription
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // When - Try to create another subscription to the same product for the same device
        SubscriptionRequest secondRequest = new SubscriptionRequest();
        secondRequest.setCustomerId(testCustomer.getId());
        secondRequest.setDeviceId(testDevice.getId());
        secondRequest.setProductId(testProduct.getId());
        secondRequest.setStartDate(OffsetDateTime.now().plusDays(2));
        secondRequest.setEndDate(OffsetDateTime.now().plusMonths(2));

        // Then - Should return bad request
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Device " + testDevice.getId() + " already has an active subscription to product " + testProduct.getId()));
    }

    @Test
    void getSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WithNonExistentIdentifiers_ShouldReturnNotFound() throws Exception {
        // When & Then - Try to get subscription feature with non-existent identifiers
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}/product/{productNo}/feature/{featureNo}", 
                        "NONEXISTENT_DEVICE", "NONEXISTENT_FEATURE", "NONEXISTENT_PRODUCT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WithValidIdentifiers_ShouldReturnSubscriptionFeature() throws Exception {
        // Given - Create a subscription first (this should trigger creation of subscription features)
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setCustomerId(testCustomer.getId());
        subscriptionRequest.setDeviceId(testDevice.getId());
        subscriptionRequest.setProductId(testProduct.getId());
        subscriptionRequest.setStartDate(OffsetDateTime.now().plusDays(1));
        subscriptionRequest.setEndDate(OffsetDateTime.now().plusMonths(1));

        // Create the subscription via API
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionRequest)))
                .andExpect(status().isOk());

        // Wait briefly to ensure subscription features are created
        Thread.sleep(100);
        
        // When & Then - Get subscription feature by deviceNo, featureNo, and productNo
        // extract response content as JSON
        mockMvc.perform(get("/api/subscriptions/device/{deviceNo}/product/{productNo}/feature/{featureNo}", 
                        testDevice.getDeviceNo(), testProduct.getProductNo(), testFeature1.getFeatureNo()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.quota").value(testFeature1.getQuota()))
                .andExpect(jsonPath("$.accessed").exists())
                .andExpect(jsonPath("$.balance").exists())
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        String responseJson = result.getResponse().getContentAsString();
                        SubscriptionFeatureResponse response = objectMapper.readValue(responseJson, SubscriptionFeatureResponse.class);
                        boolean ok = response.getBalance() > 0;
                        assertThat(response.getBalanceSufficient()).isEqualTo(ok);
                    }
                });

    }
}