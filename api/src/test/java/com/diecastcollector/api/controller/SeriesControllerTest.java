package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.BrandRequest;
import com.diecastcollector.api.dto.BrandResponse;
import com.diecastcollector.api.dto.SeriesRequest;
import com.diecastcollector.api.dto.SeriesResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SeriesControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createSeries() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "Hot Wheels");

        ResponseEntity<SeriesResponse> response = post(headers, new SeriesRequest(brand.id(), "HW Starting Grid", 2026));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("HW Starting Grid");
        assertThat(response.getBody().year()).isEqualTo(2026);
        assertThat(response.getBody().brand().id()).isEqualTo(brand.id());
        assertThat(response.getBody().brand().name()).isEqualTo(brand.name());
    }

    @Test
    void fetchSeries() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "Matchbox");
        SeriesResponse created = create(headers, brand.id(), "Moving Parts", 2023);

        ResponseEntity<SeriesResponse> byId = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), SeriesResponse.class);
        assertThat(byId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byId.getBody().name()).isEqualTo("Moving Parts");
        assertThat(byId.getBody().year()).isEqualTo(2023);
        assertThat(byId.getBody().brand().name()).isEqualTo(brand.name());

        ResponseEntity<SeriesResponse[]> all = restTemplate.exchange(
                "/series", HttpMethod.GET, new HttpEntity<>(headers), SeriesResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).extracting(SeriesResponse::id).contains(created.id());
    }

    @Test
    void updateSeries() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "Hot Wheels");
        SeriesResponse created = create(headers, brand.id(), "Car Culture", 2022);

        ResponseEntity<SeriesResponse> updated =
                put(headers, created.id(), new SeriesRequest(brand.id(), "Car Culture: Speed Machines", 2023));

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("Car Culture: Speed Machines");
        assertThat(updated.getBody().year()).isEqualTo(2023);

        ResponseEntity<SeriesResponse> refetched = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), SeriesResponse.class);
        assertThat(refetched.getBody().name()).isEqualTo("Car Culture: Speed Machines");
        assertThat(refetched.getBody().year()).isEqualTo(2023);

        // Re-saving a Series with its own values isn't a duplicate of itself.
        ResponseEntity<SeriesResponse> unchanged =
                put(headers, created.id(), new SeriesRequest(brand.id(), "Car Culture: Speed Machines", 2023));
        assertThat(unchanged.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteSeries() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "Hot Wheels");
        SeriesResponse created = create(headers, brand.id(), "Pickup Trucks", 2021);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> refetch = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        assertThat(refetch.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sameNameAndYearAllowedForDifferentBrands() {
        var headers = authHeaders();
        BrandResponse hotWheels = createBrand(headers, "Hot Wheels");
        BrandResponse matchbox = createBrand(headers, "Matchbox");

        assertThat(post(headers, new SeriesRequest(hotWheels.id(), "Starting Grid", 2026)).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(post(headers, new SeriesRequest(matchbox.id(), "Starting Grid", 2026)).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void sameNameAllowedInDifferentYears() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "Hot Wheels");

        assertThat(post(headers, new SeriesRequest(brand.id(), "HW Starting Grid", 2025)).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(post(headers, new SeriesRequest(brand.id(), "HW Starting Grid", 2026)).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(post(headers, new SeriesRequest(brand.id(), "HW Starting Grid", null)).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void duplicateSeriesIsRejected() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "Hot Wheels");
        create(headers, brand.id(), "HW Starting Grid", 2026);

        ResponseEntity<Object> duplicate = restTemplate.exchange(
                "/series",
                HttpMethod.POST,
                new HttpEntity<>(new SeriesRequest(brand.id(), "HW Starting Grid", 2026), headers),
                Object.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void duplicateYearlessSeriesIsRejected() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "California Collectibles");
        create(headers, brand.id(), "Nissan Skyline R34", null);

        ResponseEntity<Object> duplicate = restTemplate.exchange(
                "/series",
                HttpMethod.POST,
                new HttpEntity<>(new SeriesRequest(brand.id(), "Nissan Skyline R34", null), headers),
                Object.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateIntoDuplicateIsRejected() {
        var headers = authHeaders();
        BrandResponse brand = createBrand(headers, "Hot Wheels");
        create(headers, brand.id(), "HW Starting Grid", 2026);
        SeriesResponse other = create(headers, brand.id(), "HW Exotics", 2026);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/series/" + other.id(),
                HttpMethod.PUT,
                new HttpEntity<>(new SeriesRequest(brand.id(), "HW Starting Grid", 2026), headers),
                Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createWithoutBrandIsRejected() {
        var headers = authHeaders();

        ResponseEntity<Object> response = restTemplate.exchange(
                "/series",
                HttpMethod.POST,
                new HttpEntity<>(new SeriesRequest(null, "HW Starting Grid", 2026), headers),
                Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createWithUnknownBrandIsRejected() {
        var headers = authHeaders();

        ResponseEntity<Object> response = restTemplate.exchange(
                "/series",
                HttpMethod.POST,
                new HttpEntity<>(new SeriesRequest(Long.MAX_VALUE, "HW Starting Grid", 2026), headers),
                Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private BrandResponse createBrand(HttpHeaders headers, String name) {
        // Brand names are globally unique and the test DB is shared across the suite, so suffix
        // each one (see AGENTS.md).
        return restTemplate
                .exchange(
                        "/brands",
                        HttpMethod.POST,
                        new HttpEntity<>(new BrandRequest(name + "-" + UUID.randomUUID()), headers),
                        BrandResponse.class)
                .getBody();
    }

    private SeriesResponse create(HttpHeaders headers, Long brandId, String name, Integer year) {
        return post(headers, new SeriesRequest(brandId, name, year)).getBody();
    }

    private ResponseEntity<SeriesResponse> post(HttpHeaders headers, SeriesRequest request) {
        return restTemplate.exchange("/series", HttpMethod.POST, new HttpEntity<>(request, headers), SeriesResponse.class);
    }

    private ResponseEntity<SeriesResponse> put(HttpHeaders headers, Long id, SeriesRequest request) {
        return restTemplate.exchange(
                "/series/" + id, HttpMethod.PUT, new HttpEntity<>(request, headers), SeriesResponse.class);
    }
}
