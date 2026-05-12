package com.erp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/seed-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public abstract class BaseControllerTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    private static String cachedAdminToken;

    @BeforeEach
    void configureRestTemplate() {
        restTemplate.getRestTemplate()
            .setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    protected String adminToken() {
        if (cachedAdminToken == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = "{\"email\":\"admin@erp.com\",\"password\":\"test123\"}";
            HttpEntity<String> request = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, String.class);
            try {
                JsonNode root = new ObjectMapper().readTree(response.getBody());
                cachedAdminToken = root.get("data").get("accessToken").asText();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse admin token from login response: " + response.getBody(), e);
            }
        }
        return cachedAdminToken;
    }

    protected HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken());
        return headers;
    }

    protected HttpHeaders adminJsonHeaders() {
        HttpHeaders headers = adminHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected <T> HttpEntity<T> adminEntity(T body) {
        return new HttpEntity<>(body, adminJsonHeaders());
    }

    protected HttpEntity<Void> adminEntity() {
        return new HttpEntity<>(adminHeaders());
    }
}
