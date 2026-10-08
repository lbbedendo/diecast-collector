package com.diecastcollector.api.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the app-issued JWT as a bearer scheme so Swagger UI shows an "Authorize" button, and
 * requires it on every operation by default. Public controllers (/auth, /photos) opt out with an
 * empty {@code @SecurityRequirements}, mirroring the permitAll() list in {@link SecurityConfig}.
 *
 * <p>The token to paste is the API's own JWT ({@code accessToken} from {@code POST /auth/google},
 * {@code /auth/apple}, or {@code /auth/dev} locally), not a Google/Apple ID token.
 */
@Configuration
public class OpenApiConfig {

    static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("Diecast Collector API"))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("The accessToken returned by POST /auth/google, /auth/apple or /auth/dev")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
