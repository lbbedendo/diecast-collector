package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.AutomakerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AutomakerControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createAndFetchAutomaker() {
        var headers = authHeaders();
        var request = new AutomakerRequest("Ferrari", "Italy");

        ResponseEntity<Object> created = restTemplate.exchange(
                "/automakers", HttpMethod.POST, new HttpEntity<>(request, headers), Object.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Object[]> all = restTemplate.exchange(
                "/automakers", HttpMethod.GET, new HttpEntity<>(headers), Object[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).isNotEmpty();
    }
}
