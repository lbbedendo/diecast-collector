package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.AutomakerRequest;
import com.diecastcollector.api.dto.AutomakerResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AutomakerControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    // Automaker names are unique and V6 seeds real ones (Ferrari, Honda, ...), so every name a test
    // creates gets a per-test suffix (JUnit makes a new instance per test method).
    private final String suffix = "-" + UUID.randomUUID();

    @Test
    void seededAutomakersArePresent() {
        var headers = authHeaders();

        ResponseEntity<AutomakerResponse[]> all = restTemplate.exchange(
                "/automakers", HttpMethod.GET, new HttpEntity<>(headers), AutomakerResponse[].class);

        assertThat(all.getBody())
                .extracting(AutomakerResponse::name, AutomakerResponse::country)
                .contains(tuple("Ferrari", "Italy"), tuple("Nissan", "Japan"), tuple("Ford", "United States"));
    }

    @Test
    void createAutomaker() {
        var headers = authHeaders();

        ResponseEntity<AutomakerResponse> response = restTemplate.exchange(
                "/automakers",
                HttpMethod.POST,
                new HttpEntity<>(new AutomakerRequest("Ferrari" + suffix, "Italy"), headers),
                AutomakerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Ferrari" + suffix);
        assertThat(response.getBody().country()).isEqualTo("Italy");
    }

    @Test
    void fetchAutomaker() {
        var headers = authHeaders();
        AutomakerResponse created = create(headers, "Honda" + suffix, "Japan");

        ResponseEntity<AutomakerResponse> byId = restTemplate.exchange(
                "/automakers/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), AutomakerResponse.class);
        assertThat(byId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byId.getBody().name()).isEqualTo("Honda" + suffix);
        assertThat(byId.getBody().country()).isEqualTo("Japan");

        ResponseEntity<AutomakerResponse[]> all = restTemplate.exchange(
                "/automakers", HttpMethod.GET, new HttpEntity<>(headers), AutomakerResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).extracting(AutomakerResponse::id).contains(created.id());
    }

    @Test
    void updateAutomaker() {
        var headers = authHeaders();
        AutomakerResponse created = create(headers, "Toyota" + suffix, "Japan");

        ResponseEntity<AutomakerResponse> updated = restTemplate.exchange(
                "/automakers/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(new AutomakerRequest("Toyota Motor Corporation" + suffix, "Japan"), headers),
                AutomakerResponse.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("Toyota Motor Corporation" + suffix);

        ResponseEntity<AutomakerResponse> refetched = restTemplate.exchange(
                "/automakers/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), AutomakerResponse.class);
        assertThat(refetched.getBody().name()).isEqualTo("Toyota Motor Corporation" + suffix);
    }

    @Test
    void deleteAutomaker() {
        var headers = authHeaders();
        AutomakerResponse created = create(headers, "Mazda" + suffix, "Japan");

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/automakers/" + created.id(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> refetch = restTemplate.exchange(
                "/automakers/" + created.id(), HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        assertThat(refetch.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private AutomakerResponse create(HttpHeaders headers, String name, String country) {
        return restTemplate
                .exchange(
                        "/automakers",
                        HttpMethod.POST,
                        new HttpEntity<>(new AutomakerRequest(name, country), headers),
                        AutomakerResponse.class)
                .getBody();
    }
}
