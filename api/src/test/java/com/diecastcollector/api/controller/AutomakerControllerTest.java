package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.AutomakerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AutomakerControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createAndFetchAutomaker() {
        var request = new AutomakerRequest("Ferrari");

        ResponseEntity<Object> created = restTemplate.postForEntity("/automakers", request, Object.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Object[]> all = restTemplate.getForEntity("/automakers", Object[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).isNotEmpty();
    }
}
