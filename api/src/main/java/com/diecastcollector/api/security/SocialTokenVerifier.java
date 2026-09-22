package com.diecastcollector.api.security;

import com.diecastcollector.api.enums.AuthProvider;

public interface SocialTokenVerifier {
    AuthProvider provider();

    /** Verifies the ID token's signature, issuer and audience, and extracts the caller's identity. */
    VerifiedIdentity verify(String idToken);
}
