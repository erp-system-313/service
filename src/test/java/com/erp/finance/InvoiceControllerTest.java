package com.erp.finance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class InvoiceControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test public void testList() { check("/api/v1/invoices"); }
    @Test public void testGet() { check("/api/v1/invoices/1"); }
    @Test public void testGetPdf() { check("/api/v1/invoices/1/pdf"); }
    @Test public void testCreate() { post("/api/v1/invoices"); }
    @Test public void testAddPayment() { put("/api/v1/invoices/1/add-payment"); }
    @Test public void testSend() { put("/api/v1/invoices/1/send"); }
    @Test public void testCancel() { put("/api/v1/invoices/1/cancel"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/invoices").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private void post(String u) { assertThat(req(u, HttpMethod.POST, "{\"amount\":100}").getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST); }
    private void put(String u) { assertThat(req(u, HttpMethod.PUT, "{}").getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(url(u), String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m, String b) { HttpHeaders h = new HttpHeaders(); h.setBearerAuth("token"); h.setContentType(MediaType.APPLICATION_JSON); return restTemplate.exchange(url(u), m, new HttpEntity<>(b, h), String.class); }
    private String url(String u) { return "http://localhost:8080" + u; }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}