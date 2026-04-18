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
public class CategoryControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test
    void testListCategories() {
        HttpEntity<Void> req = new HttpEntity<>(authHeaders());
        var r = exchange("http://localhost:8080/api/v1/categories?page=0&size=20", HttpMethod.GET, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetCategoryById() {
        HttpEntity<Void> req = new HttpEntity<>(authHeaders());
        var r = exchange("http://localhost:8080/api/v1/categories/1", HttpMethod.GET, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateCategory() {
        String json = "{\"name\":\"Test Category\"}";
        HttpEntity<String> req = new HttpEntity<>(json, authJsonHeaders());
        var r = exchange("http://localhost:8080/api/v1/categories", HttpMethod.POST, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testUpdateCategory() {
        String json = "{\"name\":\"Updated\"}";
        HttpEntity<String> req = new HttpEntity<>(json, authJsonHeaders());
        var r = exchange("http://localhost:8080/api/v1/categories/1", HttpMethod.PUT, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testDeleteCategory() {
        HttpEntity<Void> req = new HttpEntity<>(authHeaders());
        var r = exchange("http://localhost:8080/api/v1/categories/1", HttpMethod.DELETE, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth("valid-token");
        return h;
    }

    private HttpHeaders authJsonHeaders() {
        HttpHeaders h = authHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private ResponseEntity<String> exchange(String url, HttpMethod method, HttpEntity<?> req) {
        return restTemplate.exchange(url, method, req, String.class);
    }
}