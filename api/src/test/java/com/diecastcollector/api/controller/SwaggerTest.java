package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SwaggerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void swaggerUiShortcutIsReachableWithoutToken() {
        // springdoc serves /swagger-ui.html as a redirect to /swagger-ui/index.html; it used to be
        // missing from SecurityConfig's permitAll() list and came back 403.
        ResponseEntity<String> response = restTemplate.getForEntity("/swagger-ui.html", String.class);

        assertThat(response.getStatusCode().value()).isNotIn(401, 403);
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection())
                .isTrue();
    }

    @Test
    void swaggerUiPageIsReachableWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/swagger-ui/index.html", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("unchecked")
    void apiDocsDeclareBearerSchemeRequiredByDefault() {
        Map<String, Object> docs = restTemplate.getForObject("/v3/api-docs", Map.class);

        // The scheme is what makes Swagger UI show its "Authorize" button.
        var schemes = (Map<String, Map<String, Object>>) ((Map<String, Object>) docs.get("components")).get("securitySchemes");
        assertThat(schemes.get("bearerAuth")).containsEntry("type", "http").containsEntry("scheme", "bearer");
        assertThat((List<Map<String, Object>>) docs.get("security")).containsExactly(Map.of("bearerAuth", List.of()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void publicEndpointsOptOutOfTheBearerScheme() {
        Map<String, Object> docs = restTemplate.getForObject("/v3/api-docs", Map.class);
        var paths = (Map<String, Map<String, Map<String, Object>>>) docs.get("paths");

        // An empty security list overrides the global requirement: no padlock in Swagger UI.
        assertThat(paths.get("/auth/google").get("post").get("security")).isEqualTo(List.of());
        assertThat(paths.get("/photos/{filename}").get("get").get("security")).isEqualTo(List.of());
        // Protected endpoints inherit the global requirement (no per-operation override).
        assertThat(paths.get("/models").get("get")).doesNotContainKey("security");
    }
}
