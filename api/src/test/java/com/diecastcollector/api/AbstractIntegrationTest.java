package com.diecastcollector.api;

import com.diecastcollector.api.domain.User;
import com.diecastcollector.api.enums.AuthProvider;
import com.diecastcollector.api.repository.UserRepository;
import com.diecastcollector.api.security.AppJwtService;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("diecast_collector")
                    .withUsername("diecast_collector")
                    .withPassword("diecast_collector");

    @Autowired private UserRepository userRepository;
    @Autowired private AppJwtService appJwtService;

    @BeforeAll
    static void startContainer() {
        POSTGRES.start();
    }

    @AfterAll
    static void stopContainer() {
        POSTGRES.stop();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    /** Persists a fresh test user and returns headers bearing a valid access token for them. */
    protected HttpHeaders authHeaders() {
        User user = userRepository.save(
                new User(AuthProvider.GOOGLE, "test-" + UUID.randomUUID(), "test@example.com", "Test User"));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(appJwtService.issueAccessToken(user));
        return headers;
    }
}
