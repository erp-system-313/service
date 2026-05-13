package com.erp.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TicketControllerTest {
    @Autowired protected TestRestTemplate restTemplate;

    @Test public void testList()           { check("/api/v1/support/tickets"); }
    @Test public void testGetById()        { check("/api/v1/support/tickets/1"); }
    @Test public void testCreate()         { post("/api/v1/support/tickets"); }
    @Test public void testUpdate()         { put("/api/v1/support/tickets/1"); }
    @Test public void testDelete()         { del("/api/v1/support/tickets/1"); }
    @Test public void testKnowledgeBase()  { check("/api/v1/support/kb"); }
    @Test public void testNoAuth()         { assertThat(noauth("/api/v1/support/tickets").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN); }

    private void check(String u) {
        assertThat(req(u).getStatusCode()).isIn(
            HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN
        );
    }
    private void post(String u) {
        assertThat(req(u, HttpMethod.POST, "{\"title\":\"Test ticket\",\"customerId\":1,\"priority\":\"HIGH\"}")
            .getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN);
    }
    private void put(String u) {
        assertThat(req(u, HttpMethod.PUT, "{\"status\":\"IN_PROGRESS\"}")
            .getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.FORBIDDEN);
    }
    private void del(String u) {
        assertThat(req(u, HttpMethod.DELETE, "").getStatusCode())
            .isIn(HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND, HttpStatus.FORBIDDEN);
    }
    private ResponseEntity<String> req(String u) {
        return restTemplate.getForEntity(u, String.class);
    }
    private ResponseEntity<String> req(String u, HttpMethod m, String b) {
        return restTemplate.exchange(u, m, entity(b), String.class);
    }
    private HttpEntity<String> entity(String b) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth("token");
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(b, h);
    }
    private ResponseEntity<String> noauth(String u) {
        return restTemplate.getForEntity(u, String.class);
    }
}
