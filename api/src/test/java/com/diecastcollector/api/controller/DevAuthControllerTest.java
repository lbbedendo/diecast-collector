package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.AuthResponse;
import com.diecastcollector.api.dto.AutomakerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * DevAuthController only exists as a bean when app.auth.dev-login.enabled=true (it's off by
 * default in application.yml), so this enables it just for this test class — a different
 * property set means Spring boots a separate ApplicationContext here rather than reusing the
 * one cached by the other controller tests.
 */
class DevAuthControllerTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void enableDevLogin(DynamicPropertyRegistry registry) {
        registry.add("app.auth.dev-login.enabled", () -> "true");
    }

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void devLoginIssuesATokenThatActuallyAuthenticates() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/auth/dev", null, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().user().email()).isEqualTo("dev@localhost");

        // Prove the token is real, not just present: it must pass JwtAuthenticationFilter on an
        // actual protected endpoint.
        var headers = new HttpHeaders();
        headers.setBearerAuth(response.getBody().accessToken());
        ResponseEntity<AutomakerResponse[]> automakers = restTemplate.exchange(
                "/automakers", HttpMethod.GET, new HttpEntity<>(headers), AutomakerResponse[].class);
        assertThat(automakers.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void devLoginReusesTheSameUserAcrossCalls() {
        AuthResponse first = restTemplate.postForObject("/auth/dev", null, AuthResponse.class);
        AuthResponse second = restTemplate.postForObject("/auth/dev", null, AuthResponse.class);

        assertThat(second.user().id()).isEqualTo(first.user().id());
    }
}
