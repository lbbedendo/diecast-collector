package com.diecastcollector.api.security;

import com.diecastcollector.api.enums.AuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Verifies a Google Sign-In ID token by checking its signature against Google's published JWKS
 * and validating issuer/audience/expiry. This is the server-side half of the mobile app's
 * "Sign in with Google" flow: the app gets an ID token from the Google SDK, sends it here, and
 * we never trust it without re-verifying the signature ourselves.
 */
@Component
public class GoogleTokenVerifier implements SocialTokenVerifier {

    private static final String ISSUER = "https://accounts.google.com";
    private static final String JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";

    private final JwtDecoder jwtDecoder;

    public GoogleTokenVerifier(@Value("${app.auth.google.client-id}") String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(JWKS_URI).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        this.jwtDecoder = token -> {
            Jwt jwt = decoder.decode(token);
            if (clientId != null && !clientId.isBlank()) {
                var audience = jwt.getAudience();
                if (audience == null || !audience.contains(clientId)) {
                    throw new JwtException("Token audience does not match configured Google client id");
                }
            }
            return jwt;
        };
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public VerifiedIdentity verify(String idToken) {
        try {
            Jwt jwt = jwtDecoder.decode(idToken);
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            return new VerifiedIdentity(jwt.getSubject(), email, name);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid Google ID token", e);
        }
    }
}
