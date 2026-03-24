package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import com.github.lucasdengcn.billing.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:feature-access-performance-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=false",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=WARN",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN"
})
class FeatureAccessControllerPerformanceTest {

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

        // Create test customer
        testCustomer = Customer.builder()
                .name("Performance Test Customer")
                .customerNo("PERF-001")
                .mobileNo("1234567890")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test device
        testDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Performance Test Device")
                .deviceNo("PERF-000001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .build();
        testDevice = deviceRepository.save(testDevice);

        // Create test product
        testProduct = Product.builder()
                .productNo("PROD-PLAN001")
                .title("Performance Plan")
                .description("Plan for performance testing")
                .basePrice(new BigDecimal("49.99"))
                .build();
        testProduct = productRepository.save(testProduct);
        
        // Create test product feature
        testFeature = ProductFeature.builder()
                .product(testProduct)
                .featureNo("FEAT-PERF001")
                .title("Performance Test Feature")
                .featureType(FeatureType.API_ACCESS)
                .quota(10000)
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
                .title("Performance Subscription Feature")
                .featureType(FeatureType.API_ACCESS)
                .build();
        testSubscriptionFeature = subscriptionFeatureRepository.save(testSubscriptionFeature);
    }

    @Test
    @DisplayName("Performance test: Single threaded synchronous feature usage tracking")
    void trackFeatureUsage_SingleThreaded_Performance() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo(testDevice.getDeviceNo())
                .productNo(testProduct.getProductNo())
                .featureNo(testFeature.getFeatureNo())
                .usageAmount(1)
                .detailValue("Performance test access")
                .build();

        int iterations = 100;
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < iterations; i++) {
            mockMvc.perform(post("/api/feature/usage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", org.hamcrest.Matchers.is("OK")));
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTimePerRequest = (double) totalTime / iterations;

        System.out.println("Single-threaded synchronous: Total time = " + totalTime + "ms, Avg time per request = " + avgTimePerRequest + "ms");
        
        // Verify all requests were processed
        List<FeatureAccessLog> logs = featureAccessLogRepository.findAll();
        assertThat(logs).hasSize(iterations);
    }

    @Test
    @DisplayName("Performance test: Single threaded asynchronous feature usage tracking")
    void trackFeatureUsageAsync_SingleThreaded_Performance() throws Exception {
        // Given
        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo(testDevice.getDeviceNo())
                .productNo(testProduct.getProductNo())
                .featureNo(testFeature.getFeatureNo())
                .usageAmount(1)
                .detailValue("Async performance test access")
                .build();

        int iterations = 100;
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < iterations; i++) {
            mockMvc.perform(post("/api/feature/usage-async")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$", org.hamcrest.Matchers.is("Accepted")));
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTimePerRequest = (double) totalTime / iterations;

        System.out.println("Single-threaded async: Total time = " + totalTime + "ms, Avg time per request = " + avgTimePerRequest + "ms");
        
        // Note: Async operations are not immediately reflected in DB, so we can't verify exact count here
    }

    @Disabled
    @Test
    @DisplayName("Performance test: Concurrent synchronous feature usage tracking")
    void trackFeatureUsage_Concurrent_Performance() throws Exception {
        // Given
        int numThreads = 10;
        int requestsPerThread = 20;
        int totalRequests = numThreads * requestsPerThread;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                                .deviceNo(testDevice.getDeviceNo())
                                .productNo(testProduct.getProductNo())
                                .featureNo(testFeature.getFeatureNo())
                                .usageAmount(1)
                                .detailValue("Concurrent test access - Thread " + threadNum + " Request " + j)
                                .build();

                        mockMvc.perform(post("/api/feature/usage")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", org.hamcrest.Matchers.is("OK")));
                        
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // Wait for all requests to complete
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTimePerRequest = (double) totalTime / totalRequests;

        System.out.println("Concurrent synchronous: Total time = " + totalTime + "ms, Total successful requests = " + successCount.get() + "/" + totalRequests + ", Avg time per request = " + avgTimePerRequest + "ms");
        
        // Verify results
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(totalRequests);
        
        List<FeatureAccessLog> logs = featureAccessLogRepository.findAll();
        assertThat(logs).hasSize(totalRequests);
    }

    @Test
    @DisplayName("Performance test: Concurrent asynchronous feature usage tracking")
    void trackFeatureUsageAsync_Concurrent_Performance() throws Exception {
        // Given
        int numThreads = 10;
        int requestsPerThread = 20;
        int totalRequests = numThreads * requestsPerThread;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                                .deviceNo(testDevice.getDeviceNo())
                                .productNo(testProduct.getProductNo())
                                .featureNo(testFeature.getFeatureNo())
                                .usageAmount(1)
                                .detailValue("Concurrent async test access - Thread " + threadNum + " Request " + j)
                                .build();

                        mockMvc.perform(post("/api/feature/usage-async")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isAccepted())
                                .andExpect(jsonPath("$", org.hamcrest.Matchers.is("Accepted")));
                        
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // Wait for all requests to complete
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTimePerRequest = (double) totalTime / totalRequests;

        System.out.println("Concurrent async: Total time = " + totalTime + "ms, Total successful requests = " + successCount.get() + "/" + totalRequests + ", Avg time per request = " + avgTimePerRequest + "ms");
        
        // Verify results
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(totalRequests);
    }

    @Disabled
    @Test
    @DisplayName("Performance test: Mixed synchronous and asynchronous requests")
    void trackFeatureUsage_MixedRequests_Performance() throws Exception {
        // Given
        int syncRequests = 50;
        int asyncRequests = 50;
        int totalRequests = syncRequests + asyncRequests;
        
        List<Runnable> tasks = Collections.synchronizedList(new ArrayList<>());
        
        // Add sync requests
        for (int i = 0; i < syncRequests; i++) {
            final int requestId = i;
            tasks.add(() -> {
                try {
                    FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                            .deviceNo(testDevice.getDeviceNo())
                            .productNo(testProduct.getProductNo())
                            .featureNo(testFeature.getFeatureNo())
                            .usageAmount(1)
                            .detailValue("Mixed test sync access - " + requestId)
                            .build();

                    mockMvc.perform(post("/api/feature/usage")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$", org.hamcrest.Matchers.is("OK")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        
        // Add async requests
        for (int i = 0; i < asyncRequests; i++) {
            final int requestId = i;
            tasks.add(() -> {
                try {
                    FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                            .deviceNo(testDevice.getDeviceNo())
                            .productNo(testProduct.getProductNo())
                            .featureNo(testFeature.getFeatureNo())
                            .usageAmount(1)
                            .detailValue("Mixed test async access - " + requestId)
                            .build();

                    mockMvc.perform(post("/api/feature/usage-async")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isAccepted())
                            .andExpect(jsonPath("$", org.hamcrest.Matchers.is("Accepted")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        
        // Shuffle the tasks to mix sync and async requests
        Collections.shuffle(tasks);
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();

        // Execute all tasks concurrently
        for (Runnable task : tasks) {
            executor.submit(() -> {
                try {
                    task.run();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all requests to complete
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTimePerRequest = (double) totalTime / totalRequests;

        System.out.println("Mixed requests: Total time = " + totalTime + "ms, Total successful requests = " + successCount.get() + "/" + totalRequests + ", Avg time per request = " + avgTimePerRequest + "ms");
        
        // Verify results
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(totalRequests);
        
        // For sync requests, we can verify the logs were created
        List<FeatureAccessLog> logs = featureAccessLogRepository.findAll();
        assertThat(logs).hasSize(syncRequests); // Only sync requests create immediate logs
    }
}