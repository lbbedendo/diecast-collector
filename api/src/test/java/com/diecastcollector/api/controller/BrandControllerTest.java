package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.BrandRequest;
import com.diecastcollector.api.dto.BrandResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class BrandControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createBrand() {
        var headers = authHeaders();

        ResponseEntity<BrandResponse> response = restTemplate.exchange(
                "/brands", HttpMethod.POST, new HttpEntity<>(new BrandRequest("Hot Wheels"), headers),
                BrandResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Hot Wheels");
    }

    @Test
    void fetchBrand() {
        var headers = authHeaders();
        BrandResponse created = create(headers, "Matchbox");

        ResponseEntity<BrandResponse> byId = restTemplate.exchange(
                "/brands/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), BrandResponse.class);
        assertThat(byId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byId.getBody().name()).isEqualTo("Matchbox");

        ResponseEntity<BrandResponse[]> all = restTemplate.exchange(
                "/brands", HttpMethod.GET, new HttpEntity<>(headers), BrandResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).extracting(BrandResponse::id).contains(created.id());
    }

    @Test
    void updateBrand() {
        var headers = authHeaders();
        BrandResponse created = create(headers, "California Collectible");

        ResponseEntity<BrandResponse> updated = restTemplate.exchange(
                "/brands/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(new BrandRequest("California Collectibles"), headers),
                BrandResponse.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("California Collectibles");

        ResponseEntity<BrandResponse> refetched = restTemplate.exchange(
                "/brands/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), BrandResponse.class);
        assertThat(refetched.getBody().name()).isEqualTo("California Collectibles");
    }

    @Test
    void deleteBrand() {
        var headers = authHeaders();
        BrandResponse created = create(headers, "Greenlight");

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/brands/" + created.id(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> refetch = restTemplate.exchange(
                "/brands/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        assertThat(refetch.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private BrandResponse create(HttpHeaders headers, String name) {
        return restTemplate
                .exchange("/brands", HttpMethod.POST, new HttpEntity<>(new BrandRequest(name), headers), BrandResponse.class)
                .getBody();
    }
}
