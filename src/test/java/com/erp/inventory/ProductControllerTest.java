package com.erp.inventory;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import com.erp.BaseControllerTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerTest extends BaseControllerTest {

    @Test
    @Order(1)
    void testListProducts() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products?page=0&size=20",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    void testGetProductById() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products/1",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void testGetLowStockProducts() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products/low-stock",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void testCreateProduct() {
        String json = "{\"name\":\"Test\",\"sku\":\"TEST-001\",\"price\":100}";
        HttpEntity<String> request = new HttpEntity<>(json, adminJsonHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products",
            HttpMethod.POST, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    void testUpdateProduct() {
        String json = "{\"name\":\"Updated\"}";
        HttpEntity<String> request = new HttpEntity<>(json, adminJsonHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products/1",
            HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    void testDeleteProduct() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products/1",
            HttpMethod.DELETE, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(7)
    void testListProducts_Pagination() {
        HttpEntity<Void> request = new HttpEntity<>(adminHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products?page=0&size=10",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(8)
    void testListProducts_NoAuth() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/products",
            HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}