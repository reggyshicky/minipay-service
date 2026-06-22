package com.minipay.minipay_service.controller;

import com.minipay.minipay_service.config.AbstractIntegrationTest;
import com.minipay.minipay_service.dto.request.LoginRequest;
import com.minipay.minipay_service.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void register_shouldReturn201AndToken_whenValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("integrationuser");
        request.setEmail("integration@test.com");
        request.setPassword("testpass123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("token");
        assertThat(response.getBody()).contains("integrationuser");
    }

    @Test
    void register_shouldReturn409_whenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicateuser");
        request.setEmail("first@test.com");
        request.setPassword("testpass123");
        restTemplate.postForEntity("/api/auth/register", request, String.class);

        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername("duplicateuser");
        duplicateRequest.setEmail("second@test.com");
        duplicateRequest.setPassword("testpass123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", duplicateRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Username already taken");
    }

    @Test
    void login_shouldReturn200AndToken_whenCredentialsAreValid() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginuser");
        registerRequest.setEmail("loginuser@test.com");
        registerRequest.setPassword("testpass123");
        restTemplate.postForEntity("/api/auth/register", registerRequest, String.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("testpass123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("token");
    }

    @Test
    void login_shouldReturn401_whenPasswordIsWrong() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("wrongpassuser");
        registerRequest.setEmail("wrongpass@test.com");
        registerRequest.setPassword("correctpass123");
        restTemplate.postForEntity("/api/auth/register", registerRequest, String.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpassuser");
        loginRequest.setPassword("wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}