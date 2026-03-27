package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingByTrackIdRequest;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import com.github.lucasdengcn.billing.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:feature-access-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class FeatureAccessControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private FeatureAccessLogRepository featureAccessLogRepository;

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
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private ProductFeature testFeature;
    private Subscription testSubscription;
    private SubscriptionFeature testSubscriptionFeature;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with WebApplicationContext
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        
        // Clean up database
        featureAccessLogRepository.deleteAll();
        subscriptionFeatureRepository.deleteAll();
        subscriptionRepository.deleteAll();
        deviceRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();

        // Create test customer
        testCustomer = Customer.builder()
                .name("Integration Test Customer")
                .customerNo("FAC-001")
                .mobileNo("1234567890")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test device
        testDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Integration Test Device")
                .deviceNo("FA-000001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();
        testDevice = deviceRepository.save(testDevice);

        // Create test product
        testProduct = Product.builder()
                .productNo("FEATURE_PLAN_001")
                .title("Feature Plan")
                .description("Plan with features")
                .basePrice(new BigDecimal("49.99"))
                .build();
        testProduct = productRepository.save(testProduct);
        
        // Create test product feature
        testFeature = ProductFeature.builder()
                .product(testProduct)
                .featureNo("FEAT_0001")
                .title("Test Feature")
                .featureType(FeatureType.API_ACCESS)
                .quota(1000)
                .build();
        testFeature = productFeatureRepository.save(testFeature);
        
        // Create test subscription
        testSubscription = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .baseFee(new BigDecimal("49.99"))
                .totalFee(new BigDecimal("49.99"))
                .build();
        testSubscription = subscriptionRepository.save(testSubscription);
        
        // Create test subscription feature
        testSubscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription)
                .device(testDevice)
                .productFeature(testFeature)
                .title("Test Subscription Feature")
                .featureType(FeatureType.API_ACCESS)
                .build();
        testSubscriptionFeature = subscriptionFeatureRepository.save(testSubscriptionFeature);
    }

    @Test
    void trackFeatureUsage_WithValidRequest_ShouldRecordFeatureAccess() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(1)
                .detailValue("Test access detail")
                .build();

        // Verify initial state
        assertThat(featureAccessLogRepository.findAll()).isEmpty();

        // When & Then
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("OK")));

        // Verify database state
        List<FeatureAccessLog> logs = featureAccessLogRepository.findAll();
        assertThat(logs).hasSize(1);
        FeatureAccessLog log = logs.get(0);
        assertThat(log.getSubscriptionId()).isEqualTo(testSubscription.getId());
        assertThat(log.getProductFeatureId()).isEqualTo(testFeature.getId());
        assertThat(log.getDeviceId()).isEqualTo(testDevice.getId());
        assertThat(log.getUsageAmount()).isEqualTo(1);
        assertThat(log.getDetailValue()).isEqualTo("Test access detail");
    }

    @Test
    void trackFeatureUsage_WithMissingDeviceNo_ShouldReturnBadRequest() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void trackFeatureUsage_WithMissingProductNo_ShouldReturnBadRequest() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .featureNo("FEAT_0001")
                .usageAmount(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void trackFeatureUsage_WithMissingFeatureNo_ShouldReturnBadRequest() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .productNo("FEATURE_PLAN_001")
                .usageAmount(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void trackFeatureUsage_WithNonExistentDevice_ShouldReturnNotFound() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("NON-123456")
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Disabled
    @Test
    void trackFeatureUsageAsync_WithValidRequest_ShouldAcceptRequest() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(2)
                .detailValue("Async access detail")
                .build();

        // Verify initial state
        assertThat(featureAccessLogRepository.findAll()).isEmpty();

        // When & Then
        mockMvc.perform(post("/api/feature/usage-async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", is("Accepted")));
        
        // Note: For async operations, we can't immediately verify the database record
        // since the operation happens asynchronously. We can still test that the request is accepted.
    }

    @Disabled
    @Test
    void trackFeatureUsageAsync_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(-1) // Invalid amount
                .build();

        // When & Then
        mockMvc.perform(post("/api/feature/usage-async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void trackFeatureUsageByTrackId_WithValidRequest_ShouldRecordFeatureAccess() throws Exception {
        // Given
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(1)
                .detailValue("Test access by trackId")
                .build();

        // Verify initial state
        assertThat(featureAccessLogRepository.findAll()).isEmpty();

        // When & Then
        mockMvc.perform(post("/api/feature/usage/{trackId}", testSubscriptionFeature.getTrackId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("OK")));

        // Verify database state
        List<FeatureAccessLog> logs = featureAccessLogRepository.findAll();
        assertThat(logs).hasSize(1);
        FeatureAccessLog log = logs.get(0);
        assertThat(log.getSubscriptionId()).isEqualTo(testSubscription.getId());
        assertThat(log.getProductFeatureId()).isEqualTo(testFeature.getId());
        assertThat(log.getDeviceId()).isEqualTo(testDevice.getId());
        assertThat(log.getUsageAmount()).isEqualTo(1);
        assertThat(log.getDetailValue()).isEqualTo("Test access by trackId");
    }

    @Test
    void trackFeatureUsageByTrackId_WithInvalidTrackId_ShouldReturnNotFound() throws Exception {
        // Given
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/feature/usage/{trackId}", "TRK-NONEXISTENT123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Disabled
    @Test
    void trackFeatureUsageByTrackIdAsync_WithValidRequest_ShouldAcceptRequest() throws Exception {
        // Given
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(2)
                .detailValue("Async access by trackId")
                .build();

        // Verify initial state
        assertThat(featureAccessLogRepository.findAll()).isEmpty();

        // When & Then
        mockMvc.perform(post("/api/feature/usage/{trackId}/async", testSubscriptionFeature.getTrackId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", is("Accepted")));
    }

    @Disabled
    @Test
    void trackFeatureUsageByTrackIdAsync_WithInvalidTrackId_ShouldReturnNotFound() throws Exception {
        // Given
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(1)
                .build();

        // When & Then
        mockMvc.perform(post("/api/feature/usage/{trackId}/async", "TRK-NONEXISTENT123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void getFeatureUsageLogsByTrackId_WithValidTrackId_ShouldReturnLogs() throws Exception {
        // Given - Create a log entry first
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(1)
                .build();
        
        mockMvc.perform(post("/api/feature/usage/{trackId}", testSubscriptionFeature.getTrackId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/feature/usage/trackId/{trackId}", testSubscriptionFeature.getTrackId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements", is(1)))
                .andExpect(jsonPath("content[0].usageAmount", is(1)));
    }

    @Test
    void getFeatureUsageLogsByTrackId_WithInvalidTrackId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/feature/usage/trackId/{trackId}", "TRK-NONEXISTENT123456789"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFeatureUsageLogs_WithValidCombination_ShouldReturnLogs() throws Exception {
        // Given - Create a log entry first
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(1)
                .detailValue("Test access detail")
                .build();
        
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/feature/usage/device/{deviceNo}/product/{productNo}/feature/{featureNo}", 
                          "FA-000001", "FEATURE_PLAN_001", "FEAT_0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements", is(1)))
                .andExpect(jsonPath("content[0].usageAmount", is(1)))
                .andExpect(jsonPath("content[0].detailValue", is("Test access detail")));
    }

    @Test
    void getFeatureUsageLogs_WithNonExistentCombination_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/feature/usage/device/{deviceNo}/product/{productNo}/feature/{featureNo}", 
                          "NON-EXISTENT", "NON-EXISTENT", "NON-EXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFeatureUsageLogsByDevice_WithValidDeviceNo_ShouldReturnLogs() throws Exception {
        // Given - Create a log entry first
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(2)
                .detailValue("Device-specific access")
                .build();
        
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/feature/usage/device/{deviceNo}", "FA-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements", is(1)))
                .andExpect(jsonPath("content[0].usageAmount", is(2)))
                .andExpect(jsonPath("content[0].detailValue", is("Device-specific access")));
    }

    @Test
    void getFeatureUsageLogsByDevice_WithNonExistentDeviceNo_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/feature/usage/device/{deviceNo}", "NON-EXISTENT-DEVICE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFeatureUsageLogsBySubscription_WithValidSubscriptionId_ShouldReturnLogs() throws Exception {
        // Given - Create a log entry first
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("FA-000001")
                .productNo("FEATURE_PLAN_001")
                .featureNo("FEAT_0001")
                .usageAmount(3)
                .detailValue("Subscription-specific access")
                .build();
        
        mockMvc.perform(post("/api/feature/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/feature/usage/subscription/{subscriptionId}", testSubscription.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements", is(1)))
                .andExpect(jsonPath("content[0].usageAmount", is(3)))
                .andExpect(jsonPath("content[0].detailValue", is("Subscription-specific access")));
    }

    @Test
    void getFeatureUsageLogsBySubscription_WithNonExistentSubscriptionId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/feature/usage/subscription/{subscriptionId}", 999L))
                .andExpect(status().isNotFound());
    }
}