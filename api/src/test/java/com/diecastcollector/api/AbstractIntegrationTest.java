package com.diecastcollector.api;

import com.diecastcollector.api.domain.User;
import com.diecastcollector.api.enums.AuthProvider;
import com.diecastcollector.api.repository.UserRepository;
import com.diecastcollector.api.security.AppJwtService;
import java.util.UUID;
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

    static {
        // Started once and never explicitly stopped (singleton container pattern): with several
        // test classes sharing this static field, per-class @BeforeAll/@AfterAll would stop and
        // restart it between classes, racing new connections against the restart. Testcontainers'
        // own Ryuk reaper stops it when the JVM exits.
        POSTGRES.start();
    }

    @Autowired private UserRepository userRepository;
    @Autowired private AppJwtService appJwtService;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // application.yml no longer ships a default (see APP_JWT_SECRET) — tests need their own.
        registry.add(
                "app.jwt.secret",
                () -> "test-only-jwt-signing-secret-not-for-production-use-0123456789abcdef");
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
