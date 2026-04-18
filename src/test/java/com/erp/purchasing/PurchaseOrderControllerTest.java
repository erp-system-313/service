package com.erp.purchasing;

import com.erp.auth.dto.LoginRequest;
import com.erp.auth.dto.LoginResponse;
import com.erp.common.dto.ApiResponse;
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
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PurchaseOrderControllerTest {
    @Autowired protected TestRestTemplate restTemplate;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected EntityManagerFactory emf;
    protected static String adminToken;
    @BeforeAll static void setupClass(@Autowired EntityManagerFactory emf, @Autowired PasswordEncoder pe) { EntityManager em = emf.createEntityManager(); var tx = em.getTransaction(); tx.begin(); var role = new com.erp.admin.entity.Role(); role.setName("ADMIN"); role.setIsActive(true); em.persist(role); var user = new com.erp.admin.entity.User(); user.setEmail("admin@erp.com"); user.setPasswordHash(pe.encode("test123")); user.setRole(role); user.setIsActive(true); em.persist(user); tx.commit(); em.close(); }
    @BeforeEach void setUp() { if (adminToken == null) { var login = new LoginRequest(); login.setEmail("admin@erp.com"); login.setPassword("test123"); var r = restTemplate.postForEntity("http://localhost:8080/api/v1/auth/login", login, new ParameterizedTypeReference<>() {}); adminToken = r.getBody().getData().getAccessToken(); } }
    private HttpHeaders getHeaders() { HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); h.setBearerAuth(adminToken); return h; }
    @Test @Order(1) void testListPurchaseOrders() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/purchase-orders?page=0&size=20", HttpMethod.GET, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK); }
    @Test @Order(2) void testGetPurchaseOrderById() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/purchase-orders/1", HttpMethod.GET, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
}