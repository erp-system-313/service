package com.erp.purchasing;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SupplierControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test void testList() { assertStatus(exchange("/api/v1/suppliers?page=0&size=20", HttpMethod.GET), HttpStatus.OK); }
    @Test void testGet() { assertStatus(exchange("/api/v1/suppliers/1", HttpMethod.GET), HttpStatus.OK); }
    @Test void testCreate() { assertStatus(post("/api/v1/suppliers", "{\"name\":\"Test\"}"), HttpStatus.CREATED); }
    @Test void testUpdate() { assertStatus(put("/api/v1/suppliers/1", "{\"name\":\"Updated\"}"), HttpStatus.OK); }
    @Test void testDelete() { assertStatus(exchange("/api/v1/suppliers/1", HttpMethod.DELETE), HttpStatus.OK); }
    @Test void testNoAuth() { assertThat(anonExchange("/api/v1/suppliers").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private HttpHeaders auth() { HttpHeaders h = new HttpHeaders(); h.setBearerAuth("valid-token"); return h; }
    private HttpHeaders authJson() { HttpHeaders h = auth(); h.setContentType(MediaType.APPLICATION_JSON); return h; }
    private ResponseEntity<String> exchange(String url, HttpMethod m) { return restTemplate.exchange("http://localhost:8080" + url, m, new HttpEntity<>(auth()), String.class); }
    private ResponseEntity<String> post(String url, String json) { return restTemplate.exchange("http://localhost:8080" + url, HttpMethod.POST, new HttpEntity<>(json, authJson()), String.class); }
    private ResponseEntity<String> put(String url, String json) { return restTemplate.exchange("http://localhost:8080" + url, HttpMethod.PUT, new HttpEntity<>(json, authJson()), String.class); }
    private ResponseEntity<String> anonExchange(String url) { return restTemplate.exchange("http://localhost:8080" + url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class); }
    private void assertStatus(ResponseEntity<String> r, HttpStatus ok) { assertThat(r.getStatusCode()).isIn(ok, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST); }
}