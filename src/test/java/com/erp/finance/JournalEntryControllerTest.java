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
public class JournalEntryControllerTest extends BaseControllerTest {

    @Test public void testList() { check("/api/v1/journal-entries"); }
    @Test public void testGet() { check("/api/v1/journal-entries/1"); }
    @Test public void testCreate() { post("/api/v1/journal-entries"); }
    @Test public void testPost() { post("/api/v1/journal-entries/1/post"); }
    @Test public void testReverse() { post("/api/v1/journal-entries/1/reverse", "PUT"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/journal-entries").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private void post(String u) { post(u, "POST"); }
    private void post(String u, String m) { HttpMethod method = "PUT".equals(m) ? HttpMethod.PUT : HttpMethod.POST; assertThat(req(u, method).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(u, String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m) { return restTemplate.exchange(u, m, new HttpEntity<>(adminHeaders()), String.class); }
    private String url(String u) { return u; }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}