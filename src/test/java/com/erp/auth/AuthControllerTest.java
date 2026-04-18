package com.erp.auth;

import com.erp.auth.dto.*;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.admin.dto.*;
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
public class AuthControllerTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected EntityManagerFactory emf;

    protected static String adminToken;
    protected static Long testUserId;

    protected static final String baseUrl = "http://localhost";

    @BeforeAll
    static void setupClass(@Autowired EntityManagerFactory emf, @Autowired PasswordEncoder passwordEncoder) {
        EntityManager em = emf.createEntityManager();
        
        var tx = em.getTransaction();
        tx.begin();
        
        var adminRole = new com.erp.admin.entity.Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator role");
        adminRole.setIsActive(true);
        em.persist(adminRole);

        var userRole = new com.erp.admin.entity.Role();
        userRole.setName("USER");
        userRole.setDescription("User role");
        userRole.setIsActive(true);
        em.persist(userRole);

        var admin = new com.erp.admin.entity.User();
        admin.setEmail("admin@erp.com");
        admin.setPasswordHash(passwordEncoder.encode("test123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(adminRole);
        admin.setIsActive(true);
        em.persist(admin);

        tx.commit();
        em.close();
    }

    @BeforeEach
    void setUp() {
        adminToken = null;
    }

    @Test
    @Order(1)
    void testLogin_Success() {
        var loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@erp.com");
        loginRequest.setPassword("test123");

        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/auth/login",
            loginRequest,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getAccessToken()).isNotNull();
        
        adminToken = response.getBody().getData().getAccessToken();
    }

    @Test
    @Order(2)
    void testLogin_InvalidCredentials() {
        var loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@erp.com");
        loginRequest.setPassword("wrongpassword");

        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/auth/login",
            loginRequest,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void testLogin_MissingEmail() {
        var loginRequest = new LoginRequest();
        loginRequest.setPassword("test123");

        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/auth/login",
            loginRequest,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    void testRefreshToken_Success() {
        if (adminToken == null) {
            testLogin_Success();
        }
        
        var refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(adminToken);

        ResponseEntity<ApiResponse<TokenResponse>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/auth/refresh",
            refreshRequest,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(5)
    void testForgotPassword_Success() {
        var forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("admin@erp.com");

        ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/auth/forgot-password",
            forgotPasswordRequest,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(6)
    void testForgotPassword_InvalidEmail() {
        var forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("nonexistent@test.com");

        ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/auth/forgot-password",
            forgotPasswordRequest,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(7)
    void testResetPassword_Success() {
        var resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken("test-token");
        resetPasswordRequest.setNewPassword("newpassword123");

        ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/auth/reset-password",
            resetPasswordRequest,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(8)
    void testLogout_WithAuth() {
        if (adminToken == null) {
            testLogin_Success();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/auth/logout",
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(9)
    void testLogout_NoAuth() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/auth/logout",
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}