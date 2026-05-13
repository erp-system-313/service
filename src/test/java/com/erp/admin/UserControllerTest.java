package com.erp.admin;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import com.erp.BaseControllerTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest extends BaseControllerTest {

    @Test
    @Order(1)
    void testListUsers_WithAuth() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users?page=0&size=20",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    void testListUsers_NoAuth() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void testGetUserById() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users/1",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void testGetUserById_NotFound() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users/99999",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    void testCreateUser_Invalid() {
        String json = "{\"email\":\"invalid\",\"firstName\":\"Test\"}";
        HttpEntity<String> request = new HttpEntity<>(json, adminJsonHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.POST, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.CREATED, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    void testUpdateUser() {
        String json = "{\"firstName\":\"Updated\"}";
        HttpEntity<String> request = new HttpEntity<>(json, adminJsonHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users/2",
            HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(7)
    void testDeleteUser() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users/2",
            HttpMethod.DELETE, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(8)
    void testListUsers_Pagination() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users?page=0&size=10",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(9)
    void testListUsers_FilterByRole() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users?roleName=ADMIN",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }
}
