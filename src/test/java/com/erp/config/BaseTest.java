package com.erp.config;

import com.erp.admin.entity.Role;
import com.erp.admin.entity.User;
import com.erp.common.dto.PageResponse;
import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountType;
import com.erp.inventory.entity.Category;
import com.erp.inventory.entity.Product;
import com.erp.hr.entity.Employee;
import com.erp.purchasing.entity.Supplier;
import com.erp.sales.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
public abstract class BaseTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected String adminToken;
    protected String userToken;

    protected Long testCategoryId;
    protected Long testProductId;
    protected Long testSupplierId;
    protected Long testCustomerId;
    protected Long testEmployeeId;
    protected Long testAccountId;

    protected final String baseUrl = "http://localhost";

    @BeforeEach
    void setUp() {
        adminToken = null;
        userToken = null;
        seedTestData();
    }

    protected void seedTestData() {
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator role");
        adminRole.setIsActive(true);
        entityManager.persist(adminRole);

        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("User role");
        userRole.setIsActive(true);
        entityManager.persist(userRole);

        User admin = new User();
        admin.setEmail("admin@erp.com");
        admin.setPasswordHash(passwordEncoder.encode("test123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(adminRole);
        admin.setIsActive(true);
        entityManager.persist(admin);

        User user = new User();
        user.setEmail("user@test.com");
        user.setPasswordHash(passwordEncoder.encode("test123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(userRole);
        user.setIsActive(true);
        entityManager.persist(user);

        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Category Description");
        category.setStatus(Category.Status.ACTIVE);
        entityManager.persist(category);
        testCategoryId = category.getId();

        Product product = new Product();
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setDescription("Test Product Description");
        product.setCategory(category);
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setCost(BigDecimal.valueOf(50.00));
        product.setQuantity(100);
        product.setMinStock(10);
        product.setStatus(Product.Status.ACTIVE);
        entityManager.persist(product);
        testProductId = product.getId();

        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier");
        supplier.setEmail("supplier@test.com");
        supplier.setPhone("1234567890");
        supplier.setAddress("Test Address");
        supplier.setStatus(Supplier.Status.ACTIVE);
        entityManager.persist(supplier);
        testSupplierId = supplier.getId();

        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail("customer@test.com");
        customer.setPhone("1234567890");
        customer.setAddress("Test Address");
        customer.setIsActive(true);
        entityManager.persist(customer);
        testCustomerId = customer.getId();

        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP001");
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setEmail("employee@test.com");
        employee.setPhone("1234567890");
        employee.setDepartment("IT");
        employee.setPosition("Developer");
        employee.setHireDate(LocalDate.now());
        employee.setStatus(Employee.Status.ACTIVE);
        entityManager.persist(employee);
        testEmployeeId = employee.getId();

        Account account = new Account();
        account.setAccountNumber("1000");
        account.setName("Cash");
        account.setAccountType(AccountType.ASSET);
        account.setIsActive(true);
        entityManager.persist(account);
        testAccountId = account.getId();

        entityManager.flush();
    }

    protected String loginAsAdmin() {
        if (adminToken != null) return adminToken;
        
        var loginRequest = new com.erp.auth.dto.LoginRequest();
        loginRequest.setEmail("admin@erp.com");
        loginRequest.setPassword("test123");
        
        ResponseEntity<com.erp.common.dto.ApiResponse<com.erp.auth.dto.LoginResponse>> response = restTemplate.postForEntity(
            baseUrl + ":" + port + "/api/v1/auth/login",
            loginRequest,
            new ParameterizedTypeReference<>() {}
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        
        adminToken = response.getBody().getData().getAccessToken();
        return adminToken;
    }

    protected String loginAsUser() {
        if (userToken != null) return userToken;
        
        var loginRequest = new com.erp.auth.dto.LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("test123");
        
        ResponseEntity<com.erp.common.dto.ApiResponse<com.erp.auth.dto.LoginResponse>> response = restTemplate.postForEntity(
            baseUrl + ":" + port + "/api/v1/auth/login",
            loginRequest,
            new ParameterizedTypeReference<>() {}
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        
        userToken = response.getBody().getData().getAccessToken();
        return userToken;
    }

    protected HttpHeaders getAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    protected HttpHeaders getHeaders() {
        return getAuthHeaders(loginAsAdmin());
    }

    protected String getUrl(String path) {
        return baseUrl + ":" + port + path;
    }

    protected <T> HttpEntity<T> createRequest(T body) {
        return new HttpEntity<>(body, getHeaders());
    }

    protected <T> HttpEntity<T> createRequest(T body, String token) {
        return new HttpEntity<>(body, getAuthHeaders(token));
    }

    protected HttpEntity<Void> createEmptyRequest() {
        return new HttpEntity<>(getHeaders());
    }

    protected HttpEntity<Void> createEmptyRequest(String token) {
        return new HttpEntity<>(getAuthHeaders(token));
    }
}