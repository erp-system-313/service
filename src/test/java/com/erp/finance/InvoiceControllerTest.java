package com.erp.finance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import com.erp.BaseControllerTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class InvoiceControllerTest extends BaseControllerTest {

    @Test public void testList() { check("/api/v1/invoices"); }
    @Test public void testGet() { check("/api/v1/invoices/1"); }
    @Test public void testGetPdf() { check("/api/v1/invoices/1/pdf"); }
    @Test public void testCreate() { post("/api/v1/invoices"); }
    @Test public void testAddPayment() { post("/api/v1/invoices/1/payments"); }
    @Test public void testSend() { put("/api/v1/invoices/1/send"); }
    @Test public void testCancel() { put("/api/v1/invoices/1/cancel"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/invoices").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private void post(String u) { assertThat(req(u, HttpMethod.POST, "{\"amount\":100}").getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private void put(String u) { assertThat(req(u, HttpMethod.PUT, "{}").getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(u, String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m, String b) { return restTemplate.exchange(u, m, new HttpEntity<>(b, adminJsonHeaders()), String.class); }
    private String url(String u) { return u; }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}