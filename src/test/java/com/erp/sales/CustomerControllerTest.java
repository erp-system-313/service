package com.erp.sales;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CustomerControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test public void testList() { check("/api/v1/customers"); }
    @Test public void testGetById() { check("/api/v1/customers/1"); }
    @Test public void testGetOrders() { check("/api/v1/customers/1/orders"); }
    @Test public void testCreate() { post("/api/v1/customers"); }
    @Test public void testUpdate() { put("/api/v1/customers/1"); }
    @Test public void testDelete() { del("/api/v1/customers/1"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/customers").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private void post(String u) { assertThat(req(u, HttpMethod.POST, "{\"name\":\"test\"}").getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST); }
    private void put(String u) { assertThat(req(u, HttpMethod.PUT, "{\"name\":\"x\"}").getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
    private void del(String u) { assertThat(req(u, HttpMethod.DELETE, "").getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(url(u), String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m, String b) { return restTemplate.exchange(url(u), m, entity(b), String.class); }
    private String url(String u) { return "http://localhost:8080" + u; }
    private HttpEntity<String> entity(String b) { HttpHeaders h = new HttpHeaders(); h.setBearerAuth("token"); h.setContentType(MediaType.APPLICATION_JSON); return new HttpEntity<>(b, h); }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}