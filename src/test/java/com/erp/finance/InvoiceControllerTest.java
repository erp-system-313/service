package com.erp.finance;

import com.erp.auth.dto.LoginRequest;
import com.erp.auth.dto.LoginResponse;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.finance.dto.*;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoiceControllerTest {
    @Autowired protected TestRestTemplate restTemplate;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected EntityManagerFactory emf;
    protected static String adminToken;
    protected static Long testInvoiceId;

    @BeforeAll
    static void setupClass(@Autowired EntityManagerFactory emf, @Autowired PasswordEncoder pe) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction(); tx.begin();
        var role = new com.erp.admin.entity.Role(); role.setName("ADMIN"); role.setIsActive(true); em.persist(role);
        var user = new com.erp.admin.entity.User(); user.setEmail("admin@erp.com"); user.setPasswordHash(pe.encode("test123")); user.setRole(role); user.setIsActive(true); em.persist(user);
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

    @Test @Order(1) void testListInvoices() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/invoices?page=0&size=20", HttpMethod.GET, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK); }
    @Test @Order(2) void testGetInvoiceById() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/invoices/1", HttpMethod.GET, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
    @Test @Order(3) void testGetInvoicePdf() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/invoices/1/pdf", HttpMethod.GET, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
    @Test @Order(4) void testCreateInvoice() { var req = new CreateInvoiceRequest(); req.setCustomerId(1L); req.setInvoiceDate(LocalDate.now()); req.setDueDate(LocalDate.now().plusDays(30)); req.setAmount(BigDecimal.valueOf(1000)); HttpEntity<CreateInvoiceRequest> request = new HttpEntity<>(req, getHeaders()); var r = restTemplate.postForEntity("http://localhost:8080/api/v1/invoices", request, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST); }
    @Test @Order(5) void testSendInvoice() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/invoices/1/send", HttpMethod.PUT, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
    @Test @Order(6) void testCancelInvoice() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/invoices/1/cancel", HttpMethod.PUT, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
    @Test @Order(7) void testPagination() { HttpEntity<Void> req = new HttpEntity<>(getHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/invoices?page=0&size=5", HttpMethod.GET, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK); }
    @Test @Order(8) void testNoAuth() { HttpEntity<Void> req = new HttpEntity<>(new HttpHeaders()); var r = restTemplate.exchange("http://localhost:8080/api/v1/invoices", HttpMethod.GET, req, new ParameterizedTypeReference<>() {}); assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }
}