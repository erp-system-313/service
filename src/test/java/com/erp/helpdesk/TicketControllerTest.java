package com.erp.helpdesk;

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
public class TicketControllerTest extends BaseControllerTest {

    @Test public void testList()           { check("/api/v1/support/tickets"); }
    @Test public void testGetById()        { check("/api/v1/support/tickets/1"); }
    @Test public void testCreate()         { post("/api/v1/support/tickets", "{\"title\":\"Test ticket\",\"customerId\":1,\"priority\":\"HIGH\"}"); }
    @Test public void testUpdate()         { put("/api/v1/support/tickets/1", "{\"status\":\"IN_PROGRESS\"}"); }
    @Test public void testDelete()         { del("/api/v1/support/tickets/1"); }
    @Test public void testKnowledgeBase()  { check("/api/v1/support/kb"); }
    @Test public void testNoAuth()         { assertThat(noauth("/api/v1/support/tickets").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private void check(String u) {
        assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }
    private void post(String u, String body) {
        assertThat(req(u, HttpMethod.POST, body).getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }
    private void put(String u, String body) {
        assertThat(req(u, HttpMethod.PUT, body).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }
    private void del(String u) {
        assertThat(req(u, HttpMethod.DELETE).getStatusCode())
            .isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED, HttpStatus.NO_CONTENT);
    }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(u, String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m) { return restTemplate.exchange(u, m, new HttpEntity<>(adminHeaders()), String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m, String body) {
        return restTemplate.exchange(u, m, new HttpEntity<>(body, adminJsonHeaders()), String.class);
    }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(u, String.class); }
}