package com.erp.inventory;

import com.erp.auth.dto.LoginRequest;
import com.erp.auth.dto.LoginResponse;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.inventory.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected EntityManagerFactory emf;

    protected static String adminToken;
    protected static Long testCategoryId;
    protected static Long testProductId;

    protected static final String baseUrl = "http://localhost";

    @BeforeAll
    static void setupClass(@Autowired EntityManagerFactory emf, @Autowired PasswordEncoder pe) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        tx.begin();
        
        var role = new com.erp.admin.entity.Role();
        role.setName("ADMIN");
        role.setDescription("Admin Role");
        role.setIsActive(true);
        em.persist(role);

        var user = new com.erp.admin.entity.User();
        user.setEmail("admin@erp.com");
        user.setPasswordHash(pe.encode("test123"));
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setRole(role);
        user.setIsActive(true);
        em.persist(user);

        var category = new com.erp.inventory.entity.Category();
        category.setName("Test Category");
        category.setDescription("Test Category");
        category.setStatus(com.erp.inventory.entity.Category.Status.ACTIVE);
        em.persist(category);
        testCategoryId = category.getId();

        tx.commit();
        em.close();
    }

    @BeforeEach
    void setUp() {
        if (adminToken == null) {
            var loginRequest = new LoginRequest();
            loginRequest.setEmail("admin@erp.com");
            loginRequest.setPassword("test123");

            ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.postForEntity(
                baseUrl + ":" + 8080 + "/api/v1/auth/login",
                loginRequest,
                new ParameterizedTypeReference<>() {}
            );
            adminToken = response.getBody().getData().getAccessToken();
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    @Test @Order(1)
    void testGetAllProducts_Success() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());
        ResponseEntity<ApiResponse<PageResponse<ProductDto>>> response = restTemplate.exchange(
            baseUrl + ":8080/api/v1/products?page=0&size=20",
            HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(2)
    void testGetProductById_Success() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());
        ResponseEntity<ApiResponse<ProductDto>> response = restTemplate.exchange(
            baseUrl + ":8080/api/v1/products/" + testCategoryId,
            HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test @Order(3)
    void testCreateProduct_Success() {
        var requestBody = new CreateProductRequest();
        requestBody.setName("New Product");
        requestBody.setSku("NEW-001");
        requestBody.setDescription("Test");
        requestBody.setCategoryId(testCategoryId);
        requestBody.setPrice(BigDecimal.valueOf(100));
        requestBody.setCost(BigDecimal.valueOf(50));
        requestBody.setQuantity(100);
        requestBody.setMinStock(10);

        HttpEntity<CreateProductRequest> request = new HttpEntity<>(requestBody, getHeaders());
        ResponseEntity<ApiResponse<ProductDto>> response = restTemplate.postForEntity(
            baseUrl + ":8080/api/v1/products", request, new ParameterizedTypeReference<>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        testProductId = response.getBody().getData().getId();
    }

    @Test @Order(4)
    void testCreateProduct_InvalidInput() {
        var requestBody = new CreateProductRequest();
        requestBody.setName("");  // Empty name - invalid

        HttpEntity<CreateProductRequest> request = new HttpEntity<>(requestBody, getHeaders());
        ResponseEntity<ApiResponse<ProductDto>> response = restTemplate.postForEntity(
            baseUrl + ":8080/api/v1/products", request, new ParameterizedTypeReference<>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test @Order(5)
    void testUpdateProduct_Success() {
        if (testProductId == null) testCreateProduct_Success();
        
        var requestBody = new UpdateProductRequest();
        requestBody.setName("Updated Product");

        HttpEntity<UpdateProductRequest> request = new HttpEntity<>(requestBody, getHeaders());
        ResponseEntity<ApiResponse<ProductDto>> response = restTemplate.exchange(
            baseUrl + ":8080/api/v1/products/" + testProductId,
            HttpMethod.PUT, request, new ParameterizedTypeReference<>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(6)
    void testDeleteProduct_Success() {
        if (testProductId == null) testCreateProduct_Success();
        
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
            baseUrl + ":8080/api/v1/products/" + testProductId,
            HttpMethod.DELETE, request, new ParameterizedTypeReference<>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(7)
    void testGetProducts_Pagination() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());
        ResponseEntity<ApiResponse<PageResponse<ProductDto>>> response = restTemplate.exchange(
            baseUrl + ":8080/api/v1/products?page=0&size=5",
            HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getSize()).isEqualTo(5);
    }

    @Test @Order(8)
    void testGetProducts_NoAuth() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<ApiResponse<PageResponse<ProductDto>>> response = restTemplate.exchange(
            baseUrl + ":8080/api/v1/products",
            HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}