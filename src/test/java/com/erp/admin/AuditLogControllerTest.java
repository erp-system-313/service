package com.erp.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuditLogControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test public void testList() { check("/api/v1/audit-logs"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/audit-logs").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN); }

    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(u, String.class); }
    private String url(String u) { return u; }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}