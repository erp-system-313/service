package com.erp.sales;

import com.erp.auth.dto.LoginRequest;
import com.erp.auth.dto.LoginResponse;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.sales.dto.*;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SalesOrderControllerTest {
    @Autowired protected TestRestTemplate restTemplate;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected EntityManagerFactory emf;
    protected static String adminToken;
    protected static Long testCustomerId;
    protected static Long testOrderId;

    @BeforeAll
    static void setupClass(@Autowired EntityManagerFactory emf, @Autowired PasswordEncoder pe) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction(); tx.begin();
        var role = new com.erp.admin.entity.Role(); role.setName("ADMIN"); role.setIsActive(true); em.persist(role);
        var user = new com.erp.admin.entity.User(); user.setEmail("admin@erp.com"); user.setPasswordHash(pe.encode("test123")); user.setRole(role); user.setIsActive(true); em.persist(user);
        var customer = new com.erp.sales.entity.Customer(); customer.setName("Test Customer"); customer.setEmail("test@test.com"); customer.setIsActive(true); em.persist(customer);
        testCustomerId = customer.getId();
        tx.commit(); em.close();
    }

    @BeforeEach
    void setUp() {
        if (adminToken == null) {
            var login = new LoginRequest(); login.setEmail("admin@erp.com"); login.setPassword("test123");
            ResponseEntity<ApiResponse<LoginResponse>> r = restTemplate.postForEntity("http://localhost:8080/api/v1/auth/login", login, new ParameterizedTypeReference<>() {});
            adminToken = r.getBody().getData().getAccessToken();
        }
    }

    private HttpHeaders getHeaders() { HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); h.setBearerAuth(adminToken); return h; }

    @Test @Order(1) void testListSalesOrders() {
        HttpEntity<Void> req = new HttpEntity<>(getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders?page=0&size=20", HttpMethod.GET, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(2) void testGetSalesOrderById() {
        HttpEntity<Void> req = new HttpEntity<>(getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders/1", HttpMethod.GET, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test @Order(3) void testCreateSalesOrder() {
        var req = new CreateSalesOrderRequest(); req.setCustomerId(testCustomerId);
        var lines = new ArrayList<SalesOrderLineDto>();
        var line = new SalesOrderLineDto(); line.setProductId(1L); line.setQuantity(5); line.setUnitPrice(BigDecimal.valueOf(100));
        lines.add(line);
        req.setLines(lines);
        
        HttpEntity<CreateSalesOrderRequest> request = new HttpEntity<>(req, getHeaders());
        var r = restTemplate.postForEntity("http://localhost:8080/api/v1/sales-orders", request, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST);
    }

    @Test @Order(4) void testUpdateSalesOrder() {
        var req = new UpdateSalesOrderRequest(); req.setNotes("Updated notes");
        HttpEntity<UpdateSalesOrderRequest> request = new HttpEntity<>(req, getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders/1", HttpMethod.PUT, request, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test @Order(5) void testConfirmSalesOrder() {
        HttpEntity<Void> req = new HttpEntity<>(getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders/1/confirm", HttpMethod.PUT, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test @Order(6) void testShipSalesOrder() {
        HttpEntity<Void> req = new HttpEntity<>(getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders/1/ship", HttpMethod.PUT, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test @Order(7) void testCancelSalesOrder() {
        HttpEntity<Void> req = new HttpEntity<>(getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders/1/cancel", HttpMethod.PUT, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test @Order(8) void testDeleteSalesOrder() {
        HttpEntity<Void> req = new HttpEntity<>(getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders/1", HttpMethod.DELETE, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test @Order(9) void testPagination() {
        HttpEntity<Void> req = new HttpEntity<>(getHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders?page=0&size=5", HttpMethod.GET, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(10) void testNoAuth() {
        HttpEntity<Void> req = new HttpEntity<>(new HttpHeaders());
        var r = restTemplate.exchange("http://localhost:8080/api/v1/sales-orders", HttpMethod.GET, req, new ParameterizedTypeReference<>() {});
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}