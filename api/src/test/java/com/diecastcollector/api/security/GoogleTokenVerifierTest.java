package com.diecastcollector.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

class GoogleTokenVerifierTest {

    private static final String CLIENT_ID = "web-client.apps.googleusercontent.com";

    private static final KeyPair GOOGLE_KEY = rsaKeyPair();
    private static final KeyPair OTHER_KEY = rsaKeyPair();

    private final GoogleTokenVerifier verifier = verifier(CLIENT_ID);

    @Test
    void validTokenGivesTheIdentity() {
        VerifiedIdentity identity = verifier.verify(sign(GOOGLE_KEY, claims().build()));

        assertThat(identity).isEqualTo(new VerifiedIdentity("google-uid-1", "collector@example.com", "Collector"));
    }

    @Test
    void bothGoogleIssuerSpellingsAreAccepted() {
        String token = sign(GOOGLE_KEY, claims().issuer("accounts.google.com").build());

        assertThat(verifier.verify(token).providerUid()).isEqualTo("google-uid-1");
    }

    @Test
    void tokenSignedByAnotherKeyIsRejected() {
        assertRejected(verifier, sign(OTHER_KEY, claims().build()));
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        assertRejected(verifier, sign(GOOGLE_KEY, claims().issuer("https://evil.example.com").build()));
    }

    @Test
    void tokenIssuedToAnotherAppIsRejected() {
        assertRejected(verifier, sign(GOOGLE_KEY, claims().audience("other-app.apps.googleusercontent.com").build()));
    }

    @Test
    void expiredTokenIsRejected() {
        Instant past = Instant.now().minusSeconds(3600);
        String token = sign(GOOGLE_KEY, claims()
                .issueTime(Date.from(past.minusSeconds(3600)))
                .expirationTime(Date.from(past))
                .build());

        assertRejected(verifier, token);
    }

    @Test
    void everyTokenIsRejectedWhenNoClientIdIsConfigured() {
        // A blank audience check would accept a token Google issued to any other app.
        assertRejected(verifier(""), sign(GOOGLE_KEY, claims().build()));
        assertRejected(verifier(null), sign(GOOGLE_KEY, claims().build()));
    }

    @Test
    void malformedTokenIsRejected() {
        assertRejected(verifier, "not-a-jwt");
    }

    private static void assertRejected(GoogleTokenVerifier verifier, String token) {
        assertThatThrownBy(() -> verifier.verify(token)).isInstanceOf(InvalidTokenException.class);
    }

    private static GoogleTokenVerifier verifier(String clientId) {
        return new GoogleTokenVerifier(
                clientId, NimbusJwtDecoder.withPublicKey((RSAPublicKey) GOOGLE_KEY.getPublic()).build());
    }

    private static JWTClaimsSet.Builder claims() {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .audience(CLIENT_ID)
                .subject("google-uid-1")
                .claim("email", "collector@example.com")
                .claim("name", "Collector")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)));
    }

    private static String sign(KeyPair key, JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
            jwt.sign(new RSASSASigner(key.getPrivate()));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyPair rsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
