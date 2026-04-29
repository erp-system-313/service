package com.erp.hr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LeaveControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test public void testList() { check("/api/v1/leave-requests"); }
    @Test public void testGet() { check("/api/v1/leave-requests/1"); }
    @Test public void testCreate() { post("/api/v1/leave-requests"); }
    @Test public void testApprove() { post("/api/v1/leave-requests/1/approve", "PUT"); }
    @Test public void testReject() { post("/api/v1/leave-requests/1/reject", "PUT"); }
    @Test public void testBalances() { check("/api/v1/leave-requests/balances"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/leave-requests").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private void post(String u) { post(u, "POST"); }
    private void post(String u, String m) { HttpMethod method = "PUT".equals(m) ? HttpMethod.PUT : HttpMethod.POST; assertThat(req(u, method).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(url(u), String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m) { HttpHeaders h = new HttpHeaders(); h.setBearerAuth("token"); return restTemplate.exchange(url(u), m, new HttpEntity<>(h), String.class); }
    private String url(String u) { return "http://localhost:8080" + u; }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}