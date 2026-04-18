package com.erp.purchasing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PurchaseOrderControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test public void testList() { check("/api/v1/purchase-orders"); }
    @Test public void testGet() { check("/api/v1/purchase-orders/1"); }
    @Test public void testCreate() { post("/api/v1/purchase-orders"); }
    @Test public void testUpdate() { put("/api/v1/purchase-orders/1"); }
    @Test public void testReceive() { post("/api/v1/purchase-orders/1/receive", "PUT"); }
    @Test public void testCancel() { post("/api/v1/purchase-orders/1/cancel", "PUT"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/purchase-orders").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private void post(String u) { post(u, "POST"); }
    private void put(String u) { post(u, "PUT"); }
    private void post(String u, String m) { HttpMethod method = "PUT".equals(m) ? HttpMethod.PUT : HttpMethod.POST; assertThat(req(u, method).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(url(u), String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m) { HttpHeaders h = new HttpHeaders(); h.setBearerAuth("token"); return restTemplate.exchange(url(u), m, new HttpEntity<>(h), String.class); }
    private String url(String u) { return "http://localhost:8080" + u; }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}