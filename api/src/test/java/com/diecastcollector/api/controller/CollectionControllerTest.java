package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.CollectionRequest;
import com.diecastcollector.api.dto.CollectionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CollectionControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createCollection() {
        var headers = authHeaders();

        ResponseEntity<CollectionResponse> response = restTemplate.exchange(
                "/collections",
                HttpMethod.POST,
                new HttpEntity<>(new CollectionRequest("Factory Fresh", 2024), headers),
                CollectionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Factory Fresh");
        assertThat(response.getBody().year()).isEqualTo(2024);
    }

    @Test
    void fetchCollection() {
        var headers = authHeaders();
        CollectionResponse created = create(headers, "Moving Parts", 2023);

        ResponseEntity<CollectionResponse> byId = restTemplate.exchange(
                "/collections/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), CollectionResponse.class);
        assertThat(byId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byId.getBody().name()).isEqualTo("Moving Parts");
        assertThat(byId.getBody().year()).isEqualTo(2023);

        ResponseEntity<CollectionResponse[]> all = restTemplate.exchange(
                "/collections", HttpMethod.GET, new HttpEntity<>(headers), CollectionResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).extracting(CollectionResponse::id).contains(created.id());
    }

    @Test
    void updateCollection() {
        var headers = authHeaders();
        CollectionResponse created = create(headers, "Premium", 2022);

        ResponseEntity<CollectionResponse> updated = restTemplate.exchange(
                "/collections/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(new CollectionRequest("Premium Series", 2023), headers),
                CollectionResponse.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("Premium Series");
        assertThat(updated.getBody().year()).isEqualTo(2023);

        ResponseEntity<CollectionResponse> refetched = restTemplate.exchange(
                "/collections/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), CollectionResponse.class);
        assertThat(refetched.getBody().name()).isEqualTo("Premium Series");
        assertThat(refetched.getBody().year()).isEqualTo(2023);
    }

    @Test
    void deleteCollection() {
        var headers = authHeaders();
        CollectionResponse created = create(headers, "Super Treasure Hunt", 2021);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/collections/" + created.id(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> refetch = restTemplate.exchange(
                "/collections/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        assertThat(refetch.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private CollectionResponse create(HttpHeaders headers, String name, Integer year) {
        return restTemplate
                .exchange(
                        "/collections",
                        HttpMethod.POST,
                        new HttpEntity<>(new CollectionRequest(name, year), headers),
                        CollectionResponse.class)
                .getBody();
    }
}
