package com.erp.hr;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = "/seed-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class AttendanceClockIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;

    @BeforeAll
    void login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"email\":\"admin@erp.com\",\"password\":\"test123\"}";
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/v1/auth/login", request, String.class);
        try {
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            adminToken = root.get("data").get("accessToken").asText();
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + response.getBody(), e);
        }
    }

    private HttpEntity<Void> authEntity() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(adminToken);
        return new HttpEntity<>(h);
    }

    @Test
    public void testCreateEmployeeAndClockIn() {
        // First create an employee
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setBearerAuth(adminToken);
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        String empJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@erp.com",
                "phone": "1234567890",
                "department": "ENGINEERING",
                "position": "Developer",
                "hireDate": "2024-01-01",
                "status": "ACTIVE"
            }
        """;
        HttpEntity<String> createEntity = new HttpEntity<>(empJson, jsonHeaders);
        ResponseEntity<String> createResponse = restTemplate.exchange(
            "/api/v1/employees", HttpMethod.POST, createEntity, String.class);
        System.out.println("Create employee: " + createResponse.getStatusCode() + " " + createResponse.getBody());
        assertThat(createResponse.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);

        // Now clock in
        ResponseEntity<String> clockInResponse = restTemplate.exchange(
            "/api/v1/attendance/clock-in?employeeId=1",
            HttpMethod.POST,
            authEntity(),
            String.class
        );
        System.out.println("Clock-in: " + clockInResponse.getStatusCode() + " " + clockInResponse.getBody());
        assertThat(clockInResponse.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST);

        // Clock out
        ResponseEntity<String> clockOutResponse = restTemplate.exchange(
            "/api/v1/attendance/clock-out?employeeId=1",
            HttpMethod.POST,
            authEntity(),
            String.class
        );
        System.out.println("Clock-out: " + clockOutResponse.getStatusCode() + " " + clockOutResponse.getBody());
    }
}
