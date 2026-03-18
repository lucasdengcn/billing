package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.model.request.CustomerInfo;
import com.github.lucasdengcn.billing.model.request.DeviceBatchRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceUpdateRequest;
import com.github.lucasdengcn.billing.repository.CustomerRepository;
import com.github.lucasdengcn.billing.repository.DeviceRepository;
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
        "spring.datasource.url=jdbc:h2:mem:device-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class DeviceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer testCustomer;
    private Device testDevice1;
    private Device testDevice2;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with WebApplicationContext
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        
        // Clean up database
        deviceRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        testCustomer = Customer.builder()
                .name("Integration Test Customer")
                .customerNo("INTEG001")
                .mobileNo("1234567890")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test devices
        testDevice1 = Device.builder()
                .customer(testCustomer)
                .deviceName("Integration Test Device 1")
                .deviceNo("IT-000001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .lastActivityAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        testDevice1 = deviceRepository.save(testDevice1);

        testDevice2 = Device.builder()
                .customer(testCustomer)
                .deviceName("Integration Test Device 2")
                .deviceNo("IT-000002")
                .deviceType("TABLET")
                .status(DeviceStatus.INACTIVE)
                .lastActivityAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now())
                .build();
        testDevice2 = deviceRepository.save(testDevice2);
    }

    @Test
    void createDevice_WithNewCustomer_ShouldCreateDeviceAndCustomer() throws Exception {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setName("New Integration Customer");
        customerInfo.setCustomerNo("XX-123456");

        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setCustomer(customerInfo);
        request.setDeviceName("New Integration Device");
        request.setDeviceNo("NI-000003");
        request.setDeviceType("DESKTOP");
        request.setStatus(DeviceStatus.ACTIVE);

        // When & Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.deviceName").value("New Integration Device"))
                .andExpect(jsonPath("$.deviceNo").value("NI-000003"))
                .andExpect(jsonPath("$.deviceType").value("DESKTOP"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify database state
        List<Device> devices = deviceRepository.findAll();
        assertThat(devices).hasSize(3); // 2 existing + 1 new
        assertThat(devices).anyMatch(d -> d.getDeviceNo().equals("NI-000003"));
    }

    @Test
    void createDevice_WithExistingCustomer_ShouldCreateDevice() throws Exception {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(testCustomer.getId());

        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setCustomer(customerInfo);
        request.setDeviceName("Device for Existing Customer");
        request.setDeviceNo("EC-000004");
        request.setDeviceType("MOBILE");
        request.setStatus(DeviceStatus.ACTIVE);

        // When & Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceNo").value("EC-000004"))
                .andExpect(jsonPath("$.deviceName").value("Device for Existing Customer"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify device is associated with correct customer
        Device savedDevice = deviceRepository.findByDeviceNo("EC-000004").orElseThrow();
        assertThat(savedDevice.getCustomer().getId()).isEqualTo(testCustomer.getId());
    }

    @Test
    void createDevice_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required deviceNo
        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setDeviceName("Invalid Device");
        // deviceNo is required but not set

        // When & Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void batchCreateDevices_ShouldCreateMultipleDevices() throws Exception {
        // Given
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(testCustomer.getId());

        DeviceUpdateRequest device1 = new DeviceUpdateRequest();
        device1.setDeviceName("Batch Device 1");
        device1.setDeviceNo("BD-000005");
        device1.setDeviceType("MOBILE");
        device1.setStatus(DeviceStatus.ACTIVE);

        DeviceUpdateRequest device2 = new DeviceUpdateRequest();
        device2.setDeviceName("Batch Device 2");
        device2.setDeviceNo("BD-000006");
        device2.setDeviceType("TABLET");
        device2.setStatus(DeviceStatus.ACTIVE);

        DeviceBatchRegisterRequest request = new DeviceBatchRegisterRequest();
        request.setCustomer(customerInfo);
        request.setDevices(List.of(device1, device2));

        // When & Then
        mockMvc.perform(post("/api/devices/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceNo").value("BD-000005"))
                .andExpect(jsonPath("$[1].deviceNo").value("BD-000006"));

        // Verify database state
        List<Device> devices = deviceRepository.findAll();
        assertThat(devices).hasSize(4); // 2 existing + 2 new
    }

    @Test
    void updateDevice_ShouldUpdateDeviceDetails() throws Exception {
        // Given
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Updated Device Name");
        request.setDeviceNo("UD-000007");
        request.setDeviceType("UPDATED-TYPE");
        request.setStatus(DeviceStatus.SUSPENDED);

        // When & Then
        mockMvc.perform(put("/api/devices/{id}", testDevice1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceName").value("Updated Device Name"))
                .andExpect(jsonPath("$.deviceNo").value("UD-000007"))
                .andExpect(jsonPath("$.deviceType").value("UPDATED-TYPE"))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        // Verify database update
        Device updatedDevice = deviceRepository.findById(testDevice1.getId()).orElseThrow();
        assertThat(updatedDevice.getDeviceName()).isEqualTo("Updated Device Name");
        assertThat(updatedDevice.getDeviceNo()).isEqualTo("UD-000007");
        assertThat(updatedDevice.getStatus()).isEqualTo(DeviceStatus.SUSPENDED);
    }

    @Test
    void updateDevice_WhenDeviceNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("Non-existent Device");
        request.setDeviceNo("NE-000008");
        request.setStatus(DeviceStatus.ACTIVE);

        // When & Then
        mockMvc.perform(put("/api/devices/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Device not found with id: 999"));
    }

    @Test
    void activateDevice_ShouldActivateDevice() throws Exception {
        // Given - Capture the original state from database
        Device originalDevice = deviceRepository.findByDeviceNo(testDevice2.getDeviceNo()).orElseThrow();
        OffsetDateTime originalDeviceUpdatedAt = originalDevice.getUpdatedAt();
        DeviceStatus originalStatus = originalDevice.getStatus();

        // Verify initial state
        assertThat(originalStatus).isEqualTo(DeviceStatus.INACTIVE);

        // When & Then
        mockMvc.perform(post("/api/devices/activate/{deviceNo}", testDevice2.getDeviceNo()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify database state - retrieve a fresh instance from database
        Device activatedDevice = deviceRepository.findByDeviceNo(testDevice2.getDeviceNo()).orElseThrow();

        // Verify the device was properly activated
        assertThat(activatedDevice.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(activatedDevice.getUpdatedAt()).isAfter(originalDeviceUpdatedAt);

        // Verify the state changed
        assertThat(originalStatus).isNotEqualTo(activatedDevice.getStatus());
    }

    @Test
    void activateDevice_WhenDeviceNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/devices/activate/{deviceNo}", "NON-EXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Device not found with deviceNo: NON-EXISTENT"));
    }

    @Test
    void deactivateDevice_ShouldDeactivateDevice() throws Exception {
        // Given - Capture the original state from database
        Device originalDevice = deviceRepository.findByDeviceNo(testDevice1.getDeviceNo()).orElseThrow();
        OffsetDateTime originalDeviceUpdatedAt = originalDevice.getUpdatedAt();
        DeviceStatus originalStatus = originalDevice.getStatus();

        // Verify initial state
        assertThat(originalStatus).isEqualTo(DeviceStatus.ACTIVE);

        // When & Then
        mockMvc.perform(patch("/api/devices/deactivate/{deviceNo}", testDevice1.getDeviceNo()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.status").value("DEACTIVATED"));

        // Verify database state - retrieve a fresh instance from database
        Device deactivatedDevice = deviceRepository.findByDeviceNo(testDevice1.getDeviceNo()).orElseThrow();

        // Verify the device was properly deactivated
        assertThat(deactivatedDevice.getStatus()).isEqualTo(DeviceStatus.DEACTIVATED);
        assertThat(deactivatedDevice.getUpdatedAt()).isAfter(originalDeviceUpdatedAt);

        // Verify the state changed
        assertThat(originalStatus).isNotEqualTo(deactivatedDevice.getStatus());

    }

    @Test
    void deactivateDevice_WhenDeviceNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/devices/deactivate/{deviceNo}", "NON-EXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Device not found with deviceNo: NON-EXISTENT"));
    }

    @Test
    void getDeviceById_ShouldReturnDevice() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/devices/{id}", testDevice1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDevice1.getId()))
                .andExpect(jsonPath("$.deviceName").value(testDevice1.getDeviceName()))
                .andExpect(jsonPath("$.deviceNo").value(testDevice1.getDeviceNo()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getDeviceById_WhenDeviceNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/devices/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Device not found with id: 999"));
    }

    @Test
    void getCustomerDevices_ShouldReturnCustomerDevices() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/devices/customer/{customerId}", testCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceNo").value(testDevice1.getDeviceNo()))
                .andExpect(jsonPath("$[1].deviceNo").value(testDevice2.getDeviceNo()));
    }

    @Test
    void getCustomerDevices_WhenCustomerNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/devices/customer/{customerId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDevices_ShouldReturnAllDevices() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceNo").value(testDevice1.getDeviceNo()))
                .andExpect(jsonPath("$[1].deviceNo").value(testDevice2.getDeviceNo()));
    }

    @Test
    void getAllDevices_WhenNoDevices_ShouldReturnEmptyList() throws Exception {
        // Given
        deviceRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createDevice_WithInvalidDeviceNoFormat_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid deviceNo format (lowercase letters)
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(testCustomer.getId());

        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setCustomer(customerInfo);
        request.setDeviceName("Invalid Format Device");
        request.setDeviceNo("hw-123456"); // Invalid: lowercase letters
        request.setDeviceType("MOBILE");

        // When & Then - Should fail due to pattern validation
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.deviceNo").value(containsString("Device number must follow the format")));
    }

    @Test
    void createDevice_WithInvalidDeviceNoLength_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid deviceNo format (too few digits)
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(testCustomer.getId());

        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setCustomer(customerInfo);
        request.setDeviceName("Invalid Length Device");
        request.setDeviceNo("HW-12345"); // Invalid: only 5 digits
        request.setDeviceType("MOBILE");

        // When & Then - Should fail due to pattern validation
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createDevice_WithDuplicateDeviceNo_ShouldReturnBadRequest() throws Exception {
        // Given - Try to create device with existing deviceNo
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(testCustomer.getId());

        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setCustomer(customerInfo);
        request.setDeviceName("Duplicate Device");
        request.setDeviceNo(testDevice1.getDeviceNo()); // Duplicate deviceNo
        request.setDeviceType("MOBILE");

        // When & Then - Should fail due to unique constraint violation
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}