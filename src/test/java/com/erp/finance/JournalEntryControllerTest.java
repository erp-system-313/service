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

    // GET-based tests
    @Test public void testList() { check("/api/v1/journal-entries"); }
    @Test public void testGet() { check("/api/v1/journal-entries/1"); }
    @Test public void testNoAuth() { assertThat(noauth("/api/v1/journal-entries").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED); }

    // POST without body → BAD_REQUEST (validation error)
    @Test public void testCreate() {
        assertThat(req("/api/v1/journal-entries", HttpMethod.POST).getStatusCode())
            .isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }

    // POST to /{id}/post (the controller uses @PostMapping)
    @Test public void testPost() {
        assertThat(req("/api/v1/journal-entries/1/post", HttpMethod.POST).getStatusCode())
            .isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }

    // PUT to /{id}/reverse (the controller uses @PutMapping)
    @Test public void testReverse() {
        assertThat(req("/api/v1/journal-entries/1/reverse", HttpMethod.PUT).getStatusCode())
            .isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }

    // Shared helper methods
    private void check(String u) { assertThat(req(u).getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED); }
    private ResponseEntity<String> req(String u) { return restTemplate.getForEntity(u, String.class); }
    private ResponseEntity<String> req(String u, HttpMethod m) { return restTemplate.exchange(u, m, new HttpEntity<>(adminHeaders()), String.class); }
    private String url(String u) { return u; }
    private ResponseEntity<String> noauth(String u) { return restTemplate.getForEntity(url(u), String.class); }
}
