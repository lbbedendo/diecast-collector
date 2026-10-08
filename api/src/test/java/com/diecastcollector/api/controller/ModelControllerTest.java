package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.AutomakerRequest;
import com.diecastcollector.api.dto.AutomakerResponse;
import com.diecastcollector.api.dto.BrandRequest;
import com.diecastcollector.api.dto.BrandResponse;
import com.diecastcollector.api.dto.ModelRequest;
import com.diecastcollector.api.dto.ModelResponse;
import com.diecastcollector.api.dto.SeriesRequest;
import com.diecastcollector.api.dto.SeriesResponse;
import com.diecastcollector.api.enums.ModelCondition;
import com.diecastcollector.api.enums.ModelScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ModelControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createModel() {
        var headers = authHeaders();
        var request = modelRequest(headers, "Ferrari 458 Italia");

        ResponseEntity<ModelResponse> response =
                restTemplate.exchange("/models", HttpMethod.POST, new HttpEntity<>(request, headers), ModelResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Ferrari 458 Italia");
        assertThat(response.getBody().scale()).isEqualTo(ModelScale.SCALE_1_64);
        assertThat(response.getBody().condition()).isEqualTo(ModelCondition.MINT);
        // The create response's nested automaker/brand/series are bare id references (not
        // hydrated from the DB, unlike GET's @EntityGraph fetch — see fetchModel() below).
        assertThat(response.getBody().automaker().id()).isEqualTo(request.automakerId());
        assertThat(response.getBody().brand().id()).isEqualTo(request.brandId());
        assertThat(response.getBody().series().id()).isEqualTo(request.seriesId());
    }

    @Test
    void fetchModel() {
        var headers = authHeaders();
        ModelResponse created = create(headers, modelRequest(headers, "Porsche 911 GT3"));

        ResponseEntity<ModelResponse> byId = restTemplate.exchange(
                "/models/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), ModelResponse.class);
        assertThat(byId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byId.getBody().name()).isEqualTo("Porsche 911 GT3");
        // Unlike the create response, GET fully hydrates the nested entities (@EntityGraph fetch).
        assertThat(byId.getBody().automaker().name()).startsWith("Ferrari-");
        assertThat(byId.getBody().brand().name()).startsWith("Hot Wheels-");
        assertThat(byId.getBody().series().name()).startsWith("HW Starting Grid-");

        ResponseEntity<ModelResponse[]> all = restTemplate.exchange(
                "/models", HttpMethod.GET, new HttpEntity<>(headers), ModelResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).extracting(ModelResponse::id).contains(created.id());
    }

    @Test
    void fetchModelIsScopedToOwner() {
        var owner = authHeaders();
        ModelResponse created = create(owner, modelRequest(owner, "Owner-only Civic"));

        var otherUser = authHeaders();
        ResponseEntity<Object> response = restTemplate.exchange(
                "/models/" + created.id(), HttpMethod.GET, new HttpEntity<>(otherUser), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateModel() {
        var headers = authHeaders();
        ModelResponse created = create(headers, modelRequest(headers, "Lamborghini Huracan"));

        var updateRequest = new ModelRequest(
                "Lamborghini Huracan EVO",
                created.modelYear(),
                created.scale(),
                "Yellow",
                ModelCondition.SEALED,
                created.seriesNumber(),
                true,
                created.purchasePrice(),
                created.purchaseDate(),
                created.purchasedFrom(),
                "Mint on card",
                created.automaker().id(),
                created.brand().id(),
                created.series().id());

        ResponseEntity<ModelResponse> updated = restTemplate.exchange(
                "/models/" + created.id(), HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), ModelResponse.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("Lamborghini Huracan EVO");
        assertThat(updated.getBody().color()).isEqualTo("Yellow");
        assertThat(updated.getBody().condition()).isEqualTo(ModelCondition.SEALED);
        assertThat(updated.getBody().chase()).isTrue();

        ResponseEntity<ModelResponse> refetched = restTemplate.exchange(
                "/models/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), ModelResponse.class);
        assertThat(refetched.getBody().name()).isEqualTo("Lamborghini Huracan EVO");
        assertThat(refetched.getBody().chase()).isTrue();
    }

    @Test
    void deleteModel() {
        var headers = authHeaders();
        ModelResponse created = create(headers, modelRequest(headers, "Nissan Skyline GT-R"));

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/models/" + created.id(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> refetch = restTemplate.exchange(
                "/models/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        assertThat(refetch.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ModelRequest modelRequest(HttpHeaders headers, String name) {
        // automaker/brand names are globally unique, so each call needs its own —
        // reusing a fixed name here would collide across test methods (and test classes, since
        // they share one database) the second time it's created.
        String unique = UUID.randomUUID().toString();
        Long automakerId = restTemplate
                .exchange(
                        "/automakers",
                        HttpMethod.POST,
                        new HttpEntity<>(new AutomakerRequest("Ferrari-" + unique, "Italy"), headers),
                        AutomakerResponse.class)
                .getBody()
                .id();
        Long brandId = restTemplate
                .exchange(
                        "/brands",
                        HttpMethod.POST,
                        new HttpEntity<>(new BrandRequest("Hot Wheels-" + unique), headers),
                        BrandResponse.class)
                .getBody()
                .id();
        Long seriesId = restTemplate
                .exchange(
                        "/series",
                        HttpMethod.POST,
                        new HttpEntity<>(new SeriesRequest("HW Starting Grid-" + unique, 2024), headers),
                        SeriesResponse.class)
                .getBody()
                .id();

        return new ModelRequest(
                name,
                2015,
                ModelScale.SCALE_1_64,
                "Red",
                ModelCondition.MINT,
                "3/10",
                false,
                new BigDecimal("12.99"),
                LocalDate.of(2024, 1, 15),
                "Target",
                "Great find",
                automakerId,
                brandId,
                seriesId);
    }

    private ModelResponse create(HttpHeaders headers, ModelRequest request) {
        return restTemplate
                .exchange("/models", HttpMethod.POST, new HttpEntity<>(request, headers), ModelResponse.class)
                .getBody();
    }
}
