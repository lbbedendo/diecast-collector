package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void devLoginRouteDoesNotExistByDefault() {
        // app.auth.dev-login.enabled is false by default (see application.yml) — DevAuthController
        // must not even be registered as a bean, so this 404s rather than running dev-only logic.
        // DevAuthControllerTest covers the enabled case with its own context.
        ResponseEntity<Object> response = restTemplate.postForEntity("/auth/dev", null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
