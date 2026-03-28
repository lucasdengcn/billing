package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.SubscriptionRenewalEstimateRequest;
import com.github.lucasdengcn.billing.repository.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:subscription-estimate-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class SubscriptionControllerEstimateRenewalFeeTest {

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
                .name("Estimate Test Customer")
                .customerNo("EST-001")
                .mobileNo("1234567890")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test device
        testDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Estimate Test Device")
                .deviceNo("EST-000001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();
        testDevice = deviceRepository.save(testDevice);

        // Create test product
        testProduct = Product.builder()
                .productNo("ESTIMATE_PLAN_001")
                .title("Estimate Plan")
                .description("{\"tier\":\"estimate\",\"support\":\"24/7\"}")
                .basePrice(new BigDecimal("59.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create a subscription for this customer, device, and product
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .baseFee(new BigDecimal("59.99"))
                .discountRate(new BigDecimal("0.90"))
                .totalFee(new BigDecimal("53.99"))
                .build();
        subscriptionRepository.save(subscription);
    }

    @Test
    void estimateRenewalFee_WithValidRequest_ShouldReturnEstimatedFee() throws Exception {
        // Given
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("EST-000001");
        request.setProductNo("ESTIMATE_PLAN_001");
        request.setRenewalPeriods(3);

        // When & Then
        mockMvc.perform(post("/api/subscriptions/renew/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFee").value(161.97)) // 59.99 * 0.90 * 3 = 161.973 -> rounded to 161.97
                .andExpect(jsonPath("$.baseFee").value(59.99))
                .andExpect(jsonPath("$.discountRate").value(0.90))
                .andExpect(jsonPath("$.renewalPeriods").value(3))
                .andExpect(jsonPath("$.productTitle").value("Estimate Plan"))
                .andExpect(jsonPath("$.deviceNo").value("EST-000001"))
                .andExpect(jsonPath("$.productNo").value("ESTIMATE_PLAN_001"));
    }

    @Test
    void estimateRenewalFee_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required fields
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        // Not setting deviceNo, productNo, or renewalPeriods

        // When & Then
        mockMvc.perform(post("/api/subscriptions/renew/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.deviceNo").exists())
                .andExpect(jsonPath("$.errors.productNo").exists())
                .andExpect(jsonPath("$.errors.renewalPeriods").exists());
    }

    @Test
    void estimateRenewalFee_WithNonExistentDevice_ShouldReturnNotFound() throws Exception {
        // Given - Non-existent device
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("NON-EXISTENT-DEVICE");
        request.setProductNo("ESTIMATE_PLAN_001");
        request.setRenewalPeriods(1);

        // When & Then
        mockMvc.perform(post("/api/subscriptions/renew/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void estimateRenewalFee_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
        // Given - Non-existent product
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("EST-000001");
        request.setProductNo("NON-EXISTENT-PRODUCT");
        request.setRenewalPeriods(1);

        // When & Then
        mockMvc.perform(post("/api/subscriptions/renew/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void estimateRenewalFee_WithNonExistentSubscription_ShouldReturnNotFound() throws Exception {
        // Given - Create a device and product but no subscription between them
        Device otherDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Other Test Device")
                .deviceNo("OTHER-000001")
                .deviceType("DESKTOP")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();
        otherDevice = deviceRepository.save(otherDevice);

        // Use the other device with existing product (no subscription exists between them)
        SubscriptionRenewalEstimateRequest request = new SubscriptionRenewalEstimateRequest();
        request.setDeviceNo("OTHER-000001");
        request.setProductNo("ESTIMATE_PLAN_001");
        request.setRenewalPeriods(1);

        // When & Then
        mockMvc.perform(post("/api/subscriptions/renew/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
