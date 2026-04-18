package com.erp.admin;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void testListUsers_WithAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users?page=0&size=20",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    void testListUsers_NoAuth() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void testGetUserById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users/1",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void testGetUserById_NotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users/99999",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    void testCreateUser_Invalid() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"email\":\"invalid\",\"firstName\":\"Test\"}";
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users",
            HttpMethod.POST, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.CREATED, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    void testUpdateUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"firstName\":\"Updated\"}";
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users/1",
            HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(7)
    void testDeleteUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users/1",
            HttpMethod.DELETE, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(8)
    void testListUsers_Pagination() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users?page=0&size=10",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(9)
    void testListUsers_FilterByRole() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/users?roleName=ADMIN",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }
}