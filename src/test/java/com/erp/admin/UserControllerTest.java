package com.erp.admin;

import com.erp.admin.dto.*;
import com.erp.auth.dto.LoginRequest;
import com.erp.auth.dto.LoginResponse;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
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
public class UserControllerTest {

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
    static void setupClass(@Autowired EntityManagerFactory emf, @Autowired PasswordEncoder pe) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        tx.begin();
        
        var adminRole = new com.erp.admin.entity.Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Admin Role");
        adminRole.setIsActive(true);
        em.persist(adminRole);

        var userRole = new com.erp.admin.entity.Role();
        userRole.setName("USER");
        userRole.setDescription("User Role");
        userRole.setIsActive(true);
        em.persist(userRole);

        var admin = new com.erp.admin.entity.User();
        admin.setEmail("admin@erp.com");
        admin.setPasswordHash(pe.encode("test123"));
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

    @Test
    @Order(1)
    void testGetAllUsers_Success() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiResponse<PageResponse<UserDto>>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users?page=0&size=20",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @Order(2)
    void testGetUserById_Success() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users/1",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(3)
    void testGetUserById_NotFound() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users/99999",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(4)
    void testCreateUser_Success() {
        var createRequest = new CreateUserRequest();
        createRequest.setEmail("newuser@test.com");
        createRequest.setPassword("password123");
        createRequest.setFirstName("New");
        createRequest.setLastName("User");
        createRequest.setRoleName("USER");

        HttpEntity<CreateUserRequest> request = new HttpEntity<>(createRequest, getHeaders());

        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/users",
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        testUserId = response.getBody().getData().getId();
    }

    @Test
    @Order(5)
    void testCreateUser_InvalidEmail() {
        var createRequest = new CreateUserRequest();
        createRequest.setEmail("invalid-email");
        createRequest.setPassword("password123");
        createRequest.setFirstName("New");
        createRequest.setLastName("User");

        HttpEntity<CreateUserRequest> request = new HttpEntity<>(createRequest, getHeaders());

        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.postForEntity(
            baseUrl + ":" + 8080 + "/api/v1/users",
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(6)
    void testUpdateUser_Success() {
        if (testUserId == null) testCreateUser_Success();
        
        var updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");

        HttpEntity<UpdateUserRequest> request = new HttpEntity<>(updateRequest, getHeaders());

        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users/" + testUserId,
            HttpMethod.PUT,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(7)
    void testDeleteUser_Success() {
        if (testUserId == null) testCreateUser_Success();
        
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users/" + testUserId,
            HttpMethod.DELETE,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(8)
    void testGetAllUsers_Pagination() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiResponse<PageResponse<UserDto>>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users?page=0&size=10",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getPage()).isEqualTo(0);
        assertThat(response.getBody().getData().getSize()).isEqualTo(10);
    }

    @Test
    @Order(9)
    void testGetAllUsers_NoAuth() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<ApiResponse<PageResponse<UserDto>>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(10)
    void testGetUsers_FilterByRole() {
        HttpEntity<Void> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiResponse<PageResponse<UserDto>>> response = restTemplate.exchange(
            baseUrl + ":" + 8080 + "/api/v1/users?roleName=ADMIN",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}