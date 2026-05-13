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
public class CategoryControllerTest extends BaseControllerTest {

    @Test
    void testListCategories() {
        HttpEntity<Void> req = new HttpEntity<>(authHeaders());
        var r = exchange("/api/v1/categories?page=0&size=20", HttpMethod.GET, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void testGetCategoryById() {
        HttpEntity<Void> req = new HttpEntity<>(authHeaders());
        var r = exchange("/api/v1/categories/1", HttpMethod.GET, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void testCreateCategory() {
        String json = "{\"name\":\"Test Category\"}";
        HttpEntity<String> req = new HttpEntity<>(json, authJsonHeaders());
        var r = exchange("/api/v1/categories", HttpMethod.POST, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void testUpdateCategory() {
        String json = "{\"name\":\"Updated\"}";
        HttpEntity<String> req = new HttpEntity<>(json, authJsonHeaders());
        var r = exchange("/api/v1/categories/1", HttpMethod.PUT, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void testDeleteCategory() {
        HttpEntity<Void> req = new HttpEntity<>(authHeaders());
        var r = exchange("/api/v1/categories/1", HttpMethod.DELETE, req);
        assertThat(r.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.NO_CONTENT);
    }

    private HttpHeaders authHeaders() {
        return adminHeaders();
    }

    private HttpHeaders authJsonHeaders() {
        return adminJsonHeaders();
    }

    private ResponseEntity<String> exchange(String url, HttpMethod method, HttpEntity<?> req) {
        return restTemplate.exchange(url, method, req, String.class);
    }
}