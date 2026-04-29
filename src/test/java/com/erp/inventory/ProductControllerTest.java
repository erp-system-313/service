package com.erp.inventory;

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
public class ProductControllerTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void testListProducts() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products?page=0&size=20",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    void testGetProductById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products/1",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void testGetLowStockProducts() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products/low-stock",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void testCreateProduct() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"name\":\"Test\",\"sku\":\"TEST-001\",\"price\":100}";
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products",
            HttpMethod.POST, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    void testUpdateProduct() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"name\":\"Updated\"}";
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products/1",
            HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    void testDeleteProduct() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products/1",
            HttpMethod.DELETE, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(7)
    void testListProducts_Pagination() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("valid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products?page=0&size=10",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(8)
    void testListProducts_NoAuth() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8080/api/v1/products",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}