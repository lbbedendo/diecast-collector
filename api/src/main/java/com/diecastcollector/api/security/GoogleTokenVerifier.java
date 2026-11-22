package com.diecastcollector.api.security;

import com.diecastcollector.api.enums.AuthProvider;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Verifies a Google Sign-In ID token by checking its signature against Google's published JWKS
 * and validating issuer/audience/expiry. This is the server-side half of the mobile app's
 * "Sign in with Google" flow: the app gets an ID token from the Google SDK, sends it here, and
 * we never trust it without re-verifying the signature ourselves.
 *
 * <p>The audience must be the configured Web OAuth client ID ({@code GOOGLE_CLIENT_ID}) — the
 * {@code serverClientId} the Android app asks Credential Manager for. With no client ID configured
 * every token is rejected: skipping the audience check would accept a token Google issued to any
 * other app.
 */
@Component
public class GoogleTokenVerifier implements SocialTokenVerifier {

    private static final String JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    // Google documents both spellings as valid issuers of its ID tokens.
    private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    private final String clientId;
    private final NimbusJwtDecoder decoder;

    @Autowired
    public GoogleTokenVerifier(@Value("${app.auth.google.client-id}") String clientId) {
        this(clientId, NimbusJwtDecoder.withJwkSetUri(JWKS_URI).build());
    }

    /** Takes the signature-checking decoder so tests can sign tokens with their own key. */
    GoogleTokenVerifier(String clientId, NimbusJwtDecoder decoder) {
        this.clientId = clientId;
        this.decoder = decoder;
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.of(
                new JwtTimestampValidator(),
                new JwtClaimValidator<String>(JwtClaimNames.ISS, ISSUERS::contains),
                jwt -> jwt.getAudience() != null && jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                                "invalid_token", "Token audience does not match configured Google client id", null)))));
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public VerifiedIdentity verify(String idToken) {
        if (clientId == null || clientId.isBlank()) {
            throw new InvalidTokenException("Google sign-in is not configured: GOOGLE_CLIENT_ID is not set");
        }
        try {
            Jwt jwt = decoder.decode(idToken);
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            return new VerifiedIdentity(jwt.getSubject(), email, name);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid Google ID token", e);
        }
    }
}
