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
 * Verifies a "Sign in with Apple" identity token the same way {@link GoogleTokenVerifier} verifies
 * Google's: against Apple's published JWKS, with issuer/audience/expiry checks. Apple's token
 * carries email only on first authorization, so the app should send us the email/display name it
 * received from ASAuthorizationAppleIDCredential on first login and we fall back to it below.
 */
@Component
public class AppleTokenVerifier implements SocialTokenVerifier {

    private static final String ISSUER = "https://appleid.apple.com";
    private static final String JWKS_URI = "https://appleid.apple.com/auth/keys";

    private final JwtDecoder jwtDecoder;

    public AppleTokenVerifier(@Value("${app.auth.apple.client-id}") String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(JWKS_URI).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        this.jwtDecoder = token -> {
            Jwt jwt = decoder.decode(token);
            if (clientId != null && !clientId.isBlank()) {
                var audience = jwt.getAudience();
                if (audience == null || !audience.contains(clientId)) {
                    throw new JwtException("Token audience does not match configured Apple client/service id");
                }
            }
            return jwt;
        };
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.APPLE;
    }

    @Override
    public VerifiedIdentity verify(String idToken) {
        try {
            Jwt jwt = jwtDecoder.decode(idToken);
            // Apple only includes email/name in the identity token's payload on the very first
            // authorization; subsequent logins omit them. The client is responsible for capturing
            // and forwarding them once (e.g. as part of the first SocialLoginRequest) if you need
            // to persist more than the stable `sub`.
            String email = jwt.getClaimAsString("email");
            return new VerifiedIdentity(jwt.getSubject(), email, null);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid Apple identity token", e);
        }
    }
}
