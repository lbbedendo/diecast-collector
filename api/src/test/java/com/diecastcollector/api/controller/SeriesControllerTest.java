package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.SeriesRequest;
import com.diecastcollector.api.dto.SeriesResponse;
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

        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                "/series",
                HttpMethod.POST,
                new HttpEntity<>(new SeriesRequest("HW Starting Grid", 2026), headers),
                SeriesResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("HW Starting Grid");
        assertThat(response.getBody().year()).isEqualTo(2026);
    }

    @Test
    void fetchSeries() {
        var headers = authHeaders();
        SeriesResponse created = create(headers, "Moving Parts", 2023);

        ResponseEntity<SeriesResponse> byId = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), SeriesResponse.class);
        assertThat(byId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byId.getBody().name()).isEqualTo("Moving Parts");
        assertThat(byId.getBody().year()).isEqualTo(2023);

        ResponseEntity<SeriesResponse[]> all = restTemplate.exchange(
                "/series", HttpMethod.GET, new HttpEntity<>(headers), SeriesResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).extracting(SeriesResponse::id).contains(created.id());
    }

    @Test
    void updateSeries() {
        var headers = authHeaders();
        SeriesResponse created = create(headers, "Car Culture", 2022);

        ResponseEntity<SeriesResponse> updated = restTemplate.exchange(
                "/series/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(new SeriesRequest("Car Culture: Speed Machines", 2023), headers),
                SeriesResponse.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("Car Culture: Speed Machines");
        assertThat(updated.getBody().year()).isEqualTo(2023);

        ResponseEntity<SeriesResponse> refetched = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), SeriesResponse.class);
        assertThat(refetched.getBody().name()).isEqualTo("Car Culture: Speed Machines");
        assertThat(refetched.getBody().year()).isEqualTo(2023);
    }

    @Test
    void deleteSeries() {
        var headers = authHeaders();
        SeriesResponse created = create(headers, "Pickup Trucks", 2021);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> refetch = restTemplate.exchange(
                "/series/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        assertThat(refetch.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private SeriesResponse create(HttpHeaders headers, String name, Integer year) {
        return restTemplate
                .exchange(
                        "/series",
                        HttpMethod.POST,
                        new HttpEntity<>(new SeriesRequest(name, year), headers),
                        SeriesResponse.class)
                .getBody();
    }
}
